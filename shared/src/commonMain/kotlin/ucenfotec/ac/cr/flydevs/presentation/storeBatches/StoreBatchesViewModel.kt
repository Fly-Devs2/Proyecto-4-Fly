package ucenfotec.ac.cr.flydevs.presentation.storeBatches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IBatchRepository

class StoreBatchesViewModel(
    private val authRepository: IAuthRepository,
    private val batchRepository: IBatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreBatchesUiState())
    val uiState: StateFlow<StoreBatchesUiState> = _uiState.asStateFlow()

    init {
        loadStoreAndBatches()
    }

    private fun loadStoreAndBatches() {
        val uid = authRepository.getCurrentUserUid()
        if (uid == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Inicia sesión para ver los lotes de tu tienda.") }
            return
        }

        viewModelScope.launch {
            runCatching {
                val user = authRepository.getUserProfile(uid)
                val storeId = user?.storeId
                val storeName = user?.storeName

                if (storeId == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "No tienes una tienda asignada.") }
                    return@launch
                }

                _uiState.update { it.copy(storeId = storeId, storeName = storeName ?: "Mi Tienda") }
                observeBatches(storeId)
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Error al cargar el perfil.") }
            }
        }
    }

    private fun observeBatches(storeId: String) {
        batchRepository.observeOutgoingStoreBatches(storeId)
            .onEach { batches ->
                _uiState.update { it.copy(isLoading = false, batches = batches, errorMessage = null) }
            }
            .catch { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Error al cargar lotes.") }
            }
            .launchIn(viewModelScope)
    }
}
