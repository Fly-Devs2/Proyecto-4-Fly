package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.DocumentSnapshot
import ucenfotec.ac.cr.flydevs.data.repository.GameCardMapper.toGameCard
import ucenfotec.ac.cr.flydevs.domain.model.*
import ucenfotec.ac.cr.flydevs.domain.repository.ICardEnvelopeRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IOrderQrRepository
import ucenfotec.ac.cr.flydevs.getEpochMillis

class CardEnvelopeRepositoryImpl: ICardEnvelopeRepository {
    private val cardEnvelopesCollection = Firebase.firestore.collection("envelopes")
    private val gameCardsCollection = Firebase.firestore.collection("game_cards")
    private val ordersCollection = Firebase.firestore.collection("orders")
    private val usersCollection = Firebase.firestore.collection("users")
    private val defaultShippingCost = 600L
    private val orderQrRepository: IOrderQrRepository = OrderQrRepositoryImpl()

    override suspend fun getCardEnvelopebyUser(userId: String): List<CardEnvelope> {
        if (userId.isBlank()) {
            return emptyList()
        }

        val snapshot = cardEnvelopesCollection
            .where {
                "userId" equalTo userId
            }
            .get()

        val envelopes = mutableListOf<CardEnvelope>()

        snapshot.documents.forEach { document ->
            val envelope = mapDocumentToEnvelope(document)

            if (envelope != null) {
                envelopes.add(envelope)
            }
        }

        return envelopes
    }

    override suspend fun addCardToEnvelope(
        userId: String,
        cardId: String
    ): String {
        println("DEBUG_ENVELOPE_REPO: addCardToEnvelope START")
        println("DEBUG_ENVELOPE_REPO: userId=$userId")
        println("DEBUG_ENVELOPE_REPO: cardId=$cardId")

        if (userId.isBlank()) {
            throw Exception("No se encontró el usuario comprador.")
        }

        val card = getCardById(cardId)
            ?: throw Exception("No se encontró la carta seleccionada.")

        if (card.sellerId.isBlank()) {
            throw Exception("La carta no tiene vendedor asignado.")
        }

        if (card.status != CardStatus.AVAILABLE) {
            throw Exception("La carta ya no está disponible.")
        }

        val pendingEnvelopeDocument = getPendingEnvelopeDocumentByUserAndSeller(
            userId = userId,
            sellerId = card.sellerId
        )

        val envelopeId = if (pendingEnvelopeDocument == null) {
            println("DEBUG_ENVELOPE_REPO: Creating new envelope for seller=${card.sellerId}")

            createEnvelopeWithCard(
                userId = userId,
                sellerId = card.sellerId,
                cardId = cardId,
                cardPrice = card.price
            )
        } else {
            println("DEBUG_ENVELOPE_REPO: Adding card to existing envelope=${pendingEnvelopeDocument.id}")

            addCardToExistingEnvelope(
                envelopeDocument = pendingEnvelopeDocument,
                cardId = cardId
            )

            pendingEnvelopeDocument.id
        }

        gameCardsCollection.document(cardId).update(
            "status" to CardStatus.RESERVED.name
        )



        return envelopeId
    }

    override suspend fun removeCardFromEnvelope(
        envelopeId: String,
        cardId: String
    ) {
        println("DEBUG_ENVELOPE_REPO: removeCardFromEnvelope START")
        println("DEBUG_ENVELOPE_REPO: envelopeId=$envelopeId")
        println("DEBUG_ENVELOPE_REPO: cardId=$cardId")

        val envelopeDocument = cardEnvelopesCollection
            .document(envelopeId)
            .get()

        val currentCardIds = getStringList(envelopeDocument, "cardIds")

        if (!currentCardIds.contains(cardId)) {
            throw Exception("La carta no existe dentro del sobre.")
        }

        val updatedCardIds = currentCardIds.filter { currentCardId ->
            currentCardId != cardId
        }

        val totals = calculateTotals(updatedCardIds)

        cardEnvelopesCollection.document(envelopeId).update(
            "cardsAdded" to updatedCardIds,
            "cardsQuantity" to updatedCardIds.size.toLong(),
            "subTotal" to totals.first,
            "total" to totals.second
        )

        gameCardsCollection.document(cardId).update(
            "status" to CardStatus.AVAILABLE.name
        )


    }

