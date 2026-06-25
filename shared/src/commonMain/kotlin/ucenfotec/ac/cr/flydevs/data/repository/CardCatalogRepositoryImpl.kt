package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import ucenfotec.ac.cr.flydevs.domain.model.GameCard
import ucenfotec.ac.cr.flydevs.domain.repository.CardCatalogRepository

class CardCatalogRepositoryImpl : CardCatalogRepository {

    private val gameCardsCollection =
        Firebase.firestore.collection("game_cards")

    override suspend fun getCardCatalog(): List<GameCard> {
        println( " DEBUG_CATALOG: Fetching game cards from Firestore...")

        val snapshot= gameCardsCollection.get()

        println(" DEBUG_CATALOG: Snapshot size: ${snapshot.documents.size}")

        val cards = snapshot.documents.map { document ->
            println(" DEBUG_CATALOG: Processing document ID: ${document.id}")

            val card = GameCard(
                id = document.id,
                name = document.get<String>("name") ?: "",
                description = document.get<String>("description") ?: "",
                expansion = document.get<String>("expansion") ?: "",
                condition = document.get<String>("condition") ?: "",
                language = document.get<String>("language") ?: "",
                imageUrl = document.get<String>("imageUrl") ?: "",
                price = document.get<Long>("price") ?: 0L,
                quantity = document.get<Long>("quantity") ?: 0L,
                sellerId = document.get<String>("sellerId") ?: "",
                status = document.get<String>("status") ?: ""
            )
            println("Fetched card: ${card.name}, ID: ${card.id}, Status: ${card.status}, Image URL: ${card.imageUrl}, Price: ${card.price}, Quantity: ${card.quantity}")

            card
        }

        return cards
    }

}




