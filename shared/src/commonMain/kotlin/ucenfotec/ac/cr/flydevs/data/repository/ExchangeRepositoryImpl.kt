package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import ucenfotec.ac.cr.flydevs.domain.model.Exchange
import ucenfotec.ac.cr.flydevs.domain.model.ExchangeStatus
import ucenfotec.ac.cr.flydevs.domain.repository.IExchangeRepository

class ExchangeRepositoryImpl : IExchangeRepository {

    private val exchangesCollection by lazy {
        Firebase.firestore.collection("orders")
    }

    override suspend fun createExchange(exchange: Exchange): Exchange {

        val doc = exchangesCollection.document
        val withId = exchange.copy(id = doc.id)
        doc.set(withId)
        return withId
    }

    override suspend fun getExchange(exchangeId: String): Exchange? {
        val snapshot = exchangesCollection.document(exchangeId).get()
        return if (snapshot.exists) snapshot.data<Exchange>() else null
    }

    override suspend fun getExchangesForBuyer(buyerId: String): List<Exchange> {
        val snapshot = exchangesCollection.where { "buyerId" equalTo buyerId }.get()
        return snapshot.documents.map { it.data<Exchange>() }
    }

    override suspend fun submitSellerEvidence(exchangeId: String, evidenceUrl: String): Exchange {
        val current = getExchange(exchangeId)
            ?: throw IllegalStateException("Intercambio no encontrado: $exchangeId")

        val updated = current.copy(
            sellerEvidenceUrl = evidenceUrl,
            status = ExchangeStatus.WAITING_SINPE_PROOF,
        )
        exchangesCollection.document(exchangeId).set(updated)
        return updated
    }

    override suspend fun submitSinpeProof(exchangeId: String, proofUrl: String): Exchange {
        val current = getExchange(exchangeId)
            ?: throw IllegalStateException("Intercambio no encontrado: $exchangeId")

        require(current.status == ExchangeStatus.WAITING_SINPE_PROOF) {
            "No se puede subir el comprobante SINPE: falta la evidencia del vendedor."
        }

        val updated = current.copy(
            sinpeProofUrl = proofUrl,
            status = ExchangeStatus.PROOF_RECEIVED,
        )
        exchangesCollection.document(exchangeId).set(updated)
        return updated
    }
}
