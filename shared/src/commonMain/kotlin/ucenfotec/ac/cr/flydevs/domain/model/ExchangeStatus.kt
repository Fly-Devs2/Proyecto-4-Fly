package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

/**
 * Estado del proceso de un intercambio o sobre.
 */
@Serializable
enum class ExchangeStatus {
    ESPERANDO_ENTREGA_TIENDA, // El vendedor debe entregar el sobre en la tienda y subir una foto como evidencia.
    ESPERANDO_COMPROBANTE_SINPE, // Si ya existe evidencia del vendedor, se espera el comprobante del comprador.
    COMPROBANTE_RECIBIDO, // El comprador subió el comprobante SINPE.
    EN_ENVIO,
    COMPLETADO,
    EXPIRADO, // El intercambio expiró por falta de comprobantes.
}
