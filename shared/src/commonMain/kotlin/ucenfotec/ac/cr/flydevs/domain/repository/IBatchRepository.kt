package ucenfotec.ac.cr.flydevs.domain.repository

import kotlinx.coroutines.flow.Flow
import ucenfotec.ac.cr.flydevs.domain.model.BatchEvidence
import ucenfotec.ac.cr.flydevs.domain.model.BatchGroup
import ucenfotec.ac.cr.flydevs.domain.model.DeliveryBatch

interface IBatchRepository {

    /**
     * Utilizado por el administrador o la tienda.
     */
    suspend fun createBatch(batch: DeliveryBatch): String

    /**
     * Lotes que todavía no tienen mensajero.
     */
    fun observeAvailableBatches(): Flow<List<DeliveryBatch>>

    /**
     * Lotes asignados al mensajero.
     */
    fun observeCourierBatches(
        courierId: String
    ): Flow<List<DeliveryBatch>>

    suspend fun getBatchById(
        batchId: String
    ): DeliveryBatch?

    /**
     * Agrupaciones de lotes por tienda destino, para la vista de auditoría.
     */
    fun observeBatchGroups(): Flow<List<BatchGroup>>

    /**
     * Se ejecuta después de validar el QR.
     */
    suspend fun acceptBatch(
        batchId: String,
        courierId: String,
        courierName: String
    )

    /**
     * Se ejecuta después de subir la foto de recogida.
     */
    suspend fun confirmPickup(
        batchId: String,
        courierId: String,
        evidence: BatchEvidence
    )

    suspend fun startDeliveryRoute(
        batchId: String,
        courierId: String
    )

    /**
     * Se ejecuta después de subir la foto de entrega.
     */
    suspend fun confirmDelivery(
        batchId: String,
        courierId: String,
        evidence: BatchEvidence
    )
}
