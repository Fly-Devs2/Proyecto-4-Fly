package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
enum class BatchStatus {
    READY_FOR_PICKUP,
    ACCEPTED,
    PICKED_UP,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED
}

@Serializable
enum class BatchQrStatus {
    ACTIVE,
    USED,
    EXPIRED,
    CANCELLED
}

@Serializable
enum class BatchEvidenceType {
    PICKUP,
    DELIVERY
}

@Serializable
data class BatchEvidence(
    val storagePath: String = "",
    val downloadUrl: String = "",
    val uploadedAt: Long = 0L,
    val uploadedBy: String = "",
    val type: BatchEvidenceType = BatchEvidenceType.PICKUP,
    val note: String = ""
)

@Serializable
data class DeliveryBatch(

    // El ID será el ID del documento de Firestore.
    // @Transient evita que se guarde duplicado dentro del documento.
    @Transient
    val id: String = "",

    val batchId: String = "",

    val orderIds: List<String> = emptyList(),

    val sourceStoreId: String = "",
    val sourceStoreName: String = "",
    val sourceStoreAddress: String = "",

    val destinationStoreId: String = "",
    val destinationStoreName: String = "",
    val destinationStoreAddress: String = "",

    val courierId: String? = null,
    val courierName: String? = null,

    val status: BatchStatus = BatchStatus.READY_FOR_PICKUP,

    // Pago que recibirá el mensajero.
    val courierReward: Long = 0L,

    val qrImagePath: String = "",
    val qrImageUrl: String = "",
    val qrStatus: BatchQrStatus = BatchQrStatus.ACTIVE,
    val qrExpiresAt: Long? = null,

    val pickupEvidence: BatchEvidence? = null,
    val deliveryEvidence: BatchEvidence? = null,

    val createdAt: Long = 0L,
    val acceptedAt: Long? = null,
    val pickupAt: Long? = null,
    val inTransitAt: Long? = null,
    val deliveredAt: Long? = null,
    val updatedAt: Long = 0L
)