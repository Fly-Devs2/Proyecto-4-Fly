package ucenfotec.ac.cr.flydevs.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.model.AppNotification
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.INotificationRepository

class NotificationsViewModel(
    private val authRepository: IAuthRepository,
    private val notificationRepository: INotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        val uid = authRepository.getCurrentUserUid()
        if (uid == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Usuario no autenticado"
            )
            return
        }

        notificationRepository.getNotificationsForUser(uid)
            .onEach { notifications ->
                _uiState.value = _uiState.value.copy(
                    notifications = notifications,
                    isLoading = false,
                    errorMessage = null
                )
            }
            .catch { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar notificaciones: ${error.message}"
                )
            }
            .launchIn(viewModelScope)
    }

    fun markAsRead(notification: AppNotification) {
        if (notification.read) return
        viewModelScope.launch {
            runCatching { notificationRepository.markAsRead(notification.id) }
        }
    }

    fun markAllAsRead() {
        val uid = authRepository.getCurrentUserUid() ?: return
        viewModelScope.launch {
            runCatching { notificationRepository.markAllAsRead(uid) }
        }
    }
}
