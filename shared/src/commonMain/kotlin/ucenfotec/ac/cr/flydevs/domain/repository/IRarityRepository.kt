package ucenfotec.ac.cr.flydevs.domain.repository

import ucenfotec.ac.cr.flydevs.domain.model.CardGame

interface IRarityRepository {

    suspend fun getRarities(game: CardGame): List<String>
}
