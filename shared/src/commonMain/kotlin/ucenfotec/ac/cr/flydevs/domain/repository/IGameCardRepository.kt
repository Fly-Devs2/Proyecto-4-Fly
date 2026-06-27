package ucenfotec.ac.cr.flydevs.domain.repository

import ucenfotec.ac.cr.flydevs.domain.model.GameCard

interface IGameCardRepository {

    suspend fun saveGameCard(gameCard: GameCard): GameCard
}