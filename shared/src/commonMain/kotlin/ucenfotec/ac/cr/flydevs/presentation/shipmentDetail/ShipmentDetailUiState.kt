package ucenfotec.ac.cr.flydevs.presentation.shipmentDetail

import ucenfotec.ac.cr.flydevs.domain.model.Batch
import ucenfotec.ac.cr.flydevs.domain.model.Order

data class ShipmentDetailUiState(
    val isLoading: Boolean = true,
    val batch: Batch? = null,
    val visibleOrders: List<Order> = emptyList(),
    val isLoadingOrders: Boolean = false,
    val ordersPage: Int = 0,
    val errorMessage: String? = null,
) {
    val totalOrders: Int get() = batch?.orderCount ?: 0

    val totalOrderPages: Int
        get() = if (totalOrders == 0) 1 else (totalOrders + ORDERS_PER_PAGE - 1) / ORDERS_PER_PAGE

    fun orderIdsForPage(page: Int): List<String> =
        batch?.orderIds.orEmpty().drop(page * ORDERS_PER_PAGE).take(ORDERS_PER_PAGE)

    val deliveryEvidenceUrl: String? get() = batch?.deliveryEvidence?.downloadUrl?.takeIf { it.isNotBlank() }

    val pickupEvidenceUrl: String? get() = batch?.pickupEvidence?.downloadUrl?.takeIf { it.isNotBlank() }

    companion object {
        const val ORDERS_PER_PAGE = 5
    }
}
