package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

/**
 * Notificación genérica del sistema
 */
@Serializable
data class AppNotification(
    val id: String = "",
    val userId: String = "",
    val type: String = "",
    val title: String = "",
    val body: String = "",
    val data: Map<String, String> = emptyMap(),
    val read: Boolean = false,
    val createdAt: Long = 0L
) {
    companion object {
        const val TYPE_ORDER_STATUS_CHANGED = "ORDER_STATUS_CHANGED"
    }
}
