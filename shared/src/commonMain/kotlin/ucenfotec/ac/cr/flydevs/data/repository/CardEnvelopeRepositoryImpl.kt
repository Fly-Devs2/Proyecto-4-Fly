package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import ucenfotec.ac.cr.flydevs.domain.model.*
import dev.gitlive.firebase.firestore.DocumentSnapshot
import ucenfotec.ac.cr.flydevs.domain.repository.ICardEnvelopeRepository
import ucenfotec.ac.cr.flydevs.getEpochMillis

class CardEnvelopeRepositoryImpl: ICardEnvelopeRepository {
    private val cardEnvelopesCollection = Firebase.firestore.collection("sobres")
    private val gameCardsCollection = Firebase.firestore.collection("game_cards")
    private val ordersCollection = Firebase.firestore.collection("orders")
    private val usersCollection = Firebase.firestore.collection("users")
    private val defaultShippingCost = 600L

    override suspend fun getCardEnvelopebyUser(userId: String): List<CardEnvelope> {
        println("DEBUG_ENVELOPE: Fetching envelopes for user $userId")
        println("DEBUG_ENVELOPE: Total envelopes in collection: ${getCardEnvelopes().size}")
        println("Cards: ${getCardEnvelopes().size}")
        return getCardEnvelopes().filter { envelope ->
            envelope.userId == userId
        }
    }

    override suspend fun addCardToEnvelope(userId: String, cardId: String) {
        println("DEBUG_ENVELOPE: Adding card $cardId to envelope for user $userId")

        val card = getCardById(cardId)?:throw Exception("No se encontró la carta seleccionada.")
        if(card.status != CardStatus.AVAILABLE){
            throw Exception("La carta seleccionada no está disponible.")
        }
        val pendingEnvelopeDocument = getPendingEnvelopeDocumentByUser(userId)
        if (pendingEnvelopeDocument == null) {
            createEnvelopeWithCard(userId, cardId, card.price)
        } else {
            addCardToExistingEnvelope(pendingEnvelopeDocument, cardId)
        }
        gameCardsCollection.document(cardId).update(
            "status" to CardStatus.RESERVED.name
        )
        println("DEBUG_ENVELOPE: Card $cardId added successfully")

    }

    override suspend fun removeCardFromEnvelope(userId: String, cardId: String) {
        val pendingEnvelopeDocument = getPendingEnvelopeDocumentByUser(userId)
            ?: throw Exception("No existe un sobre pendiente para este usuario.")

        val currentCardIds = getStringList(pendingEnvelopeDocument, "cartas_agregadas")

        if (!currentCardIds.contains(cardId)) {
            throw Exception("La carta no existe dentro del sobre.")
        }
        val updatedCardIds = currentCardIds.filter { currentCardId ->
            currentCardId != cardId
        }
        val totals = calculateTotals(updatedCardIds)

        cardEnvelopesCollection.document(pendingEnvelopeDocument.id).update(
            "cartas_agregadas" to updatedCardIds,
            "cantidad_cartas" to updatedCardIds.size.toLong(),
            "sub_total" to totals.first,
            "monto_total" to totals.second
        )

        gameCardsCollection.document(cardId).update(
            "status" to CardStatus.AVAILABLE.name
        )

        println("DEBUG_ENVELOPE: Card $cardId removed successfully")


    }

