package ucenfotec.ac.cr.flydevs.presentation.deliverStore

import kotlinx.serialization.Serializable
import ucenfotec.ac.cr.flydevs.domain.model.PickedImage

@Serializable
data class DeliverStoreUiState(
    val isLoading: Boolean = false,
    val selectedImage: PickedImage? = null,
    val note: String = "",
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)
