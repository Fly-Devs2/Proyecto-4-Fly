package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

/**
 * Estado del proceso de un intercambio o sobre.
 */
@Serializable
enum class ExchangeStatus {
    WAITING_STORE_DELIVERY, // El vendedor debe entregar el sobre en la tienda y subir una foto como evidencia.
    WAITING_SINPE_PROOF,    // Ya existe evidencia del vendedor, se espera el comprobante del comprador.
    PROOF_RECEIVED,         // El comprador subió el comprobante SINPE.
    SHIPPING,
    COMPLETED,
    EXPIRED,                // El intercambio expiró por falta de comprobante, cartas vuelven a AVAILABLE.
}
