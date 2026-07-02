package ucenfotec.ac.cr.flydevs.presentation.exchange

import ucenfotec.ac.cr.flydevs.domain.model.Exchange
import ucenfotec.ac.cr.flydevs.domain.model.ExchangeStatus
import ucenfotec.ac.cr.flydevs.presentation.publishGameCard.ImageError

data class ExchangeBuyerUiState(
    val exchange: Exchange? = null,
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
        get() = exchange?.status == ExchangeStatus.ESPERANDO_COMPROBANTE_SINPE

    val canSubmit: Boolean
        get() = isReadyForSinpe && proofUrl != null && confirmChecked && !isUploadingImage && !isSubmitting
}
