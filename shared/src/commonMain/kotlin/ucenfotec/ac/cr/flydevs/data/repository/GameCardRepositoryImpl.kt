package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import ucenfotec.ac.cr.flydevs.domain.model.GameCard
import ucenfotec.ac.cr.flydevs.domain.repository.GameCardRepository

class GameCardRepositoryImpl : GameCardRepository {

    private val gameCardsCollection by lazy {
        Firebase.firestore.collection("game_cards")
    }

    override suspend fun saveGameCard(gameCard: GameCard): GameCard {
        // genera un auto id único
        val doc = gameCardsCollection.document

        val cardWithId = gameCard.copy(id = doc.id)
        doc.set(cardWithId)
        return cardWithId
    }
}
