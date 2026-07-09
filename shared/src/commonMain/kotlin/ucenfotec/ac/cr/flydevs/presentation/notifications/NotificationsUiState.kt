package ucenfotec.ac.cr.flydevs.presentation.notifications

import ucenfotec.ac.cr.flydevs.domain.model.AppNotification

data class NotificationsUiState(
    val notifications: List<AppNotification> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val unreadCount: Int get() = notifications.count { !it.read }
}
