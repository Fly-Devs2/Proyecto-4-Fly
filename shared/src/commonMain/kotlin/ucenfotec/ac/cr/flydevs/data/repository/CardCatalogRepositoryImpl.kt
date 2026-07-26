package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import ucenfotec.ac.cr.flydevs.data.repository.GameCardMapper.toGameCard
import ucenfotec.ac.cr.flydevs.domain.model.GameCard
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.ICardCatalogRepository

class CardCatalogRepositoryImpl(
    private val authRepository: IAuthRepository
) : ICardCatalogRepository {

    private val gameCardsCollection =
        Firebase.firestore.collection("game_cards")

    private val usersCollection = Firebase.firestore.collection("users")

    override suspend fun getCardCatalog(): List<GameCard> {
        println(" DEBUG_CATALOG: Fetching game cards from Firestore...")

        val currentUserId = authRepository.getCurrentUserUid()
        val query = if (currentUserId != null) {
            gameCardsCollection.where { "sellerId" notEqualTo currentUserId }
        } else {
            gameCardsCollection
        }
        val snapshot = query.get()

        println(" DEBUG_CATALOG: Snapshot size: ${snapshot.documents.size}")

        return snapshot.documents.mapNotNull { document ->
            try {
                document.toGameCard()
            } catch (e: Exception) {
                println("ERROR_CATALOG: Failed to map document ${document.id}: ${e.message}")
                null
            }
        }
    }

    override suspend fun getCardsBySeller(sellerId: String): List<GameCard> {
        val snapshot = gameCardsCollection.where { "sellerId" equalTo sellerId }.get()
        return snapshot.documents.mapNotNull { document ->
            try {
                document.toGameCard()
            } catch (e: Exception) {
                println("ERROR_CATALOG: Failed to map document ${document.id}: ${e.message}")
                null
            }
        }
    }

    override suspend fun getSellerNameById(sellerId: String): String? {
        val snapshot = usersCollection.document(sellerId).get()
        return if (snapshot.exists) {
            snapshot.get<String>("name")
        } else {
            null
        }
    }
}
