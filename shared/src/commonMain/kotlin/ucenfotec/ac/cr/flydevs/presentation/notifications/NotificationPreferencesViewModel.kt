package ucenfotec.ac.cr.flydevs.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.INotificationRepository

class NotificationPreferencesViewModel(
    private val authRepository: IAuthRepository,
    private val notificationRepository: INotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationPreferencesUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        val userId = authRepository.getCurrentUserUid() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val prefs = notificationRepository.getPreferences(userId)
                _uiState.update { it.copy(isLoading = false, preferences = prefs) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun onOrderStatusChangedToggle(enabled: Boolean) {
        _uiState.update { 
            it.copy(
                preferences = it.preferences.copy(orderStatusChanged = enabled),
                saveSuccess = false 
            ) 
        }
    }

    fun onSinpeRemindersToggle(enabled: Boolean) {
        _uiState.update { 
            it.copy(
                preferences = it.preferences.copy(sinpeReminders = enabled),
                saveSuccess = false 
            ) 
        }
    }

    fun savePreferences() {
        val userId = authRepository.getCurrentUserUid() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                notificationRepository.updatePreferences(userId, _uiState.value.preferences)
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
            }
        }
    }
}
