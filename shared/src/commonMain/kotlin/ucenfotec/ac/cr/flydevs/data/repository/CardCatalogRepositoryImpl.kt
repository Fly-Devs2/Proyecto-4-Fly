package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.firestore
import ucenfotec.ac.cr.flydevs.domain.model.CardCondition
import ucenfotec.ac.cr.flydevs.domain.model.CardGame
import ucenfotec.ac.cr.flydevs.domain.model.CardLanguage
import ucenfotec.ac.cr.flydevs.domain.model.CardStatus
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

        return snapshot.documents.map { document ->
            mapDocumentToCard(document)
        }
    }

    override suspend fun getCardsBySeller(sellerId: String): List<GameCard> {
        val snapshot = gameCardsCollection.where { "sellerId" equalTo sellerId }.get()
        return snapshot.documents.map { document ->
            mapDocumentToCard(document)
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

    private fun mapDocumentToCard(document: DocumentSnapshot): GameCard {
        return GameCard(
            id = document.id,
            name = document.get<String>("name") ?: "",
            description = document.get<String>("description") ?: "",
            expansion = document.get<String>("expansion") ?: "",
            condition = document.get<CardCondition>("condition") ?: CardCondition.NEAR_MINT,
            language = document.get<CardLanguage>("language") ?: CardLanguage.EN,
            imageUrl = document.get<String>("imageUrl") ?: "",
            price = document.get<Long>("price") ?: 0L,
            quantity = document.get<Int>("quantity") ?: 1,
            sellerId = document.get<String>("sellerId") ?: "",
            status = document.get<CardStatus>("status") ?: CardStatus.AVAILABLE,
            game = document.get<CardGame>("game") ?: CardGame.ONE_PIECE,
            rarity = document.get<String>("rarity") ?: ""
        )
    }
}
