package ucenfotec.ac.cr.flydevs.presentation.purchaseHistory

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

class PurchaseHistoryViewModel(
    private val authRepository: IAuthRepository,
    private val orderRepository: IOrderRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseHistoryUiState())
    val uiState: StateFlow<PurchaseHistoryUiState> = _uiState.asStateFlow()

    init { loadOrders() }

    fun loadOrders() {
        val uid = authRepository.getCurrentUserUid() ?: return
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        orderRepository.getOrdersForUser(uid)
            .onEach { orders ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    orders = orders,
                )
            }
            .catch { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Error al cargar el historial",
                )
            }
            .launchIn(viewModelScope)
    }

    fun setFilter(filter: PurchaseHistoryFilter) {
        _uiState.value = _uiState.value.copy(activeFilter = filter)
    }

    fun setSortOption(sort: PurchaseSortOption) {
        _uiState.value = _uiState.value.copy(sortOption = sort)
    }
}
