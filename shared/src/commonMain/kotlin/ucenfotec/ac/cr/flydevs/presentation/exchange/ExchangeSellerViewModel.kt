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
 * Al confirmar, el sobre pasa a ESPERANDO_COMPROBANTE_SINPE.
 */
class ExchangeSellerViewModel(
    private val exchangeRepository: IExchangeRepository,
    private val imageStorage: IImageStorageRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExchangeSellerUiState())
    val uiState: StateFlow<ExchangeSellerUiState> = _uiState.asStateFlow()

    fun load(exchangeId: String) {
        _uiState.update { it.copy(isLoading = true, feedback = null) }
        viewModelScope.launch {
            runCatching { exchangeRepository.getExchange(exchangeId) }
                .onSuccess { exchange ->
                    _uiState.update { it.copy(isLoading = false, exchange = exchange) }
                }
                .onFailure { error ->
                    println("[ExchangeSeller] Falló la carga del sobre: ${error.message}")
                    _uiState.update {
                        it.copy(isLoading = false, feedback = SellerEvidenceFeedback.SUBMIT_FAILED)
                    }
                }
        }
    }

    fun onInfoVisibleChange(checked: Boolean) =
        _uiState.update { it.copy(infoVisibleChecked = checked, feedback = null) }

    fun onImagePicked(image: PickedImage) {
        if (_uiState.value.isUploadingImage) return

        _uiState.update {
            it.copy(isUploadingImage = true, imageError = null, evidenceUrl = null, feedback = null)
        }

        viewModelScope.launch {
            runCatching { imageStorage.uploadCardImage(image) }
                .onSuccess { url ->
                    _uiState.update { it.copy(isUploadingImage = false, evidenceUrl = url) }
                }
                .onFailure { error ->
                    println("[ExchangeSeller] Falló la subida de evidencia: ${error.message}")
                    _uiState.update {
                        it.copy(isUploadingImage = false, imageError = ImageError.UPLOAD_FAILED)
                    }
                }
        }
    }

    fun confirmDelivery() {
        val current = _uiState.value
        if (current.isSubmitting || current.isUploadingImage) return

        val exchangeId = current.exchange?.id
        val evidenceUrl = current.evidenceUrl
        if (exchangeId == null || evidenceUrl == null || !current.infoVisibleChecked) {
            _uiState.update {
                it.copy(
                    imageError = if (evidenceUrl == null) ImageError.REQUIRED else null,
                    feedback = SellerEvidenceFeedback.MISSING_EVIDENCE,
                )
            }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, feedback = null) }
        viewModelScope.launch {
            runCatching { exchangeRepository.submitSellerEvidence(exchangeId, evidenceUrl) }
                .onSuccess { updated ->
                    _uiState.update {
                        it.copy(isSubmitting = false, exchange = updated, feedback = SellerEvidenceFeedback.SUCCESS)
                    }
                }
                .onFailure { error ->
                    println("[ExchangeSeller] Falló el envío de evidencia: ${error.message}")
                    _uiState.update {
                        it.copy(isSubmitting = false, feedback = SellerEvidenceFeedback.SUBMIT_FAILED)
                    }
                }
        }
    }
}
