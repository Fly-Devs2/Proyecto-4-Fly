package ucenfotec.ac.cr.flydevs.presentation.profile

import ucenfotec.ac.cr.flydevs.domain.model.User

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val user: User? = null,
    val editedName: String = "",
    val editedPhone: String = "",
    val editedLocation: String = "",
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val isSignedOut: Boolean = false,
)
