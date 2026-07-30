package ucenfotec.ac.cr.flydevs.presentation.incident

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.model.Incident
import ucenfotec.ac.cr.flydevs.domain.model.IncidentStatus
import ucenfotec.ac.cr.flydevs.domain.model.Order
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IIncidentRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IOrderRepository

class ReportIncidentViewModel(
    private val authRepository: IAuthRepository,
    private val orderRepository: IOrderRepository,
    private val incidentRepository: IIncidentRepository,
    private val orderId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportIncidentUiState())
    val uiState: StateFlow<ReportIncidentUiState> = _uiState.asStateFlow()

    private var order: Order? = null

    init {
        loadOrder()
        observeExistingIncidents()
    }

    private fun loadOrder() {
        viewModelScope.launch {
            runCatching { orderRepository.getOrder(orderId).first() }
                .onSuccess { loaded ->
                    if (loaded == null) {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = "No se encontró la orden.")
                        }
                        return@onSuccess
                    }

                    order = loaded
                    val uid = authRepository.getCurrentUserUid()
                    val isBuyer = loaded.buyerId == uid

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            orderCode = orderCode(loaded.id),
                            cardCount = loaded.cards.size,
                            counterpartLabel = if (isBuyer) {
                                "Vendedor: ${loaded.sellerName.ifBlank { "—" }}"
                            } else {
                                "Comprador: ${loaded.buyerName.ifBlank { "—" }}"
                            },
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Error al cargar la orden.")
                    }
                }
        }
    }

    private fun observeExistingIncidents() {
        val uid = authRepository.getCurrentUserUid() ?: return
        incidentRepository.observeMyIncidentsForOrder(orderId, uid)
            .onEach { incidents ->
                _uiState.update {
                    it.copy(
                        hasOpenIncident = incidents.any { incident ->
                            incident.status != IncidentStatus.RESOLVED
                        },
                    )
                }
            }
            .catch {}
            .launchIn(viewModelScope)
    }

    fun onDescriptionChange(value: String) {
        // El corte va acá para que la UI nunca muestre más del máximo permitido.
        val trimmed = value.take(_uiState.value.maxLength)
        _uiState.update { it.copy(description = trimmed, errorMessage = null) }
    }

    fun submit() {
        val current = _uiState.value
        if (!current.canSubmit) return

        val loadedOrder = order
        if (loadedOrder == null) {
            _uiState.update { it.copy(errorMessage = "No se encontró la orden.") }
            return
        }

        val uid = authRepository.getCurrentUserUid()
        if (uid == null) {
            _uiState.update { it.copy(errorMessage = "Debes iniciar sesión para reportar una incidencia.") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            runCatching {
                val isBuyer = loadedOrder.buyerId == uid
                val reporterName = if (isBuyer) loadedOrder.buyerName else loadedOrder.sellerName

                incidentRepository.reportIncident(
                    Incident(
                        orderId = loadedOrder.id,
                        orderCode = current.orderCode,
                        reporterId = uid,
                        reporterName = reporterName.ifBlank { "Usuario" },
                        counterpartId = if (isBuyer) loadedOrder.sellerId else loadedOrder.buyerId,
                        counterpartName = if (isBuyer) loadedOrder.sellerName else loadedOrder.buyerName,
                        description = current.description,
                        orderStatusAtReport = loadedOrder.status.name,
                    )
                )
            }.onSuccess {
                _uiState.update { it.copy(isSubmitting = false, isSubmitted = true) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = error.message ?: "No se pudo enviar la incidencia.",
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

/** Mismo formato corto que usan las listas de pedidos (`FA-1042` → primeros 7 caracteres). */
private fun orderCode(orderId: String): String =
    if (orderId.length > 7) orderId.take(7).uppercase() else orderId.uppercase()
