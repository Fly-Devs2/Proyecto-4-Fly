package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

/**
 * Un carrito con cartas de varios vendedores se divide en un [Exchange] por vendedor.
 */
@Serializable
data class Exchange(
    val id: String = "",
    val buyerId: String = "",
    val sellerId: String = "",
    val storeId: String = "", //Tienda destino, hay que seleccionarla en algún punto del flujo.
    val cardIds: List<String> = emptyList(),
    val total: Long = 0L,
    val status: ExchangeStatus = ExchangeStatus.WAITING_STORE_DELIVERY,
    val sellerEvidenceUrl: String = "", // Foto de la evidencia al entregar en tienda.
    val sinpeProofUrl: String = "",    //Comprobante de SINPE Móvil (evidencia del comprador).
    val createdAt: Long = 0L,
)
