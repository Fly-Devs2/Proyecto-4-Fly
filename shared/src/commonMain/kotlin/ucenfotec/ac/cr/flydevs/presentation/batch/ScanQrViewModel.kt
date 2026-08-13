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
        // Expected payload format: 
        // flydevs://batch-qr?bid=BATCH_ID_OR_LABEL
        // flydevs://store-qr?sid=STORE_ID
        
        when {
            payload.contains("sid=") -> {
                val storeId = payload.substringAfter("sid=").substringBefore("&")
                acceptStoreBatches(storeId)
            }
            payload.contains("bid=") -> {
                val labelOrId = payload.substringAfter("bid=").substringBefore("&")
                resolveAndAcceptBatch(labelOrId)
            }
            else -> {
                // Fallback for raw batch ID
                if (payload.length > 5 && !payload.contains("://")) {
                    resolveAndAcceptBatch(payload)
                } else {
                    _uiState.update { it.copy(error = "Código QR no válido.") }
                }
            }
        }
    }

    private fun acceptStoreBatches(storeId: String) {
        val courierId = authRepository.getCurrentUserUid()
        if (courierId == null) {
            _uiState.update { it.copy(error = "Debes iniciar sesión para aceptar lotes.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            runCatching {
                val user = authRepository.getUserProfile(courierId)
                val courierName = user?.name ?: "Mensajero"

                val acceptedCount = batchRepository.acceptAllStoreBatches(
                    storeId = storeId,
                    courierId = courierId,
                    courierName = courierName
                )
                
                if (acceptedCount == 0) {
                    throw Exception("No hay lotes disponibles para esta tienda.")
                }
                
                acceptedCount
            }.onSuccess { count ->
                _uiState.update { it.copy(isLoading = false, successBatchId = "STORE_SUCCESS:$count") }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error al aceptar los lotes.") }
            }
        }
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
        // Obsoleto, ya se maneja en onQrScanned
        return null
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearSuccess() {
        _uiState.update { it.copy(successBatchId = null) }
    }
}
