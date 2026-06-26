package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GameCard(
    val id: String = "",
    val sellerId: String = "",
    val name: String = "",
    val game: CardGame? = null,
    val expansion: String? = null,
    val rarity: String? = null,
    val condition: CardCondition = CardCondition.NEAR_MINT,
    val language: CardLanguage = CardLanguage.EN,
    val price: Long = 0L,
    val quantity: Int = 1,
    val description: String = "",
    val imageUrl: String = "",
    val status: CardStatus = CardStatus.AVAILABLE,
)
