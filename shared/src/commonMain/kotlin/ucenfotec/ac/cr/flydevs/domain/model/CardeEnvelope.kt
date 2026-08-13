package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable


@Serializable
data class CardEnvelope(
    val id: String = "",
    val cards: List<GameCard> = emptyList(),
    val cardIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val subTotal: Long = 0L,
    val total: Long = 0L,
    val status: String = "PENDING",
    val shippingMethod: ShippingMethod = ShippingMethod.DELIVERY,
    val userId: String = "",
    val sellerId: String = "",
    val destinationStore: String = "",
    val sourceStore: String = "",
)
