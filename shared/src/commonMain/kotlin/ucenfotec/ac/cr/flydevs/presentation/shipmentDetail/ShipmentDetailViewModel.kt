package ucenfotec.ac.cr.flydevs.presentation.shipmentDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.repository.IBatchRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IOrderRepository

class ShipmentDetailViewModel(
    private val batchRepository: IBatchRepository,
    private val orderRepository: IOrderRepository,
    private val batchId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShipmentDetailUiState())
    val uiState: StateFlow<ShipmentDetailUiState> = _uiState.asStateFlow()

    private var ordersJob: Job? = null

    init {
        loadBatch()
    }

    fun nextOrdersPage() {
        val state = _uiState.value
        if (state.ordersPage + 1 < state.totalOrderPages) goToOrdersPage(state.ordersPage + 1)
    }

    fun previousOrdersPage() {
        val state = _uiState.value
        if (state.ordersPage > 0) goToOrdersPage(state.ordersPage - 1)
    }

    private fun goToOrdersPage(page: Int) {
        _uiState.value = _uiState.value.copy(ordersPage = page)
        loadOrdersPage(page)
    }

    // Una entrega ya realizada no cambia, así que basta con una lectura puntual.
    private fun loadBatch() {
        viewModelScope.launch {
            runCatching { batchRepository.getBatchById(batchId) }
                .onSuccess { batch ->
                    if (batch == null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Lote no encontrado",
                        )
                        return@onSuccess
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        batch = batch,
                        errorMessage = null,
                    )
                    loadOrdersPage(_uiState.value.ordersPage)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error de permisos o conexión: ${error.message}",
                    )
                }
        }
    }

    private fun loadOrdersPage(page: Int) {
        val ids = _uiState.value.orderIdsForPage(page)
        if (ids.isEmpty()) {
            _uiState.value = _uiState.value.copy(visibleOrders = emptyList(), isLoadingOrders = false)
            return
        }

        // Cancelamos la página anterior para que una respuesta tardía no pise a la actual.
        ordersJob?.cancel()
        ordersJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingOrders = true)
            runCatching { orderRepository.getOrdersByIds(ids) }
                .onSuccess { orders ->
                    _uiState.value = _uiState.value.copy(visibleOrders = orders, isLoadingOrders = false)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        visibleOrders = emptyList(),
                        isLoadingOrders = false,
                        errorMessage = "No se pudieron cargar los sobres: ${it.message}",
                    )
                }
        }
    }
}