    override suspend fun generateOrderFromEnvelope(
        envelopeId: String
    ) {
        println("DEBUG_ENVELOPE_REPO: generateOrderFromEnvelope START")
        println("DEBUG_ENVELOPE_REPO: envelopeId=$envelopeId")

        val envelopeDocument = cardEnvelopesCollection
            .document(envelopeId)
            .get()

        val envelope = mapDocumentToEnvelope(envelopeDocument)
            ?: throw Exception("No se pudo cargar la información del sobre.")

        if (envelope.cardIds.isEmpty()) {
            throw Exception("No se puede generar una orden con el sobre vacío.")
        }

        validateCardsBeforeOrder(envelope.cardIds)

        val totals = calculateTotals(envelope.cardIds)

        val seller = fetchUser(envelope.sellerId)
        val buyer = fetchUser(envelope.userId)

        // 1. Create the Order document in orders collection
        val newOrderDoc = ordersCollection.document
        val order = Order(
            id = newOrderDoc.id,
            buyerId = envelope.userId,
            sellerId = envelope.sellerId,
            sellerName = seller?.name ?: "Vendedor",
            buyerName = buyer?.name ?: "Comprador",
            cards = envelope.cards.map {
                OrderCardSnapshot(
                    cardId = it.id,
                    name = it.name,
                    imageUrls = it.imageUrls,
                    price = it.price,
                    condition = it.condition.label,
                    game = it.game?.label ?: ""
                )
            },
            status = OrderStatus.WAITING_SELLER_DELIVERY,
            createdAt = getEpochMillis(),
            modifiedAt = getEpochMillis(),
            montoTotal = totals.second,
            sobreId = envelope.id,
            shippingMethod = envelope.shippingMethod.name,
            sourceStore = envelope.cards.firstOrNull()?.sourceStore ?: "",
            destinationStore = envelope.destinationStore,
            sellerEvidenceUrls = emptyList(),
            buyerEvidenceUrls = emptyList()
        )

        newOrderDoc.set(Order.serializer(), order)

        println("DEBUG_ENVELOPE_REPO: Order created successfully")
        println("DEBUG_ENVELOPE_REPO: orderId=${order.id}")

        // 2. Update the Envelope status
        cardEnvelopesCollection.document(envelopeId).update(
            "subTotal" to totals.first,
            "total" to totals.second,
            "cardsQuantity" to envelope.cardIds.size.toLong(),
            "status" to "ORDER_GENERATED"
        )

        println("DEBUG_ENVELOPE_REPO: Envelope updated to ORDER_GENERATED")


        // 3. Generate QR for the created order
        try {
            val qrResult = orderQrRepository.generateOrderQr(
                orderId = order.id,
                forceRegenerate = false
            )

            println("DEBUG_ENVELOPE_REPO: QR generated successfully")
            println("DEBUG_ENVELOPE_REPO: orderId=${qrResult.orderId}")
            println("DEBUG_ENVELOPE_REPO: qrId=${qrResult.qrId}")
            println("DEBUG_ENVELOPE_REPO: qrImagePath=${qrResult.qrImagePath}")
            println("DEBUG_ENVELOPE_REPO: reused=${qrResult.reused}")

        } catch (e: Exception) {
            println("DEBUG_ENVELOPE_REPO: QR generation failed for orderId=${order.id}")
            println("DEBUG_ENVELOPE_REPO: error=${e.message}")

            ordersCollection.document(order.id).update(
                "qrStatus" to "ERROR",
                "qrLastError" to (e.message ?: "Error desconocido generando QR"),
                "qrLastErrorAt" to getEpochMillis()
            )

            throw Exception(
                "La orden fue creada, pero no se pudo generar el código QR. Intente nuevamente.",
                e
            )
        }



    }

