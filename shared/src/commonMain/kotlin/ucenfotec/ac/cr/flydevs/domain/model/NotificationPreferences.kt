package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class NotificationPreferences(
    val orderStatusChanged: Boolean = true,
    val sinpeReminders: Boolean = true,
    val pickupReminders: Boolean = true
)
