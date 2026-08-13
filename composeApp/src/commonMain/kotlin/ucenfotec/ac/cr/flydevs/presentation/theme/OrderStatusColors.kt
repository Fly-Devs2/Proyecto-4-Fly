package ucenfotec.ac.cr.flydevs.presentation.theme

import androidx.compose.ui.graphics.Color
import ucenfotec.ac.cr.flydevs.domain.model.OrderStatus

/**
 * Utility to provide a single accent color for an OrderStatus.
 * Mapping per requirement:
 *  - PICKED_UP -> AccentMint (green)
 *  - DISPUTED, CANCELLED -> AccentRed (red)
 *  - otherwise (in-process states) -> AccentGold (yellow)
 */

fun getOrderStatusAccent(status: OrderStatus): Color = when (status) {
    OrderStatus.PICKED_UP -> AccentMint
    OrderStatus.DISPUTED, OrderStatus.CANCELLED -> AccentRed
    else -> AccentGold  // Includes: RESERVED, WAITING_SELLER_DELIVERY, WAITING_PAYMENT, AWAITING_SINPE_VALIDATION, WAITING_STORE_SHIPMENT, IN_TRANSIT, DELIVERED_TO_STORE
}
