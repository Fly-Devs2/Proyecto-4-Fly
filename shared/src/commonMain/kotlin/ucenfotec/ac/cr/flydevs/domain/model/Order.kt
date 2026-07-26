package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: String = "",
    val cards: List<OrderCardSnapshot> = emptyList(),
    val buyerId: String = "",
    val sellerId: String = "",
    val status: OrderStatus = OrderStatus.WAITING_SELLER_DELIVERY,
    val createdAt: Long = 0L,
    val modifiedAt: Long = 0L,
    val shippingMethod: String = "DELIVERY",
    val montoTotal: Long = 0L,
    val sinpeReceiptUrl: String? = null,
    val sellerEvidenceUrls: List<String> = emptyList(),
    val buyerEvidenceUrls: List<String> = emptyList(),
    val sinpePaid: Boolean = false,
    val sinpeRejected: Boolean = false,
    val sellerName: String = "",
    val buyerName: String = "",
    val sobreId: String = "",
    val qrStatus: String = "",
    val qrImageUrl: String = "",
    val qrImagePath: String = "",
    val currentQrId: String = "",
    val destinationStore: String = "",
    val sourceStore: String = "",
)
