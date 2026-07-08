package ucenfotec.ac.cr.flydevs.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.model.User
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository

class ProfileViewModel(
    private val authRepository: IAuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init { loadProfile() }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                val uid = authRepository.getCurrentUserUid() ?: throw Exception("Usuario no autenticado")
                authRepository.getUserProfile(uid)
            }.onSuccess { user ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = user,
                    editedName = user?.name ?: "",
                    editedPhone = user?.phone ?: "",
                    editedLocation = "",
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Error al cargar perfil",
                )
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(editedName = value, saveSuccess = false)
    }

    fun onPhoneChange(value: String) {
        _uiState.value = _uiState.value.copy(editedPhone = value, saveSuccess = false)
    }

    fun onLocationChange(value: String) {
        _uiState.value = _uiState.value.copy(editedLocation = value, saveSuccess = false)
    }

    fun saveChanges() {
        val current = _uiState.value
        val user = current.user ?: return

        viewModelScope.launch {
            _uiState.value = current.copy(isSaving = true, errorMessage = null, saveSuccess = false)
            runCatching {
                val updated = user.copy(
                    name = current.editedName.trim(),
                    phone = current.editedPhone.trim(),
                )
                authRepository.saveUserProfile(updated)
                updated
            }.onSuccess { updated ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    user = updated,
                    saveSuccess = true,
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = error.message ?: "No se pudo guardar el perfil",
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            runCatching { authRepository.signOut() }
                .onSuccess { _uiState.value = ProfileUiState(isSignedOut = true) }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Error al cerrar sesión",
                    )
                }
        }
    }
}
