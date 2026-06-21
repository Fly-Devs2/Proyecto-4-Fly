package ucenfotec.ac.cr.flydevs.domain.repository

import ucenfotec.ac.cr.flydevs.domain.model.GameCard

interface GameCardRepository {

    suspend fun saveGameCard(gameCard: GameCard)
}