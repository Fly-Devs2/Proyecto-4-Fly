package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class OrderCardSnapshot(
    val cardId: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val price: Long = 0L,
    val condition: String = "",
    val game: String = ""
)
