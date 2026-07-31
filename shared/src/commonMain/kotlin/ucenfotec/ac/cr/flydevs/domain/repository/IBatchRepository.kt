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

    fun observeBatchById(
        batchId: String
    ): Flow<DeliveryBatch?>

    suspend fun findBatchIdByLabel(label: String): String?

    /**
     * Agrupaciones de lotes por tienda destino, para la vista de auditoría.
     */
    fun observeBatchGroups(): Flow<List<BatchGroup>>

    /**
     * Lotes listos para ser recogidos de una tienda específica.
     */
    fun observeOutgoingStoreBatches(storeId: String): Flow<List<DeliveryBatch>>

    /**
     * Lotes ya entregados a una tienda destino y listos para retiro.
     */
    fun observeStorePickups(storeId: String): Flow<List<DeliveryBatch>>

    /**
     * Se ejecuta después de validar el QR.
     */
    suspend fun acceptBatch(
        batchId: String,
        courierId: String,
        courierName: String
    )

    /**
     * Acepta todos los lotes listos para recogida de una tienda.
     * Retorna la cantidad de lotes aceptados.
     */
    suspend fun acceptAllStoreBatches(
        storeId: String,
        courierId: String,
        courierName: String
    ): Int

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
