package ucenfotec.ac.cr.flydevs.domain.repository

import ucenfotec.ac.cr.flydevs.domain.model.GameCard

interface CardCatalogRepository {

    suspend fun getCardCatalog(): List<GameCard>
}