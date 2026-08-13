package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.firestore.DocumentSnapshot
import ucenfotec.ac.cr.flydevs.domain.model.CardCondition
import ucenfotec.ac.cr.flydevs.domain.model.CardGame
import ucenfotec.ac.cr.flydevs.domain.model.CardLanguage
import ucenfotec.ac.cr.flydevs.domain.model.CardStatus
import ucenfotec.ac.cr.flydevs.domain.model.GameCard

object GameCardMapper {

    fun DocumentSnapshot.toGameCard(): GameCard {
        val id = this.id
        val name = getSafeString("name")
        val description = getSafeString("description")
        val expansion = getSafeString("expansion")
        val sellerId = getSafeString("sellerId")
        val rarity = getSafeString("rarity")
        val sourceStore = getSafeString("sourceStore")

        // Manejo robusto de imageUrls con fallback a imageUrl antiguo
        val imageUrls = getImageUrls()

        return GameCard(
            id = id,
            name = name,
            description = description,
            expansion = expansion,
            condition = getSafeEnum<CardCondition>("condition") ?: CardCondition.NEAR_MINT,
            language = getSafeEnum<CardLanguage>("language") ?: CardLanguage.EN,
            imageUrls = imageUrls,
            price = getSafeLong("price"),
            quantity = getSafeInt("quantity"),
            sellerId = sellerId,
            status = getSafeEnum<CardStatus>("status") ?: CardStatus.AVAILABLE,
            game = getSafeEnum<CardGame>("game") ?: CardGame.ONE_PIECE,
            rarity = rarity,
            sourceStore = sourceStore
        )
    }

    private fun DocumentSnapshot.getSafeString(field: String): String {
        return try {
            get<String>(field) ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun DocumentSnapshot.getSafeInt(field: String): Int {
        return try {
            get<Int>(field) ?: 1
        } catch (e: Exception) {
            1
        }
    }

    private fun DocumentSnapshot.getSafeLong(field: String): Long {
        return try {
            get<Long>(field) ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private inline fun <reified T : Enum<T>> DocumentSnapshot.getSafeEnum(field: String): T? {
        return try {
            val value = get<T>(field)
            value
        } catch (e: Exception) {
            try {
                val stringValue = get<String>(field)
                enumValueOf<T>(stringValue)
            } catch (e2: Exception) {
                null
            }
        }
    }

    private fun DocumentSnapshot.getImageUrls(): List<String> {
        // Primero intenta leer imageUrls (nuevo formato)
        return try {
            val urls = get<List<String>>("imageUrls")
            if (urls != null && urls.isNotEmpty()) {
                urls
            } else {
                // Si imageUrls existe pero está vacía, intentar fallback a imageUrl
                getFallbackToImageUrl()
            }
        } catch (e: Exception) {
            // Si imageUrls no existe, fallback a imageUrl antiguo
            getFallbackToImageUrl()
        }
    }

    private fun DocumentSnapshot.getFallbackToImageUrl(): List<String> {
        return try {
            val oldImageUrl = get<String>("imageUrl")
            if (oldImageUrl != null && oldImageUrl.isNotEmpty()) {
                listOf(oldImageUrl)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
