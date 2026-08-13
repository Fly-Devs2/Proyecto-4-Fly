package ucenfotec.ac.cr.flydevs.presentation.storeHome

import ucenfotec.ac.cr.flydevs.domain.model.DeliveryBatch

/** Días de espera a partir de los cuales el retiro se marca como "por vencer". */
const val PICKUP_OVERDUE_DAYS = 3

/** Una orden que llegó a la tienda y está esperando a que el comprador la retire. */
data class StorePickup(
    val orderId: String,
    val buyerName: String,
    val cardCount: Int,
    val waitingDays: Int,
) {
    val isOverdue: Boolean get() = waitingDays >= PICKUP_OVERDUE_DAYS
}

data class StoreHomeUiState(
    val isLoading: Boolean = true,
    val storeId: String = "",
    val storeName: String = "",
    val incomingBatches: List<DeliveryBatch> = emptyList(),
    val pendingPickups: List<StorePickup> = emptyList(),
    val batchesReceivedToday: Int = 0,
    val pickupsToday: Int = 0,
    val unreadNotifications: Int = 0,
    val errorMessage: String? = null,
) {
    val pendingPickupsCount: Int get() = pendingPickups.size

    val overduePickupsCount: Int get() = pendingPickups.count { it.isOverdue }

    /** Top 3 más recientes, igual que "Mis últimos pedidos" en la vista de usuario. */
    val recentPendingPickups: List<StorePickup> get() = pendingPickups.take(3)
}
