package ucenfotec.ac.cr.flydevs.presentation.orderDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IOrderRepository

class OrderDetailViewModel(
    private val orderRepository: IOrderRepository,
    private val authRepository: IAuthRepository,
    private val orderId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    init {
        loadOrder()
    }

    private fun loadOrder() {
        orderRepository.getOrder(orderId)
            .onEach { order ->
                if (order != null) {
                    val currentUid = authRepository.getCurrentUserUid()
                    val role = when {
                        order.buyerId == currentUid -> UserRole.BUYER
                        order.sellerId == currentUid -> UserRole.SELLER
                        else -> UserRole.UNKNOWN
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        order = order,
                        userRole = role
                    )
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
}
