package ucenfotec.ac.cr.flydevs.presentation.register

import kotlinx.serialization.Serializable

@Serializable
data class RegisterUiState(
    val isLoading: Boolean = false,
    val isRegistered: Boolean = false,
    val needsProfileCompletion: Boolean = false,
    val errorMessage: String? = null
)
