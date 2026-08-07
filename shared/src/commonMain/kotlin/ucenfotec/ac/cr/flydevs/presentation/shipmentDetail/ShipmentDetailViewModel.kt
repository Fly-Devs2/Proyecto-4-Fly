package ucenfotec.ac.cr.flydevs.presentation.shipmentDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IBatchRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IOrderRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IShipmentLocationRepository

class ShipmentDetailViewModel(
    private val batchRepository: IBatchRepository,
    private val orderRepository: IOrderRepository,
    private val authRepository: IAuthRepository,
    private val shipmentLocationRepository: IShipmentLocationRepository,
    private val batchId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShipmentDetailUiState())
    val uiState: StateFlow<ShipmentDetailUiState> = _uiState.asStateFlow()

    private var batchJob: Job? = null
    private var ordersJob: Job? = null

    init {
        observeBatch()
    }

    fun startRoute(
        onTrackingReady: (
            batchDocumentId: String,
            courierId: String
        ) -> Unit
    ) {
        val courierId =
            authRepository.getCurrentUserUid()
                ?: return

        val batch =
            _uiState.value.batch
                ?: return

        _uiState.value =
            _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

        viewModelScope.launch {

            runCatching {

                val orders =
                    orderRepository.getOrdersByIds(
                        batch.orderIds
                    )

                val buyerIds =
                    orders
                        .map { order ->
                            order.buyerId
                        }
                        .filter {
                            it.isNotBlank()
                        }
                        .distinct()

                /*
                 * PICKED_UP -> IN_TRANSIT
                 */
                batchRepository.startDeliveryRoute(
                    batchId = batch.id,
                    courierId = courierId
                )

                /*
                 * Creamos shipment_locations/{batch.id}
                 * antes de iniciar el servicio GPS.
                 */
                shipmentLocationRepository
                    .initializeTracking(
                        batchDocumentId = batch.id,
                        courierId = courierId,
                        buyerIds = buyerIds
                    )

            }.onSuccess {

                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null
                    )

                /*
                 * Ahora sí Android puede solicitar
                 * permiso e iniciar el GPS.
                 */
                onTrackingReady(
                    batch.id,
                    courierId
                )

            }.onFailure { error ->

                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        errorMessage =
                            "Error al iniciar ruta: ${error.message}"
                    )
            }
        }
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

    private fun observeBatch() {
        batchJob?.cancel()
        batchJob = viewModelScope.launch {
            batchRepository.observeBatchById(batchId)
                .collect { batch ->
                    if (batch == null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Lote no encontrado",
                        )
                        return@collect
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        batch = batch,
                        errorMessage = null,
                    )
                    loadOrdersPage(_uiState.value.ordersPage)
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

    override fun onCleared() {
        batchJob?.cancel()
        ordersJob?.cancel()
        super.onCleared()
    }
}
