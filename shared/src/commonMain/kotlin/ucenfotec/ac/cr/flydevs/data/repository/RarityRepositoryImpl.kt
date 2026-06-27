package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import ucenfotec.ac.cr.flydevs.domain.model.CardGame
import ucenfotec.ac.cr.flydevs.domain.model.GameRarities
import ucenfotec.ac.cr.flydevs.domain.repository.IRarityRepository

class FirestoreRarityRepository : IRarityRepository {

    private val raritiesCollection by lazy {
        Firebase.firestore.collection("rarities")
    }

    override suspend fun getRarities(game: CardGame): List<String> {
        val snapshot = raritiesCollection.document(game.name).get()
        return if (snapshot.exists) snapshot.data<GameRarities>().items else emptyList()
    }
}