    private suspend fun fetchUser(uid: String): User? {
        return try {
            val doc = usersCollection.document(uid).get()
            if (doc.exists) doc.data<User>() else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getCardEnvelopeById(
        envelopeId: String
    ): CardEnvelope? {
        return try {
            println("DEBUG_ENVELOPE_REPO: getCardEnvelopeById envelopeId=$envelopeId")

            val document = cardEnvelopesCollection
                .document(envelopeId)
                .get()

            mapDocumentToEnvelope(document)
        } catch (exception: Exception) {
            println("ERROR_ENVELOPE_REPO: Error loading envelope $envelopeId: ${exception.message}")
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

    override suspend fun deleteEnvelope(envelopeId: String) {
        println("DEBUG_ENVELOPE_REPO: deleteEnvelope START")
        println("DEBUG_ENVELOPE_REPO: envelopeId=$envelopeId")

        val envelopeDocument = cardEnvelopesCollection
            .document(envelopeId)
            .get()

        val status = getStringValue(envelopeDocument, "status", "PENDING")

        if (status != "PENDING") {
            throw Exception("Solo se pueden eliminar sobres pendientes.")
        }

        val cardIds = getStringList(envelopeDocument, "cardsAdded")

        println("DEBUG_ENVELOPE_REPO: cards to release=$cardIds")

        cardIds.forEach { cardId ->
            gameCardsCollection.document(cardId).update(
                "status" to CardStatus.AVAILABLE.name
            )
        }

        cardEnvelopesCollection
            .document(envelopeId)
            .delete()

        println("DEBUG_ENVELOPE_REPO: deleteEnvelope END SUCCESS")
    }




    private suspend fun mapDocumentToEnvelope(
        document: DocumentSnapshot
    ): CardEnvelope? {
        return try {
            val cardIds = getStringList(document, "cardsAdded")
            val cards = getCardsByIds(cardIds)

            CardEnvelope(
                id = getStringValue(document, "id", document.id),
                cardIds = cardIds,
                cards = cards,
                subTotal = getLongValue(document, "subTotal"),
                total = getLongValue(document, "total"),
                status = getStringValue(document, "status", "PENDING"),
                shippingMethod = getShippingMethod(document),
                userId = getStringValue(document, "userId", ""),
                sellerId = getStringValue(document, "sellerId", ""),
                destinationStore = getStringValue(document, "destinationStore", ""),
                sourceStore = getStringValue(document, "sourceStore", "")
            )
        } catch (e: Exception) {
            println("ERROR_ENVELOPE: Error mapping envelope ${document.id}: ${e.message}")
            null
        }
    }

    private suspend fun getPendingEnvelopeDocumentByUserAndSeller(
        userId: String,
        sellerId: String
    ): DocumentSnapshot? {
        println("DEBUG_ENVELOPE_REPO: Searching pending envelope by user and seller")
        println("DEBUG_ENVELOPE_REPO: userId=$userId")
        println("DEBUG_ENVELOPE_REPO: sellerId=$sellerId")

        val snapshot = cardEnvelopesCollection
            .where {
                "userId" equalTo userId
            }
            .get()

        println("DEBUG_ENVELOPE_REPO: envelopes found for user=${snapshot.documents.size}")

        return snapshot.documents.firstOrNull { document ->
            val documentSellerId = getStringValue(document, "sellerId")
            val documentStatus = getStringValue(document, "status", "PENDING")

            println("DEBUG_ENVELOPE_REPO: envelopeId=${document.id}")
            println("DEBUG_ENVELOPE_REPO: documentSellerId=$documentSellerId")
            println("DEBUG_ENVELOPE_REPO: documentStatus=$documentStatus")

            documentSellerId == sellerId && documentStatus == "PENDING"
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
            document.toGameCard().copy(
                id = cardId
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
        sellerId: String,
        cardId: String,
        cardPrice: Long
    ): String {


        val newEnvelopeDocument = cardEnvelopesCollection.document
        val shippingCost = defaultShippingCost
        val total = cardPrice + shippingCost

        val envelopeData = mapOf(
            "id" to newEnvelopeDocument.id,
            "userId" to userId,
            "sellerId" to sellerId,
            "cardsAdded" to listOf(cardId),
            "cardsQuantity" to 1L,
            "subTotal" to cardPrice,
            "total" to total,
            "shippingMethod" to ShippingMethod.DELIVERY.name,
            "status" to "PENDING",
            "createdAt" to System.currentTimeMillis(),
            "sourceStore" to (getCardById(cardId)?.sourceStore ?: "")
        )

        newEnvelopeDocument.set(envelopeData)

        println("DEBUG_ENVELOPE_REPO: createEnvelopeWithCard END SUCCESS")
        return newEnvelopeDocument.id
    }

    override suspend fun updateEnvelopeDestinationStore(envelopeId: String, destinationStore: String) {
        cardEnvelopesCollection.document(envelopeId).update("destinationStore" to destinationStore)
    }

    override suspend fun updateAllEnvelopesDestinationStore(userId: String, destinationStore: String) {
        val snapshot = cardEnvelopesCollection.where { "userId" equalTo userId }.get()
        snapshot.documents.forEach { doc ->
            if (getStringValue(doc, "status") == "PENDING") {
                cardEnvelopesCollection.document(doc.id).update("destinationStore" to destinationStore)
            }
        }
    }

    private suspend fun addCardToExistingEnvelope(
        envelopeDocument: DocumentSnapshot,
        cardId: String
    ) {
        val currentCardIds = getStringList(envelopeDocument, "cardsAdded")

        if (currentCardIds.contains(cardId)) {
            throw Exception("Esta carta ya fue agregada al sobre.")
        }

        val updatedCardIds = currentCardIds + cardId
        val totals = calculateTotals(updatedCardIds)

        cardEnvelopesCollection.document(envelopeDocument.id).update(
            "cardsAdded" to updatedCardIds,
            "cardsQuantity" to updatedCardIds.size.toLong(),
            "subTotal" to totals.first,
            "total" to totals.second
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