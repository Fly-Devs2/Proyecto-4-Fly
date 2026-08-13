package ucenfotec.ac.cr.flydevs.presentation.batch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.model.BatchEvidence
import ucenfotec.ac.cr.flydevs.domain.model.BatchStatus
import ucenfotec.ac.cr.flydevs.domain.model.PickedImage
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IBatchRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IImageStorageRepository

class BatchEvidenceViewModel(
    private val batchRepository: IBatchRepository,
    private val imageStorage: IImageStorageRepository,
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BatchEvidenceUiState())
    val uiState: StateFlow<BatchEvidenceUiState> = _uiState.asStateFlow()

    fun loadBatch(batchId: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val batch = batchRepository.getBatchById(batchId)
            if (batch != null) {
                _uiState.update { it.copy(isLoading = false, batch = batch) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "No se encontró el lote.") }
            }
        }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun onConfirmChange(checked: Boolean) {
        _uiState.update { it.copy(confirmChecked = checked) }
    }

    fun onImagePicked(image: PickedImage) {
        if (_uiState.value.isUploadingImage) return

        _uiState.update { it.copy(isUploadingImage = true, error = null) }

        viewModelScope.launch {
            runCatching { imageStorage.uploadEvidenceImage(image) }
                .onSuccess { url ->
                    _uiState.update { it.copy(isUploadingImage = false, proofUrl = url) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isUploadingImage = false, error = "Error al subir la imagen.") }
                }
        }
    }

    fun submitEvidence() {
        val current = _uiState.value
        val batch = current.batch ?: return
        val courierId = authRepository.getCurrentUserUid() ?: return

        if (current.proofUrl == null) {
            _uiState.update { it.copy(feedback = BatchEvidenceFeedback.MISSING_PHOTO) }
            return
        }

        if (!current.confirmChecked) {
            _uiState.update { it.copy(feedback = BatchEvidenceFeedback.MISSING_CONFIRM) }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, feedback = null) }

        viewModelScope.launch {
            runCatching {
                val evidence = BatchEvidence(
                    downloadUrl = current.proofUrl,
                    storagePath = "batches/${batch.id}/${if (batch.status == BatchStatus.ACCEPTED) "pickup" else "delivery"}",
                    note = current.note
                )

                if (batch.status == BatchStatus.IN_TRANSIT) {
                    batchRepository.confirmDelivery(batch.id, courierId, evidence)

                } else {
                    batchRepository.confirmPickup(batch.id, courierId, evidence)
                }
            }.onSuccess {
                _uiState.update { it.copy(isSubmitting = false, feedback = BatchEvidenceFeedback.SUCCESS) }
            }.onFailure { e ->
                _uiState.update { it.copy(isSubmitting = false, feedback = BatchEvidenceFeedback.SUBMIT_FAILED, error = e.message) }
            }
        }
    }
    
    fun clearFeedback() {
        _uiState.update { it.copy(feedback = null) }
    }
}
