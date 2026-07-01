package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GameRarities(
    val items: List<String> = emptyList(),
)