    override suspend fun generateOrderFromEnvelope(userId: String) {
        println("DEBUG_ENVELOPE: Generating order from envelope for user $userId")

        val pendingEnvelopeDocument = getPendingEnvelopeDocumentByUser(userId)
            ?: throw Exception("No existe un sobre pendiente para generar la orden.")

        val cardIds = getStringList(pendingEnvelopeDocument, "cartas_agregadas")

        if (cardIds.isEmpty()) {
            throw Exception("No se puede generar una orden con el sobre vacío.")
        }

        validateCardsBeforeOrder(cardIds)

        val cards = getCardsByIds(cardIds)
        if (cards.isEmpty()) throw Exception("No se pudieron recuperar las cartas del sobre.")

        // Logic to determine seller: for now, assume all cards in an envelope belong to the same seller
        // or we take the seller of the first card.
        val firstCard = cards.first()
        val sellerId = firstCard.sellerId
        
        // Fetch Names
        val seller = fetchUser(sellerId)
        val buyer = fetchUser(userId)

        val totals = calculateTotals(cardIds)

        // 1. Create the Order document in ORDERS collection
        val newOrderDoc = ordersCollection.document
        val order = Order(
            id = newOrderDoc.id,
            buyerId = userId,
            sellerId = sellerId,
            sellerName = seller?.name ?: "Vendedor",
            buyerName = buyer?.name ?: "Comprador",
            cards = cards.map { 
                OrderCardSnapshot(
                    cardId = it.id,
                    name = it.name,
                    imageUrl = it.imageUrl,
                    price = it.price,
                    condition = it.condition.label,
                    game = it.game?.label ?: ""
                )
            },
            status = OrderStatus.WAITING_SELLER_DELIVERY,
            createdAt = getEpochMillis(),
            modifiedAt = getEpochMillis(),
            montoTotal = totals.second,
            sobreId = pendingEnvelopeDocument.id,
            shippingMethod = getShippingMethod(pendingEnvelopeDocument).name,
            sellerEvidenceUrls = emptyList(),
            buyerEvidenceUrls = emptyList()
        )

        newOrderDoc.set(Order.serializer(), order)

        // 2. Update the Envelope status
        cardEnvelopesCollection.document(pendingEnvelopeDocument.id).update(
            "sub_total" to totals.first,
            "monto_total" to totals.second,
            "cantidad_cartas" to cardIds.size.toLong(),
            "status" to "ORDER_GENERATED",
            "orderId" to newOrderDoc.id
        )

        // 3. Mark cards as reserved (already done in addCard, but ensuring consistency)
        cardIds.forEach { cardId ->
            gameCardsCollection.document(cardId).update(
                "status" to CardStatus.RESERVED.name
            )
        }
        
        println("DEBUG_ENVELOPE: Order ${newOrderDoc.id} generated successfully")
    }

