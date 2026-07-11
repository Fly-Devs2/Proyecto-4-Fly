package ucenfotec.ac.cr.flydevs.presentation.notifications

import ucenfotec.ac.cr.flydevs.domain.model.NotificationPreferences

data class NotificationPreferencesUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val preferences: NotificationPreferences = NotificationPreferences(),
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
)
