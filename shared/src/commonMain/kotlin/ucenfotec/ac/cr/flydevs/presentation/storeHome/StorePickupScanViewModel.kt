package ucenfotec.ac.cr.flydevs.presentation.storeHome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IOrderRepository

data class StorePickupScanUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val pickedUpOrderId: String? = null,
    val buyerName: String = "",
    val cardCount: Int = 0,
)

/**
 * Valida el QR de retiro que presenta el comprador en la tienda.
 *
 * Mismo flujo que el escaneo de lote del mensajero, pero el enlace mueve la
 * orden a `PICKED_UP` en lugar de asignar un lote.
 */
class StorePickupScanViewModel(
    private val orderRepository: IOrderRepository,
    private val authRepository: IAuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StorePickupScanUiState())
    val uiState: StateFlow<StorePickupScanUiState> = _uiState.asStateFlow()

    /** Payload esperado: `flydevs://order-qr?oid=ORDER_ID&t=TOKEN&s=SIGNATURE`. */
    fun onQrScanned(payload: String) {
        if (_uiState.value.isLoading || _uiState.value.pickedUpOrderId != null) return

        val orderId = payload.paramOrNull("oid")
        if (orderId == null) {
            _uiState.update {
                it.copy(error = "Este código QR no corresponde a un retiro de cartas.")
            }
            return
        }

        confirmPickup(orderId, payload.paramOrNull("s"))
    }

    private fun confirmPickup(orderId: String, signature: String?) {
        val uid = authRepository.getCurrentUserUid()
        if (uid == null) {
            _uiState.update { it.copy(error = "Debes iniciar sesión para validar retiros.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            runCatching {
                val storeId = authRepository.getUserProfile(uid)?.storeId
                    ?: throw IllegalStateException("No tienes una tienda asignada.")

                orderRepository.confirmStorePickup(
                    orderId = orderId,
                    storeId = storeId,
                    qrSignature = signature,
                )
            }.onSuccess { order ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pickedUpOrderId = order.id,
                        buyerName = order.buyerName.ifBlank { "Comprador" },
                        cardCount = order.cards.size,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, error = error.message ?: "No se pudo validar el retiro.")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/** Lee un parámetro del query string; ancla en `?` o `&` para no cazar coincidencias parciales. */
private fun String.paramOrNull(name: String): String? =
    substringAfter('?', missingDelimiterValue = "")
        .split('&')
        .firstOrNull { it.startsWith("$name=") }
        ?.substringAfter('=')
        ?.takeIf { it.isNotBlank() }
