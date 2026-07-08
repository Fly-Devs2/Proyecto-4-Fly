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
    private val ordersCollection = Firebase.firestore.collection("ORDERS")
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

        println("DEBUG_ENVELOPE_REPO: addCardToEnvelope END SUCCESS envelopeId=$envelopeId")

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

        val currentCardIds = getStringList(envelopeDocument, "cartas_agregadas")

        if (!currentCardIds.contains(cardId)) {
            throw Exception("La carta no existe dentro del sobre.")
        }

        val updatedCardIds = currentCardIds.filter { currentCardId ->
            currentCardId != cardId
        }

        val totals = calculateTotals(updatedCardIds)

        cardEnvelopesCollection.document(envelopeId).update(
            "cartas_agregadas" to updatedCardIds,
            "cantidad_cartas" to updatedCardIds.size.toLong(),
            "sub_total" to totals.first,
            "monto_total" to totals.second
        )

        gameCardsCollection.document(cardId).update(
            "status" to CardStatus.AVAILABLE.name
        )

        println("DEBUG_ENVELOPE_REPO: removeCardFromEnvelope END SUCCESS")
    }

    override suspend fun generateOrderFromEnvelope(
        envelopeId: String
    ) {
        println("DEBUG_ENVELOPE_REPO: generateOrderFromEnvelope START")
        println("DEBUG_ENVELOPE_REPO: envelopeId=$envelopeId")

        val envelopeDocument = cardEnvelopesCollection
            .document(envelopeId)
            .get()

        val cardIds = getStringList(envelopeDocument, "cartas_agregadas")

        if (cardIds.isEmpty()) {
            throw Exception("No se puede generar una orden con el sobre vacío.")
        }

        validateCardsBeforeOrder(cardIds)

        val totals = calculateTotals(cardIds)

        cardEnvelopesCollection.document(envelopeId).update(
            "sub_total" to totals.first,
            "monto_total" to totals.second,
            "cantidad_cartas" to cardIds.size.toLong(),
            "status" to "ORDER_GENERATED"
        )

        println("DEBUG_ENVELOPE_REPO: generateOrderFromEnvelope END SUCCESS")
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

        val cardIds = getStringList(envelopeDocument, "cartas_agregadas")

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
            val cardIds = getStringList(document, "cartas_agregadas")
            val cards = getCardsByIds(cardIds)

            CardEnvelope(
                id = getStringValue(document, "id", document.id),
                cardIds = cardIds,
                cards = cards,
                subTotal = getLongValue(document, "sub_total"),
                total = getLongValue(document, "monto_total"),
                status = getStringValue(document, "status", "PENDING"),
                shippingMethod = getShippingMethod(document),
                userId = getStringValue(document, "userID", ""),
                sellerId = getStringValue(document, "sellerId", "")
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

        val snapshot = cardEnvelopesCollection.get()

        println("DEBUG_ENVELOPE_REPO: total envelopes=${snapshot.documents.size}")

        snapshot.documents.forEach { document ->
            val documentUserId = getStringValue(document, "userID")
            val documentSellerId = getStringValue(document, "sellerId")
            val documentStatus = getStringValue(document, "status", "PENDING")

            println("DEBUG_ENVELOPE_REPO: envelopeId=${document.id}")
            println("DEBUG_ENVELOPE_REPO: envelope userID=$documentUserId")
            println("DEBUG_ENVELOPE_REPO: envelope sellerId=$documentSellerId")
            println("DEBUG_ENVELOPE_REPO: envelope status=$documentStatus")
        }

        return snapshot.documents.firstOrNull { document ->
            val documentUserId = getStringValue(document, "userID")
            val documentSellerId = getStringValue(document, "sellerId")
            val documentStatus = getStringValue(document, "status", "PENDING")

            documentUserId == userId &&
                    documentSellerId == sellerId &&
                    documentStatus == "PENDING"
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
        sellerId: String,
        cardId: String,
        cardPrice: Long
    ): String {


        val newEnvelopeDocument = cardEnvelopesCollection.document
        val shippingCost = defaultShippingCost
        val total = cardPrice + shippingCost

        val envelopeData = mapOf(
            "id" to newEnvelopeDocument.id,
            "userID" to userId,
            "sellerId" to sellerId,
            "cartas_agregadas" to listOf(cardId),
            "cantidad_cartas" to 1L,
            "sub_total" to cardPrice,
            "monto_total" to total,
            "shippingMethod" to ShippingMethod.DELIVERY.name,
            "status" to "PENDING",
            "createdAt" to System.currentTimeMillis()
        )

        newEnvelopeDocument.set(envelopeData)

        println("DEBUG_ENVELOPE_REPO: createEnvelopeWithCard END SUCCESS")
        return newEnvelopeDocument.id
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