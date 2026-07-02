package ucenfotec.ac.cr.flydevs.presentation.exchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.model.PickedImage
import ucenfotec.ac.cr.flydevs.domain.repository.IExchangeRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IImageStorageRepository
import ucenfotec.ac.cr.flydevs.presentation.publishGameCard.ImageError

/**
 * Comprador: subir el comprobante de SINPE Móvil.
 * Al confirmar, el sobre pasa a COMPROBANTE_RECIBIDO.
 */
class ExchangeBuyerViewModel(
    private val exchangeRepository: IExchangeRepository,
    private val imageStorage: IImageStorageRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExchangeBuyerUiState())
    val uiState: StateFlow<ExchangeBuyerUiState> = _uiState.asStateFlow()

    fun load(exchangeId: String) {
        _uiState.update { it.copy(isLoading = true, feedback = null) }
        viewModelScope.launch {
            runCatching { exchangeRepository.getExchange(exchangeId) }
                .onSuccess { exchange ->
                    _uiState.update { it.copy(isLoading = false, exchange = exchange) }
                }
                .onFailure { error ->
                    println("[ExchangeBuyer] Falló la carga del sobre: ${error.message}")
                    _uiState.update {
                        it.copy(isLoading = false, feedback = SinpeProofFeedback.SUBMIT_FAILED)
                    }
                }
        }
    }

    fun onConfirmChange(checked: Boolean) =
        _uiState.update { it.copy(confirmChecked = checked, feedback = null) }

    fun onImagePicked(image: PickedImage) {
        if (_uiState.value.isUploadingImage) return

        _uiState.update {
            it.copy(isUploadingImage = true, imageError = null, proofUrl = null, feedback = null)
        }

        viewModelScope.launch {
            runCatching { imageStorage.uploadCardImage(image) }
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

        // no se puede subir el comprobante si el vendedor aún no subió su evidencia.
        if (!current.isReadyForSinpe) {
            _uiState.update { it.copy(feedback = SinpeProofFeedback.NOT_READY) }
            return
        }

        val exchangeId = current.exchange?.id
        val proofUrl = current.proofUrl
        if (exchangeId == null || proofUrl == null || !current.confirmChecked) {
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
            runCatching { exchangeRepository.submitSinpeProof(exchangeId, proofUrl) }
                .onSuccess { updated ->
                    _uiState.update {
                        it.copy(isSubmitting = false, exchange = updated, feedback = SinpeProofFeedback.SUCCESS)
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
