package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class OrderCardSnapshot(
    val cardId: String = "",
    val name: String = "",
    val imageUrls: List<String> = emptyList(),
    val price: Long = 0L,
    val condition: String = "",
    val game: String = ""
) {
    @Transient
    val imageUrl: String = imageUrls.firstOrNull() ?: ""
}
