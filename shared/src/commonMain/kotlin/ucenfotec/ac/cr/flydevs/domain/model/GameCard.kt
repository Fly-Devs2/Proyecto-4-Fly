package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GameCard(
    val id: String = "",
    val sellerId: String = "",           // vendedor dueño de la publicación
    val name: String = "",
    val expansion: String = "",
    val condition: String = "",
    val language: String = "",
    val price: Long = 0L,
    val quantity: Int = 1,
    val description: String = "",
    val imageUrl: String = "",
    val status: String = "available",   // visibilidad en el marketplace
)
