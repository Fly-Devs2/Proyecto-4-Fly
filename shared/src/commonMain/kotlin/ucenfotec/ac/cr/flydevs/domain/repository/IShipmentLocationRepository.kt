package ucenfotec.ac.cr.flydevs.domain.repository

import kotlinx.coroutines.flow.Flow
import ucenfotec.ac.cr.flydevs.domain.model.BatchStatus
import ucenfotec.ac.cr.flydevs.domain.model.ShipmentLocation

interface IShipmentLocationRepository {

    /**
     * Observa la ubicación actual del lote.
     */
    fun observeShipmentLocation(
        batchDocumentId: String
    ): Flow<ShipmentLocation?>

    /**
     * Publica o reemplaza la posición actual.
     */
    suspend fun publishShipmentLocation(
        location: ShipmentLocation
    )

    /**
     * Marca el seguimiento como finalizado.
     */
    suspend fun stopTracking(
        batchDocumentId: String,
        finalStatus: BatchStatus
    )
}