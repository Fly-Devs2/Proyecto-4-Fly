package ucenfotec.ac.cr.flydevs.presentation.exchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.model.PickedImage
import ucenfotec.ac.cr.flydevs.domain.repository.IImageStorageRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IOrderRepository
import ucenfotec.ac.cr.flydevs.presentation.publishGameCard.ImageError

/**
 * Comprador: subir el comprobante de SINPE Móvil.
 * Al confirmar, el sobre pasa a WAITING_STORE_SHIPMENT.
 */
class ExchangeBuyerViewModel(
    private val orderRepository: IOrderRepository,
    private val imageStorage: IImageStorageRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExchangeBuyerUiState())
    val uiState: StateFlow<ExchangeBuyerUiState> = _uiState.asStateFlow()

    fun load(exchangeId: String) {
        _uiState.update { it.copy(isLoading = true, feedback = null) }
        
        orderRepository.getOrder(exchangeId)
            .onEach { order ->
                _uiState.update { it.copy(isLoading = false, order = order) }
            }
            .launchIn(viewModelScope)
    }

    fun onConfirmChange(checked: Boolean) =
        _uiState.update { it.copy(confirmChecked = checked, feedback = null) }

    fun onImagePicked(image: PickedImage) {
        if (_uiState.value.isUploadingImage) return

        _uiState.update {
            it.copy(isUploadingImage = true, imageError = null, proofUrl = null, feedback = null)
        }

        viewModelScope.launch {
            runCatching { imageStorage.uploadEvidenceImage(image) }
                .onSuccess { url ->
                    _uiState.update { it.copy(isUploadingImage = false, proofUrl = url) }
                }
                .onFailure { error ->
                    println("[ExchangeBuyer] Falló la subida del comprobante: ${error.message}")
                    _uiState.update {
                        it.copy(isUploadingImage = false, imageError = ImageError.UPLOAD_FAILED)
                    }
                }
        }
    }

    fun submitProof() {
        val current = _uiState.value
        if (current.isSubmitting || current.isUploadingImage) return

        // no se puede subir el comprobante si el vendedor no subió su evidencia.
        if (!current.isReadyForSinpe) {
            _uiState.update { it.copy(feedback = SinpeProofFeedback.NOT_READY) }
            return
        }

        val orderId = current.order?.id
        val proofUrl = current.proofUrl
        if (orderId == null || proofUrl == null || !current.confirmChecked) {
            _uiState.update {
                it.copy(
                    imageError = if (proofUrl == null) ImageError.REQUIRED else null,
                    feedback = SinpeProofFeedback.MISSING_PROOF,
                )
            }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, feedback = null) }
        viewModelScope.launch {
            runCatching { orderRepository.submitSinpeProof(orderId, proofUrl) }
                .onSuccess { updated ->
                    _uiState.update {
                        it.copy(isSubmitting = false, order = updated, feedback = SinpeProofFeedback.SUCCESS)
                    }
                }
                .onFailure { error ->
                    println("[ExchangeBuyer] Falló el envío del comprobante: ${error.message}")
                    _uiState.update {
                        it.copy(isSubmitting = false, feedback = SinpeProofFeedback.SUBMIT_FAILED)
                    }
                }
        }
    }
}
