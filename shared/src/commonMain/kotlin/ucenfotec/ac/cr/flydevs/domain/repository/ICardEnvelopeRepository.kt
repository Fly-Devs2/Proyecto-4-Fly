package ucenfotec.ac.cr.flydevs.domain.repository

import ucenfotec.ac.cr.flydevs.domain.model.CardEnvelope

interface ICardEnvelopeRepository {

    suspend fun getCardEnvelopes(): List<CardEnvelope>

    suspend fun getCardEnvelopebyUser(userId: String): List<CardEnvelope>


    //Agrega una carta al sobre pendiente del usuario. Si el usuario no tiene sobre pendiente, crea uno.
    suspend fun addCardToEnvelope(userId: String, cardId: String)


    //Quita una carta del sobre antes de generar la orden.
    suspend fun removeCardFromEnvelope(userId: String, cardId: String)

    suspend fun generateOrderFromEnvelope(userId: String)


}