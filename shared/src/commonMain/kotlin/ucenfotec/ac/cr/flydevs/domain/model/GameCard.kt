package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GameCard(
    val id: String,
    val name: String,
    val description: String,
)