package ucenfotec.ac.cr.flydevs.presentation.theme

import androidx.compose.ui.graphics.Color
import ucenfotec.ac.cr.flydevs.domain.model.OrderStatus

/**
 * Utility to provide a single accent color for an OrderStatus.
 * Mapping per requirement:
 *  - PICKED_UP -> AccentMint (green)
 *  - DISPUTED, CANCELLED -> AccentRed (red)
 *  - otherwise -> AccentGold (yellow)
 */

fun getOrderStatusAccent(status: OrderStatus): Color = when (status) {
    OrderStatus.PICKED_UP -> AccentMint
    OrderStatus.DISPUTED, OrderStatus.CANCELLED -> AccentRed
    else -> AccentGold
}
