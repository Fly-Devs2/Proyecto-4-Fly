package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Batch(
    val documentId: String = "",
    val batchId: String = "",
    val status: BatchStatus = BatchStatus.READY_FOR_PICKUP,
    val orderIds: List<String> = emptyList(),
    val courierId: String? = null,
    val courierName: String? = null,
    val courierReward: Long = 0L,
    val sourceStoreId: String = "",
    val sourceStoreName: String = "",
    val sourceStoreAddress: String = "",
    val destinationStoreId: String = "",
    val destinationStoreName: String = "",
    val destinationStoreAddress: String = "",
    val pickupEvidence: BatchEvidence? = null,
    val deliveryEvidence: BatchEvidence? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val acceptedAt: Long = 0L,
    val inTransitAt: Long = 0L,
    val pickupAt: Long = 0L,
    val deliveredAt: Long = 0L,
    val qrStatus: String = "",
    val qrImageUrl: String = "",
    val qrImagePath: String = "",
    val qrExpiresAt: Long? = null,
) {
    val displayId: String get() = batchId.ifBlank { documentId }

    val orderCount: Int get() = orderIds.size
}
