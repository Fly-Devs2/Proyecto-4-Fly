package ucenfotec.ac.cr.flydevs.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.auth.GoogleAuthManager
import ucenfotec.ac.cr.flydevs.domain.repository.AuthRepository

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val googleAuthManager: GoogleAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                authRepository.login(email, password)
            }.onSuccess {
                _uiState.value = LoginUiState(isLoginSuccessful = true)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Error al iniciar sesión"
                )
            }
        }
    }

    fun onGoogleSignInClicked() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                googleAuthManager.signIn()
            }.onSuccess { idToken ->
                if (idToken != null) {
                    performFirebaseGoogleSignIn(idToken)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Inicio de sesión cancelado"
                    )
                }
            }.onFailure { error ->
                val message = when {
                    error.message?.contains("cancel", ignoreCase = true) == true ->
                        "Inicio de sesión cancelado por el usuario"
                    else -> error.message ?: "Error al conectar con Google"
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = message
                )
            }
        }
    }

    private suspend fun performFirebaseGoogleSignIn(idToken: String) {
        runCatching {
            val uid = authRepository.signInWithGoogle(idToken)
            val profile = authRepository.getUserProfile(uid)
            
            if (profile == null || profile.phone.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    needsProfileCompletion = true
                )
            } else {
                _uiState.value = LoginUiState(isLoginSuccessful = true)
            }
        }.onFailure { error ->
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = error.message ?: "Error al iniciar sesión con Google"
            )
        }
    }
    
    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }
}
