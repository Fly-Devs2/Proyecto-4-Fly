package ucenfotec.ac.cr.flydevs.domain.repository

import ucenfotec.ac.cr.flydevs.domain.model.CardGame

interface IExpansionRepository {

    suspend fun getExpansions(game: CardGame): List<String>
}
