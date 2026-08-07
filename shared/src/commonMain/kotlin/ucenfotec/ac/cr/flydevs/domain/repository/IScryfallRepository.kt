package ucenfotec.ac.cr.flydevs.domain.repository

import ucenfotec.ac.cr.flydevs.domain.model.ScryfallCard

interface IScryfallRepository {
    suspend fun getCardPrints(cardName: String, language: String? = null): List<ScryfallCard>
}
