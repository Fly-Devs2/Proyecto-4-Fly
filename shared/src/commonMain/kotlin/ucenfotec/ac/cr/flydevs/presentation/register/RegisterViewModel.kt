package ucenfotec.ac.cr.flydevs.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.auth.GoogleAuthManager
import ucenfotec.ac.cr.flydevs.domain.repository.AuthRepository

class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val googleAuthManager: GoogleAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(name: String, email: String, phone: String, password: String) {
        if (_uiState.value.isLoading) return

        val passwordRegex = Regex("^(?=.*[A-Z]).{12,30}$")
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        val phoneRegex = Regex("^[5678][0-9]{7}$")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            runCatching {
                if (name.isBlank()) {
                    throw Exception("El nombre es requerido")
                }

                if (name.length > 50) {
                    throw Exception("El nombre del usuario debe contener 50 o menos caracteres")
                }
                if (email.isBlank()) {
                    throw Exception("El correo electrónico es requerido")
                }

                if (!emailRegex.matches(email)) {
                    throw Exception("El formato del correo electrónico es inválido")
                }

                if (email.length > 254) {
                    throw Exception("El correo electrónico debe tener menos de 254 caracteres")
                }
                if (phone.isBlank()) {
                    throw Exception("El teléfono  es requerido")
                }
                if (!passwordRegex.matches(password)) {
                    throw Exception("La contraseña debe tener entre 12 y 30 caracteres y al menos una letra mayúscula")
                }

                if (!phoneRegex.matches(phone)) {
                    throw Exception("El formato del teléfono es inválido. Debe ser de Costa Rica y empezar con 5, 6, 7, u 8.")
                }
                authRepository.register(name, email, phone, password)
            }.onSuccess {
                _uiState.value = RegisterUiState(isRegistered = true)
            }.onFailure { error ->
                val message = when {
                    error.message?.contains("cancel", ignoreCase = true) == true ->
                        "Inicio de sesión cancelado por el usuario"
                    error.message?.contains("The email address is badly formatted.", ignoreCase = true) == true ->
                        "El formato de correo esta incorrecto. Ejemplo: flyapp@gmail.com"
                    else -> error.message ?: "Error al iniciar sesion"
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = message
                )
            }
        }
    }

    fun onGoogleSignInClicked() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = runCatching {
                googleAuthManager.signIn()
            }

            result.onSuccess { idToken ->
                if (idToken != null) {
                    performFirebaseGoogleSignIn(idToken)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "El proveedor de Google no devolvió un Token ID. Verifica que el 'Web Client ID' sea el correcto en la consola de Google Cloud."
                    )
                }
            }.onFailure { error ->
                val message = when {
                    error.message?.contains("cancel", ignoreCase = true) == true ->
                        "Inicio de sesión cancelado por el usuario"
                    error.message?.contains("No credentials available", ignoreCase = true) == true -> 
                        "No hay cuentas de Google configuradas en este dispositivo"
                    error.message?.contains("The email address is badly formatted.", ignoreCase = true) == true ->
                        "El formato de correo esta incorrecto. Ejemplo: flyapp@gmail.com"
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
                _uiState.value = RegisterUiState(isRegistered = true)
            }
        }.onFailure { error ->
            _uiState.value = RegisterUiState(
                errorMessage = error.message ?: "Error al iniciar sesión con Google"
            )
        }
    }

    fun completeProfile(name: String, phone: String) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                val uid = authRepository.getCurrentUserUid() ?: throw Exception("Sesión no encontrada")
                val email = authRepository.getCurrentUserEmail() ?: ""
                
                val user = ucenfotec.ac.cr.flydevs.domain.model.User(
                    uid = uid,
                    name = name,
                    email = email,
                    phone = phone
                )
                authRepository.saveUserProfile(user)
            }.onSuccess {
                _uiState.value = RegisterUiState(isRegistered = true)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Error al completar el perfil"
                )
            }
        }
    }

    fun resetError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
