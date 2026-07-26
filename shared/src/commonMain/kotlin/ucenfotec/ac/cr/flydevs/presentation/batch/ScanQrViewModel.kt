package ucenfotec.ac.cr.flydevs.presentation.batch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IBatchRepository

class ScanQrViewModel(
    private val batchRepository: IBatchRepository,
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanQrUiState())
    val uiState: StateFlow<ScanQrUiState> = _uiState.asStateFlow()

    fun onQrScanned(payload: String) {
        // Expected payload format: flydevs://batch-qr?bid=BATCH_ID_OR_LABEL
        val labelOrId = extractBatchId(payload)
        
        if (labelOrId == null) {
            _uiState.update { it.copy(error = "Código QR no válido para un lote.") }
            return
        }

        resolveAndAcceptBatch(labelOrId)
    }

    private fun resolveAndAcceptBatch(labelOrId: String) {
        val courierId = authRepository.getCurrentUserUid()
        if (courierId == null) {
            _uiState.update { it.copy(error = "Debes iniciar sesión para aceptar un lote.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            runCatching {
                // First try to resolve by label (e.g., L-2212)
                val resolvedId = batchRepository.findBatchIdByLabel(labelOrId) ?: labelOrId
                
                // NEW: Check if batch is already assigned to current user
                val batch = batchRepository.getBatchById(resolvedId)
                if (batch != null && batch.courierId == courierId) {
                    return@runCatching resolvedId
                }
                
                val user = authRepository.getUserProfile(courierId)
                val courierName = user?.name ?: "Mensajero"
                
                batchRepository.acceptBatch(
                    batchId = resolvedId,
                    courierId = courierId,
                    courierName = courierName
                )
                resolvedId
            }.onSuccess { resolvedId ->
                _uiState.update { it.copy(isLoading = false, successBatchId = resolvedId) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error al aceptar el lote.") }
            }
        }
    }

    private fun extractBatchId(payload: String): String? {
        // Simple extraction for now, can be improved with URI parsing if needed
        return if (payload.contains("bid=")) {
            payload.substringAfter("bid=").substringBefore("&")
        } else {
            // Fallback if the payload is just the batch ID
            if (payload.length > 5 && !payload.contains("://")) payload else null
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearSuccess() {
        _uiState.update { it.copy(successBatchId = null) }
    }
}
