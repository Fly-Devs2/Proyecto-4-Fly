package ucenfotec.ac.cr.flydevs.presentation.purchaseHistory

import ucenfotec.ac.cr.flydevs.domain.model.Order
import ucenfotec.ac.cr.flydevs.domain.model.OrderStatus

enum class PurchaseHistoryFilter(val label: String) {
    TODOS("Todos"),
    PENDIENTE("Pendiente"),
    PAGADO("Pagado"),
    ENVIADO("Enviado"),
}

enum class PurchaseSortOption(val label: String) {
    FECHA("Fecha"),
    ESTADO("Estado"),
}

data class PurchaseHistoryUiState(
    val isLoading: Boolean = false,
    val orders: List<Order> = emptyList(),
    val activeFilter: PurchaseHistoryFilter = PurchaseHistoryFilter.TODOS,
    val sortOption: PurchaseSortOption = PurchaseSortOption.FECHA,
    val onlyMine: Boolean = true,
    val errorMessage: String? = null,
) {
    val filteredOrders: List<Order> get() {
        val filtered = when (activeFilter) {
            PurchaseHistoryFilter.TODOS -> orders
            PurchaseHistoryFilter.PENDIENTE -> orders.filter {
                it.status in listOf(
                    OrderStatus.RESERVED,
                    OrderStatus.WAITING_PAYMENT,
                    OrderStatus.WAITING_SELLER_DELIVERY,
                )
            }
            PurchaseHistoryFilter.PAGADO -> orders.filter {
                it.status in listOf(
                    OrderStatus.WAITING_STORE_SHIPMENT,
                    OrderStatus.DELIVERED_TO_STORE,
                )
            }
            PurchaseHistoryFilter.ENVIADO -> orders.filter {
                it.status in listOf(
                    OrderStatus.IN_TRANSIT,
                    OrderStatus.PICKED_UP,
                )
            }
        }
        return when (sortOption) {
            PurchaseSortOption.FECHA  -> filtered.sortedByDescending { it.createdAt }
            PurchaseSortOption.ESTADO -> filtered.sortedBy { it.status.ordinal }
        }
    }
}
