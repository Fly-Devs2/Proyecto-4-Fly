package ucenfotec.ac.cr.flydevs.presentation.exchange

/** Vendedor sube la foto carta+sobre (pantalla EntregarEnTienda). */
enum class SellerEvidenceFeedback {
    SUCCESS,
    MISSING_EVIDENCE, // falta la foto o el checkbox de info del sobre visible
    SUBMIT_FAILED,
}

/** Comprador sube el comprobante SINPE (pantalla PagarSINPE). */
enum class SinpeProofFeedback {
    SUCCESS,
    NOT_READY,     // el vendedor aún no subió su evidencia
    MISSING_PROOF, // falta el comprobante o el checkbox de confirmación
    SUBMIT_FAILED,
}
