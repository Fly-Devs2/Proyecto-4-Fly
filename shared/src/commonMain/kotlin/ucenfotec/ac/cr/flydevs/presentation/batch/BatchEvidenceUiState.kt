package ucenfotec.ac.cr.flydevs.presentation.batch

import ucenfotec.ac.cr.flydevs.domain.model.DeliveryBatch

data class BatchEvidenceUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val isUploadingImage: Boolean = false,
    val batch: DeliveryBatch? = null,
    val proofUrl: String? = null,
    val note: String = "",
    val confirmChecked: Boolean = false,
    val feedback: BatchEvidenceFeedback? = null,
    val error: String? = null
)

enum class BatchEvidenceFeedback {
    SUCCESS,
    SUBMIT_FAILED,
    MISSING_PHOTO,
    MISSING_CONFIRM
}
