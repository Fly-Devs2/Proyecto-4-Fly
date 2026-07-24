package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BatchEvidence(
    val uploadedBy: String = "",
    val type: String = "",
    val storagePath: String = "",
    val downloadUrl: String = "",
    val note: String = "",
    val uploadedAt: Long = 0L,
)
