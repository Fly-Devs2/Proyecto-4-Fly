package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GameCard(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val expansion: String = "",
    val condition: String = "",
    val language: String = "",
    val imageUrl: String = "",
    val price: Long = 0L,
    val quantity: Long = 0L,
    val sellerId: String = "",
    val status: String = ""
)