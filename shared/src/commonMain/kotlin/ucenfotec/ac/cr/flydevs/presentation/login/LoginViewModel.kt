package ucenfotec.ac.cr.flydevs.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.auth.GoogleAuthManager
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository

class LoginViewModel(
    private val IAuthRepository: IAuthRepository,
    private val googleAuthManager: GoogleAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun login(email: String, password: String) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                if (email.isBlank()) {
                    throw Exception("El correo electrónico es requerido")
                }
                if (email.length > 254) {
                    throw Exception("El correo electrónico debe tener menos de 254 caracteres")
                }
                if (!emailRegex.matches(email)) {
                    throw Exception("El formato del correo electrónico es inválido")
                }
                if (password.length > 50) {
                    throw Exception("La contraseña debe tener 50 o menos caracteres")
                }
                if (password.isBlank()) {
                    throw Exception("La contraseña es requerida")
                }
                IAuthRepository.login(email, password)
            }.onSuccess {
                _uiState.value = LoginUiState(isLoginSuccessful = true)
            }.onFailure { error ->
                val displayMessage = when {
                    error.message?.contains("The supplied auth credential is incorrect", ignoreCase = true) == true ||
                    error.message?.contains("invalid-credential", ignoreCase = true) == true ->
                        "El correo electrónico o la contraseña son incorrectos"
                    else -> error.message ?: "Error al iniciar sesión"
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = displayMessage
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
            val uid = IAuthRepository.signInWithGoogle(idToken)
            val profile = IAuthRepository.getUserProfile(uid)
            
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
        return IAuthRepository.isUserLoggedIn()
    }
}
