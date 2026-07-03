package ucenfotec.ac.cr.flydevs.presentation.deliverStore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.model.PickedImage
import ucenfotec.ac.cr.flydevs.domain.repository.IOrderRepository

class DeliverStoreViewModel(
    private val orderRepository: IOrderRepository,
    private val orderId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeliverStoreUiState())
    val uiState: StateFlow<DeliverStoreUiState> = _uiState.asStateFlow()

    fun onImagePicked(image: PickedImage) {
        _uiState.value = _uiState.value.copy(selectedImage = image)
    }

    fun onNoteChange(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    fun confirmDelivery() {
        val image = _uiState.value.selectedImage ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                orderRepository.uploadSellerEvidence(orderId, image, _uiState.value.note)
            }.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Error al confirmar entrega"
                )
            }
        }
    }
}
