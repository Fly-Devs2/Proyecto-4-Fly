package ucenfotec.ac.cr.flydevs.presentation.home

import kotlinx.serialization.Serializable
import ucenfotec.ac.cr.flydevs.domain.model.User

@Serializable
data class HomeUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val isSignedOut: Boolean = false,
    val errorMessage: String? = null
)
