package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GenerateOrderQrRequest(
    val orderId: String,
    val forceRegenerate: Boolean = false
)

@Serializable
data class OrderQrResult(
    val orderId: String,
    val qrId: String,
    val qrImagePath: String,
    val qrImageUrl: String,
    val reused: Boolean,
    val status: String
)