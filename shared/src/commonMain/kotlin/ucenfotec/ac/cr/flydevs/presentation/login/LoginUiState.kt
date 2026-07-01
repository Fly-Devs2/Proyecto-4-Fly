package ucenfotec.ac.cr.flydevs.presentation.login

import kotlinx.serialization.Serializable

@Serializable
data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val needsProfileCompletion: Boolean = false,
    val errorMessage: String? = null
)
