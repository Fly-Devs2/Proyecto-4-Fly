package ucenfotec.ac.cr.flydevs.presentation.exchange

import ucenfotec.ac.cr.flydevs.domain.model.Exchange
import ucenfotec.ac.cr.flydevs.presentation.publishGameCard.ImageError

data class ExchangeSellerUiState(
    val exchange: Exchange? = null,
    val isLoading: Boolean = false,

    // ── Evidencia (foto sobre) ──
    val evidenceUrl: String? = null,
    val isUploadingImage: Boolean = false,
    val imageError: ImageError? = null,

    // ── Confirmación ──
    val infoVisibleChecked: Boolean = false,

    // ── Proceso ──
    val isSubmitting: Boolean = false,
    val feedback: SellerEvidenceFeedback? = null,
) {
    val canSubmit: Boolean
        get() = evidenceUrl != null && infoVisibleChecked && !isUploadingImage && !isSubmitting
}
