package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: String = "",
    val cardId: String = "",
    val buyerId: String = "",
    val sellerId: String = "",
    val cardName: String = "",
    val cardImageUrl: String = "",
    val cardPrice: Long = 0L,
    val status: OrderStatus = OrderStatus.RESERVED,
    val createdAt: Long = 0L,
    val modifiedAt: Long = 0L,
    val shippingMethod: String = "DELIVERY",
    val montoTotal: Long = 0L,
    val sinpeReceiptUrl: String? = null,
    val sellerEvidenceUrl: String? = null,
    val buyerEvidenceUrl: String? = null,
    val sinpePaid: Boolean = false,
    val sellerName: String = "",
    val buyerName: String = "",
    val sobreId: String = ""
)
