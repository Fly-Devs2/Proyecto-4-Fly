package ucenfotec.ac.cr.flydevs.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.model.User
import ucenfotec.ac.cr.flydevs.domain.model.UserRole
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository

data class SessionUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val errorMessage: String? = null
) {
    val userRole: UserRole
        get() = user?.userRole ?: UserRole.USER
}

class SessionViewModel(
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                val uid = authRepository.getCurrentUserUid()

                if (uid == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = null
                        )
                    }
                    return@launch
                }

                val user = authRepository.getUserProfile(uid)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = user
                    )
                }

            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
            }
        }
    }

    fun clearSession() {
        _uiState.value = SessionUiState(
            isLoading = false,
            user = null
        )
    }
}