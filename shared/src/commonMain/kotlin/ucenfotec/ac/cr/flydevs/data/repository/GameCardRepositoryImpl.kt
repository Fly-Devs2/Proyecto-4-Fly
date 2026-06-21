package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import ucenfotec.ac.cr.flydevs.domain.model.GameCard
import ucenfotec.ac.cr.flydevs.domain.repository.GameCardRepository

class GameCardRepositoryImpl : GameCardRepository {
    private val gameCardsCollection =
        Firebase.firestore.collection("game_cards")

    override suspend fun saveGameCard(gameCard: GameCard) {
        gameCardsCollection
            .document(gameCard.id)
            .set(gameCard)
    }
}
