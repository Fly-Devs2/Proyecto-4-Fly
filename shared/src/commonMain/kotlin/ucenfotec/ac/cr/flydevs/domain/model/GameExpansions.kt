package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GameExpansions(
    val items: List<String> = emptyList(),
)

