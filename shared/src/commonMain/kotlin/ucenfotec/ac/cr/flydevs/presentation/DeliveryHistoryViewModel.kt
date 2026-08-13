package ucenfotec.ac.cr.flydevs.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ucenfotec.ac.cr.flydevs.domain.model.BatchStatus
import ucenfotec.ac.cr.flydevs.domain.model.DeliveryBatch
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IBatchRepository

enum class DeliveryHistoryFilter {
    ALL,
    DELIVERED,
    CANCELLED
}

data class DeliveryHistoryUiState(
    val isLoading: Boolean = true,
    val batches: List<DeliveryBatch> = emptyList(),
    val selectedFilter: DeliveryHistoryFilter = DeliveryHistoryFilter.ALL,
    val errorMessage: String? = null
) {
    val filteredBatches: List<DeliveryBatch>
        get() = when (selectedFilter) {
            DeliveryHistoryFilter.ALL -> batches

            DeliveryHistoryFilter.DELIVERED ->
                batches.filter { it.status == BatchStatus.DELIVERED }

            DeliveryHistoryFilter.CANCELLED ->
                batches.filter { it.status == BatchStatus.CANCELLED }
        }
}

class DeliveryHistoryViewModel(
    private val batchRepository: IBatchRepository,
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeliveryHistoryUiState())
    val uiState: StateFlow<DeliveryHistoryUiState> = _uiState.asStateFlow()

    init {
        observeHistory()
    }

    private fun observeHistory() {
        val courierId = authRepository.getCurrentUserUid()

        if (courierId == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "No se pudo identificar al mensajero."
            )
            return
        }

        batchRepository
            .observeCourierBatches(courierId)
            .onEach { batches ->

                val historyBatches = batches.filter {
                    it.status == BatchStatus.DELIVERED ||
                            it.status == BatchStatus.CANCELLED
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    batches = historyBatches,
                    errorMessage = null
                )
            }
            .catch { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                        ?: "Error al cargar el historial."
                )
            }
            .launchIn(viewModelScope)
    }

    fun selectFilter(filter: DeliveryHistoryFilter) {
        _uiState.value = _uiState.value.copy(
            selectedFilter = filter
        )
    }
}