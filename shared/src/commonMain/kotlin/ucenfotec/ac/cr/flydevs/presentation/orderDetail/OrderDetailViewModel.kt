package ucenfotec.ac.cr.flydevs.presentation.orderDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.model.OrderStatus
import ucenfotec.ac.cr.flydevs.domain.model.PickedImage
import ucenfotec.ac.cr.flydevs.domain.model.UserRole
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IImageStorageRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IOrderRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IStoreRepository

class OrderDetailViewModel(
    private val orderRepository: IOrderRepository,
    private val authRepository: IAuthRepository,
    private val imageStorage: IImageStorageRepository,
    private val storeRepository: IStoreRepository,
    private val orderId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    init {
        loadOrder()
    }

    fun onImagePicked(image: PickedImage) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            runCatching {
                val url = imageStorage.uploadEvidenceImage(image)
                val currentOrder = _uiState.value.order
                
                if (_uiState.value.userRole == UserRole.SELLER) {
                    orderRepository.submitSellerEvidence(orderId, url)
                } else {
                    orderRepository.addBuyerEvidence(orderId, url)
                    
                    // Si el comprador sube evidencia y ya estaba entregado en tienda, lo cerramos
                    if (currentOrder?.status == OrderStatus.DELIVERED_TO_STORE) {
                        orderRepository.updateOrderStatus(orderId, OrderStatus.PICKED_UP)
                    }
                }
            }.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al subir evidencia: ${error.message}"
                )
            }
        }
    }

    fun markAsShipped() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            runCatching { orderRepository.markAsShipped(orderId) }
                .onSuccess { _uiState.value = _uiState.value.copy(isLoading = false) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
        }
    }

    fun markAsDeliveredToStore() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            runCatching { orderRepository.markAsDeliveredToStore(orderId) }
                .onSuccess { _uiState.value = _uiState.value.copy(isLoading = false) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
        }
    }

    private fun loadOrder() {
        orderRepository.getOrder(orderId)
            .onEach { order ->
                if (order != null) {
                    val currentUid = authRepository.getCurrentUserUid()
                    val role = when {
                        order.buyerId == currentUid -> UserRole.BUYER
                        order.sellerId == currentUid -> UserRole.SELLER
                        else -> UserRole.USER
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        order = order,
                        userRole = role
                    )
                    loadStoreNames(order)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Orden no encontrada"
                    )
                }
            }
            .catch { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error de permisos o conexión: ${error.message}"
                )
            }
            .launchIn(viewModelScope)
    }

    private fun loadStoreNames(order: ucenfotec.ac.cr.flydevs.domain.model.Order) {
        viewModelScope.launch {
            try {
                val stores = storeRepository.getStores()
                val sourceStore = stores.find { it.id == order.sourceStore }
                val destStore = stores.find { it.id == order.destinationStore }
                
                _uiState.value = _uiState.value.copy(
                    sourceStoreName = sourceStore?.name ?: "Tienda desconocida",
                    destinationStoreName = destStore?.name ?: "Tienda desconocida"
                )
            } catch (e: Exception) {
                println("ERROR_ORDER_DETAIL: Error loading store names: ${e.message}")
            }
        }
    }
}
