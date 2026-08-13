package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ShipmentLocation(
    /**
     * ID real del documento en batches/{id}.
     */
    val batchDocumentId: String = "",

    val courierId: String = "",

    /**
     * Compradores autorizados para consultar
     * la ubicación de las órdenes del lote.
     */
    val buyerIds: List<String> = emptyList(),

    val latitude: Double = 0.0,
    val longitude: Double = 0.0,

    /**
     * Precisión aproximada reportada por Android.
     * No se mostrará inicialmente en la interfaz.
     */
    val accuracyMeters: Double = 0.0,

    val batchStatus: String =
        BatchStatus.IN_TRANSIT.name,

    val trackingActive: Boolean = false,

    val updatedAt: Long = 0L
)