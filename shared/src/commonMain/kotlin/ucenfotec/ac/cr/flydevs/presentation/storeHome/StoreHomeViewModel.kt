package ucenfotec.ac.cr.flydevs.presentation.storeHome

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
import ucenfotec.ac.cr.flydevs.domain.model.BatchStatus
import ucenfotec.ac.cr.flydevs.domain.model.Order
import ucenfotec.ac.cr.flydevs.domain.model.OrderStatus
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IBatchRepository
import ucenfotec.ac.cr.flydevs.domain.repository.INotificationRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IOrderRepository
import ucenfotec.ac.cr.flydevs.getEpochMillis
import ucenfotec.ac.cr.flydevs.presentation.util.daysBetween
import ucenfotec.ac.cr.flydevs.presentation.util.isSameDay

class StoreHomeViewModel(
    private val authRepository: IAuthRepository,
    private val batchRepository: IBatchRepository,
    private val orderRepository: IOrderRepository,
    private val notificationRepository: INotificationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreHomeUiState())
    val uiState: StateFlow<StoreHomeUiState> = _uiState.asStateFlow()

    init {
        loadStore()
        observeNotifications()
    }

    fun loadStore() {
        val uid = authRepository.getCurrentUserUid()
        if (uid == null) {
            _uiState.update {
                it.copy(isLoading = false, errorMessage = "Inicia sesión para ver el panel de tu tienda.")
            }
            return
        }

        viewModelScope.launch {
            runCatching { authRepository.getUserProfile(uid) }
                .onSuccess { user ->
                    val storeId = user?.storeId
                    if (storeId.isNullOrBlank()) {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = "No tienes una tienda asignada.")
                        }
                        return@onSuccess
                    }

                    _uiState.update {
                        it.copy(
                            storeId = storeId,
                            storeName = user.storeName ?: user.name.ifBlank { "Mi tienda" },
                        )
                    }

                    observeIncomingBatches(storeId)
                    observeStoreOrders(storeId)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Error al cargar el perfil.")
                    }
                }
        }
    }

    private fun observeIncomingBatches(storeId: String) {
        batchRepository.observeIncomingStoreBatches(storeId)
            .onEach { batches ->
                val today = getEpochMillis()
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        incomingBatches = batches,
                        batchesReceivedToday = batches.count { batch ->
                            batch.status == BatchStatus.DELIVERED &&
                                isSameDay(batch.deliveredAt ?: 0L, today)
                        },
                        errorMessage = null,
                    )
                }
            }
            .catch { error ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "Error al cargar los lotes.")
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeStoreOrders(storeId: String) {
        orderRepository.observeStoreOrders(storeId)
            .onEach { orders ->
                val today = getEpochMillis()
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        pendingPickups = orders
                            .filter { it.status == OrderStatus.DELIVERED_TO_STORE }
                            .sortedByDescending { it.modifiedAt }
                            .map { it.toPickup(today) },
                        pickupsToday = orders.count { order ->
                            order.status == OrderStatus.PICKED_UP && isSameDay(order.modifiedAt, today)
                        },
                    )
                }
            }
            .catch { error ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "Error al cargar los retiros.")
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeNotifications() {
        val uid = authRepository.getCurrentUserUid() ?: return
        notificationRepository.getNotificationsForUser(uid)
            .onEach { notifications ->
                _uiState.update { it.copy(unreadNotifications = notifications.count { n -> !n.read }) }
            }
            .catch {}
            .launchIn(viewModelScope)
    }
}

private fun Order.toPickup(now: Long): StorePickup = StorePickup(
    orderId = id,
    buyerName = buyerName.ifBlank { "Comprador" },
    cardCount = cards.size,
    waitingDays = daysBetween(modifiedAt, now),
)