    private suspend fun fetchUser(uid: String): User? {
        return try {
            val doc = usersCollection.document(uid).get()
            if (doc.exists) doc.data<User>() else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getCardEnvelopes(): List<CardEnvelope> {
        val snapshot = cardEnvelopesCollection.get()

        println("DEBUG_ENVELOPE: Snapshot size: ${snapshot.documents.size}")

        val envelopes = mutableListOf<CardEnvelope>()



        snapshot.documents.forEach{document->
            val envelope = mapDocumentToEnvelope(document)
            if(envelope != null){
                envelopes.add(envelope as CardEnvelope)
            }
        }
        return envelopes


    }





    private suspend fun mapDocumentToEnvelope(
        document: DocumentSnapshot
    ): CardEnvelope? {
        return try {
            val cardIds = try {
                document.get<List<String>>("cartas_agregadas")
            } catch (e: Exception) {
                emptyList()
            }

            val cards = getCardsByIds(cardIds)

            CardEnvelope(
                id = try {
                    document.get<String>("id")
                } catch (e: Exception) {
                    document.id
                },
                cardIds = cardIds,
                cards = cards,
                subTotal = getLongValue(document, "sub_total"),
                total = getLongValue(document, "monto_total"),
                status = getStringValue(document, "status", "PENDING"),
                shippingMethod = getShippingMethod(document),
                userId = getStringValue(document, "userID", "")
            )
        } catch (e: Exception) {
            println("ERROR_ENVELOPE: Error mapping envelope ${document.id}: ${e.message}")
            null
        }
    }

    private suspend fun getCardsByIds(cardIds: List<String>): List<GameCard> {
        val cards = mutableListOf<GameCard>()

        cardIds.forEach { cardId ->
            try {
                val cardDocument = gameCardsCollection.document(cardId).get()
                val card = cardDocument.data<GameCard>()

                cards.add(
                    card.copy(
                        id = card.id.ifBlank { cardId }
                    )
                )
            } catch (e: Exception) {
                println("ERROR_CARD: Error fetching card $cardId: ${e.message}")
            }
        }

        return cards
    }
    private fun getStringValue(
        document: DocumentSnapshot,
        field: String,
        defaultValue: String = ""
    ): String {
        return try {
            document.get<String>(field)
        } catch (e: Exception) {
            defaultValue
        }
    }

    private fun getLongValue(
        document: DocumentSnapshot,
        field: String
    ): Long {
        return try {
            document.get<Long>(field)
        } catch (e: Exception) {
            try {
                document.get<Double>(field).toLong()
            } catch (e: Exception) {
                0L
            }
        }
    }

    private fun getShippingMethod(
        document: DocumentSnapshot
    ): ShippingMethod {
        return try {
            val shippingMethod = document.get<String>("shippingMethod")
            ShippingMethod.valueOf(shippingMethod)
        } catch (e: Exception) {
            ShippingMethod.DELIVERY
        }
    }
    private suspend fun getCardById(cardId: String): GameCard? {
        return try {
            val document = gameCardsCollection.document(cardId).get()
            val card = document.data<GameCard>()

            card.copy(
                id = card.id.ifBlank { cardId }
            )
        } catch (exception: Exception) {
            println("ERROR_CARD: Error getting card $cardId: ${exception.message}")
            null
        }
    }

    private suspend fun getPendingEnvelopeDocumentByUser(
        userId: String
    ): DocumentSnapshot? {
        val snapshot = cardEnvelopesCollection.get()

        return snapshot.documents.firstOrNull { document ->
            val documentUserId = getStringValue(document, "userID")
            val documentStatus = getStringValue(document, "status", "PENDING")

            documentUserId == userId && documentStatus == "PENDING"
        }
    }

    private suspend fun createEnvelopeWithCard(
        userId: String,
        cardId: String,
        cardPrice: Long
    ) {
        val newEnvelopeDocument = cardEnvelopesCollection.document
        val shippingCost = defaultShippingCost
        val total = cardPrice + shippingCost

        newEnvelopeDocument.set(
            mapOf(
                "id" to newEnvelopeDocument.id,
                "userID" to userId,
                "cartas_agregadas" to listOf(cardId),
                "cantidad_cartas" to 1L,
                "sub_total" to cardPrice,
                "monto_total" to total,
                "shippingMethod" to ShippingMethod.DELIVERY.name,
                "status" to "PENDING",
                "createdAt" to System.currentTimeMillis()
            )
        )
    }

    private suspend fun addCardToExistingEnvelope(
        envelopeDocument: DocumentSnapshot,
        cardId: String
    ) {
        val currentCardIds = getStringList(envelopeDocument, "cartas_agregadas")

        if (currentCardIds.contains(cardId)) {
            throw Exception("Esta carta ya fue agregada al sobre.")
        }

        val updatedCardIds = currentCardIds + cardId
        val totals = calculateTotals(updatedCardIds)

        cardEnvelopesCollection.document(envelopeDocument.id).update(
            "cartas_agregadas" to updatedCardIds,
            "cantidad_cartas" to updatedCardIds.size.toLong(),
            "sub_total" to totals.first,
            "monto_total" to totals.second
        )
    }

    private suspend fun validateCardsBeforeOrder(cardIds: List<String>) {
        cardIds.forEach { cardId ->
            val card = getCardById(cardId)
                ?: throw Exception("Una de las cartas ya no existe.")

            if (card.status == CardStatus.SOLD) {
                throw Exception("La carta ${card.name} ya fue vendida.")
            }
        }
    }

    private suspend fun calculateTotals(
        cardIds: List<String>
    ): Pair<Long, Long> {
        var subTotal = 0L

        cardIds.forEach { cardId ->
            val card = getCardById(cardId)
                ?: throw Exception("No se pudo calcular el total. Una carta no existe.")

            subTotal += card.price
        }

        val shippingCost = if (cardIds.isEmpty()) {
            0L
        } else {
            defaultShippingCost
        }

        val total = subTotal + shippingCost

        return Pair(subTotal, total)
    }

    private fun getStringList(
        document: DocumentSnapshot,
        field: String
    ): List<String> {
        return try {
            document.get<List<String>>(field)
        } catch (e: Exception) {
            emptyList()
        }
    }






}