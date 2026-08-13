package ucenfotec.ac.cr.flydevs.presentation.exchange

import ucenfotec.ac.cr.flydevs.domain.model.Order
import ucenfotec.ac.cr.flydevs.domain.model.OrderStatus
import ucenfotec.ac.cr.flydevs.presentation.publishGameCard.ImageError

data class ExchangeBuyerUiState(
    val order: Order? = null,
    val isLoading: Boolean = false,

    // ── Comprobante SINPE ──
    val proofUrl: String? = null,
    val isUploadingImage: Boolean = false,
    val imageError: ImageError? = null,

    // ── Confirmación ──
    val confirmChecked: Boolean = false,

    // ── Proceso ──
    val isSubmitting: Boolean = false,
    val feedback: SinpeProofFeedback? = null,
) {
    val isReadyForSinpe: Boolean
        get() = order?.status == OrderStatus.WAITING_PAYMENT ||
            order?.status == OrderStatus.WAITING_SELLER_DELIVERY ||
            (order?.status == OrderStatus.AWAITING_SINPE_VALIDATION && order?.sinpeRejected == true)

    val canSubmit: Boolean
        get() = isReadyForSinpe && proofUrl != null && confirmChecked && !isUploadingImage && !isSubmitting
}
