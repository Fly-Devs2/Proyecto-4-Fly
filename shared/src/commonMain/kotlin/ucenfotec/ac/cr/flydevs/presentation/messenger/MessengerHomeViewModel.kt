package ucenfotec.ac.cr.flydevs.presentation.messenger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.model.BatchStatus
import ucenfotec.ac.cr.flydevs.domain.model.DeliveryBatch
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IBatchRepository

class MessengerHomeViewModel(
    private val batchRepository: IBatchRepository,
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _uiState =
        kotlinx.coroutines.flow.MutableStateFlow(MessengerHomeUiState())

    val uiState =
        _uiState.asStateFlow()

    private var courierBatchesJob: Job? = null
    private var availableBatchesJob: Job? = null

    init {
        loadMessengerData()
    }

    /**
     * Obtiene el mensajero autenticado y comienza a observar Firestore.
     */
    fun loadMessengerData() {
        val courierId = authRepository.getCurrentUserUid()

        if (courierId.isNullOrBlank()) {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    errorMessage = "No se encontró un usuario autenticado."
                )
            }
            return
        }

        _uiState.update { currentState ->
            currentState.copy(
                courierId = courierId,
                isLoading = true,
                errorMessage = null
            )
        }

        loadCourierProfile(courierId)
        observeCourierBatches(courierId)
        observeAvailableBatches()
    }

    /**
     * Obtiene nombre y datos del mensajero desde users.
     */
    private fun loadCourierProfile(
        courierId: String
    ) {
        viewModelScope.launch {
            runCatching {
                authRepository.getUserProfile(courierId)
            }.onSuccess { user ->
                _uiState.update { currentState ->
                    currentState.copy(
                        courierName = user?.name.orEmpty()
                    )
                }
            }.onFailure { exception ->
                _uiState.update { currentState ->
                    currentState.copy(
                        errorMessage = exception.message
                            ?: "No se pudo cargar el perfil del mensajero."
                    )
                }
            }
        }
    }

    /**
     * Escucha todos los lotes asignados al mensajero.
     */
    private fun observeCourierBatches(
        courierId: String
    ) {
        courierBatchesJob?.cancel()

        courierBatchesJob = viewModelScope.launch {
            batchRepository
                .observeCourierBatches(courierId)
                .onStart {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }
                }
                .catch { exception ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            errorMessage = exception.message
                                ?: "No se pudieron cargar los lotes asignados."
                        )
                    }
                }
                .collect { batches ->
                    updateCourierBatches(batches)
                }
        }
    }

    /**
     * Escucha lotes creados que todavía no tienen mensajero.
     */
    private fun observeAvailableBatches() {
        availableBatchesJob?.cancel()

        availableBatchesJob = viewModelScope.launch {
            batchRepository
                .observeAvailableBatches()
                .catch { exception ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            errorMessage = exception.message
                                ?: "No se pudieron cargar los lotes disponibles."
                        )
                    }
                }
                .collect { batches ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            availableBatches = batches
                        )
                    }
                }
        }
    }

    /**
     * Organiza los lotes del mensajero entre:
     *
     * - Lote activo.
     * - Próximas asignaciones.
     * - Lotes completados.
     */
    private fun updateCourierBatches(
        batches: List<DeliveryBatch>
    ) {
        val unfinishedBatches = batches.filter { batch ->
            batch.status != BatchStatus.DELIVERED &&
                    batch.status != BatchStatus.CANCELLED
        }

        val activeBatch = unfinishedBatches
            .sortedWith(
                compareBy<DeliveryBatch> {
                    statusPriority(it.status)
                }.thenBy {
                    it.acceptedAt ?: it.createdAt
                }
            )
            .firstOrNull()

        val upcomingBatches = unfinishedBatches
            .filter { batch ->
                batch.id != activeBatch?.id
            }
            .sortedBy { batch ->
                batch.acceptedAt ?: batch.createdAt
            }

        val deliveredBatches = batches.filter { batch ->
            batch.status == BatchStatus.DELIVERED
        }

        val totalEarnings = deliveredBatches.sumOf { batch ->
            batch.courierReward
        }

        _uiState.update { currentState ->
            currentState.copy(
                isLoading = false,
                activeBatch = activeBatch,
                upcomingBatches = upcomingBatches,

                // Por ahora representan los totales del mensajero.
                // Posteriormente, filtraremos por fecha para mostrar solo hoy.
                completedDeliveriesToday = deliveredBatches.size,
                assignedDeliveriesToday = batches.size,
                todayEarnings = totalEarnings
            )
        }
    }

    /**
     * Define cuál lote debe mostrarse primero como lote activo.
     *
     * IN_TRANSIT tiene mayor prioridad porque ya está viajando.
     */
    private fun statusPriority(
        status: BatchStatus
    ): Int {
        return when (status) {
            BatchStatus.IN_TRANSIT -> 0
            BatchStatus.PICKED_UP -> 1
            BatchStatus.ACCEPTED -> 2
            BatchStatus.READY_FOR_PICKUP -> 3
            BatchStatus.DELIVERED -> 4
            BatchStatus.CANCELLED -> 5
        }
    }

    /**
     * Procesa eventos enviados por la pantalla.
     */
    fun onEvent(
        event: MessengerHomeEvent
    ) {
        when (event) {
            MessengerHomeEvent.Refresh -> {
                loadMessengerData()
            }

            is MessengerHomeEvent.AcceptBatch -> {
                acceptBatch(event.batchId)
            }

            is MessengerHomeEvent.StartRoute -> {
                startRoute(event.batchId)
            }

            /*
             * Estos dos eventos se conectarán cuando implementemos
             * CameraX y Firebase Storage.
             */
            is MessengerHomeEvent.ConfirmPickup -> {
                _uiState.update { currentState ->
                    currentState.copy(
                        errorMessage =
                            "Primero debe capturarse la evidencia de recogida."
                    )
                }
            }

            is MessengerHomeEvent.ConfirmDelivery -> {
                _uiState.update { currentState ->
                    currentState.copy(
                        errorMessage =
                            "Primero debe capturarse la evidencia de entrega."
                    )
                }
            }

            MessengerHomeEvent.ClearMessage -> {
                clearMessage()
            }
        }
    }

    /**
     * Asigna un lote disponible al mensajero autenticado.
     */
    private fun acceptBatch(
        batchId: String
    ) {
        val courierId = _uiState.value.courierId
        val courierName = _uiState.value.courierName

        if (courierId.isBlank()) {
            showError("No se encontró el mensajero autenticado.")
            return
        }

        if (courierName.isBlank()) {
            showError("No se pudo obtener el nombre del mensajero.")
            return
        }

        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isAcceptingBatch = true,
                    successMessage = null,
                    errorMessage = null
                )
            }

            runCatching {
                batchRepository.acceptBatch(
                    batchId = batchId,
                    courierId = courierId,
                    courierName = courierName
                )
            }.onSuccess {
                _uiState.update { currentState ->
                    currentState.copy(
                        isAcceptingBatch = false,
                        successMessage = "Lote aceptado correctamente."
                    )
                }
            }.onFailure { exception ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isAcceptingBatch = false,
                        errorMessage = exception.message
                            ?: "No se pudo aceptar el lote."
                    )
                }
            }
        }
    }

    /**
     * Cambia el lote desde PICKED_UP hasta IN_TRANSIT.
     */
    private fun startRoute(
        batchId: String
    ) {
        val courierId = _uiState.value.courierId

        if (courierId.isBlank()) {
            showError("No se encontró el mensajero autenticado.")
            return
        }

        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isUpdatingBatch = true,
                    successMessage = null,
                    errorMessage = null
                )
            }

            runCatching {
                batchRepository.startDeliveryRoute(
                    batchId = batchId,
                    courierId = courierId
                )
            }.onSuccess {
                _uiState.update { currentState ->
                    currentState.copy(
                        isUpdatingBatch = false,
                        successMessage = "La ruta fue iniciada correctamente."
                    )
                }
            }.onFailure { exception ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isUpdatingBatch = false,
                        errorMessage = exception.message
                            ?: "No se pudo iniciar la ruta."
                    )
                }
            }
        }
    }

    private fun showError(
        message: String
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                errorMessage = message,
                successMessage = null
            )
        }
    }

    private fun clearMessage() {
        _uiState.update { currentState ->
            currentState.copy(
                successMessage = null,
                errorMessage = null
            )
        }
    }

    override fun onCleared() {
        courierBatchesJob?.cancel()
        availableBatchesJob?.cancel()
        super.onCleared()
    }
}