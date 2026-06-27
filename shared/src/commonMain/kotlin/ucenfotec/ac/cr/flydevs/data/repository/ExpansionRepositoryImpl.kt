package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import ucenfotec.ac.cr.flydevs.domain.model.CardGame
import ucenfotec.ac.cr.flydevs.domain.model.GameExpansions
import ucenfotec.ac.cr.flydevs.domain.repository.IExpansionRepository

class FirestoreExpansionRepository : IExpansionRepository {

    private val expansionsCollection by lazy {
        Firebase.firestore.collection("expansions")
    }

    override suspend fun getExpansions(game: CardGame): List<String> {
        val snapshot = expansionsCollection.document(game.name).get()
        return if (snapshot.exists) snapshot.data<GameExpansions>().items else emptyList()
    }
}

