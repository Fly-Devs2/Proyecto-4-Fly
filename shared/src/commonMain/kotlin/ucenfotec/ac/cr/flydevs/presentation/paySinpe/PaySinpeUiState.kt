package ucenfotec.ac.cr.flydevs.presentation.paySinpe

import kotlinx.serialization.Serializable
import ucenfotec.ac.cr.flydevs.domain.model.PickedImage

@Serializable
data class PaySinpeUiState(
    val isLoading: Boolean = false,
    val selectedImage: PickedImage? = null,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)
