package ucenfotec.ac.cr.flydevs.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IOrderRepository

class HomeViewModel(
    private val authRepository: IAuthRepository,
    private val orderRepository: IOrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
        observeOrders()
    }

    private fun observeOrders() {
        val uid = authRepository.getCurrentUserUid() 
        println("DEBUG_ORDERS: Current user UID is: $uid")
        if (uid == null) return
        
        orderRepository.getOrdersForUser(uid)
            .onEach { orders ->
                println("DEBUG_ORDERS: UI receiving ${orders.size} orders")
                _uiState.value = _uiState.value.copy(orders = orders)
            }
            .catch { error ->
                println("DEBUG_ORDERS: Flow error: ${error.message}")
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al cargar pedidos: ${error.message}"
                )
            }
            .launchIn(viewModelScope)
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                val uid = authRepository.getCurrentUserUid() ?: throw Exception("Usuario no autenticado")
                authRepository.getUserProfile(uid)
            }.onSuccess { user ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = user
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Error al cargar perfil"
                )
            }
        }
    }

    

    fun signOut() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            runCatching {
                authRepository.signOut()
            }.onSuccess {
                _uiState.value = HomeUiState(isSignedOut = true)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Error al cerrar sesión"
                )
            }
        }
    }
}
