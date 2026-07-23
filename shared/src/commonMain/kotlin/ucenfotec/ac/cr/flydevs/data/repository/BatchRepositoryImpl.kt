package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ucenfotec.ac.cr.flydevs.domain.model.BatchEvidence
import ucenfotec.ac.cr.flydevs.domain.model.BatchEvidenceType
import ucenfotec.ac.cr.flydevs.domain.model.BatchQrStatus
import ucenfotec.ac.cr.flydevs.domain.model.BatchStatus
import ucenfotec.ac.cr.flydevs.domain.model.DeliveryBatch
import ucenfotec.ac.cr.flydevs.domain.repository.IBatchRepository
import kotlin.time.Clock

class BatchRepositoryImpl : IBatchRepository {

    private val firestore = Firebase.firestore

    private val batchesCollection =
        firestore.collection("batches")

    override suspend fun createBatch(
        batch: DeliveryBatch
    ): String {
        require(batch.orderIds.isNotEmpty()) {
            "El lote debe contener al menos una orden."
        }

        require(batch.sourceStoreId.isNotBlank()) {
            "La tienda de origen es obligatoria."
        }

        require(batch.destinationStoreId.isNotBlank()) {
            "La tienda de destino es obligatoria."
        }

        require(batch.sourceStoreId != batch.destinationStoreId) {
            "La tienda de origen y destino no pueden ser iguales."
        }

        val now = currentTimeMillis()

        val newBatch = batch.copy(
            status = BatchStatus.READY_FOR_PICKUP,
            courierId = null,
            courierName = null,
            pickupEvidence = null,
            deliveryEvidence = null,
            acceptedAt = null,
            pickupAt = null,
            inTransitAt = null,
            deliveredAt = null,
            createdAt = now,
            updatedAt = now
        )

        val documentReference = batchesCollection.add(newBatch)

        return documentReference.id
    }

    override fun observeAvailableBatches(): Flow<List<DeliveryBatch>> {
        return batchesCollection
            .where {
                "status" equalTo BatchStatus.READY_FOR_PICKUP.name
            }
            .snapshots
            .map { querySnapshot ->
                querySnapshot.documents
                    .map { document ->
                        mapDocumentToBatch(document)
                    }
                    .filter { batch ->
                        batch.courierId.isNullOrBlank()
                    }
                    .sortedBy { batch ->
                        batch.createdAt
                    }
            }
    }

    override fun observeCourierBatches(
        courierId: String
    ): Flow<List<DeliveryBatch>> {
        require(courierId.isNotBlank()) {
            "El identificador del mensajero es obligatorio."
        }

        return batchesCollection
            .where {
                "courierId" equalTo courierId
            }
            .snapshots
            .map { querySnapshot ->
                querySnapshot.documents
                    .map { document ->
                        mapDocumentToBatch(document)
                    }
                    .sortedByDescending { batch ->
                        batch.updatedAt
                    }
            }
    }

    override suspend fun getBatchById(
        batchId: String
    ): DeliveryBatch? {
        require(batchId.isNotBlank()) {
            "El identificador del lote es obligatorio."
        }

        val document = batchesCollection
            .document(batchId)
            .get()

        if (!document.exists) {
            return null
        }

        return mapDocumentToBatch(document)
    }

    override suspend fun acceptBatch(
        batchId: String,
        courierId: String,
        courierName: String
    ) {
        require(batchId.isNotBlank()) {
            "El identificador del lote es obligatorio."
        }

        require(courierId.isNotBlank()) {
            "El identificador del mensajero es obligatorio."
        }

        require(courierName.isNotBlank()) {
            "El nombre del mensajero es obligatorio."
        }

        val batchReference = batchesCollection.document(batchId)
        val now = currentTimeMillis()

        firestore.runTransaction {
            val snapshot = get(batchReference)

            check(snapshot.exists) {
                "El lote no existe."
            }

            val currentBatch = mapDocumentToBatch(snapshot)

            check(
                currentBatch.status == BatchStatus.READY_FOR_PICKUP
            ) {
                "El lote ya no está disponible."
            }

            check(currentBatch.courierId.isNullOrBlank()) {
                "El lote ya fue asignado a otro mensajero."
            }

            set(
                documentRef = batchReference,
                data = currentBatch.copy(
                    courierId = courierId,
                    courierName = courierName,
                    status = BatchStatus.ACCEPTED,
                    acceptedAt = now,
                    updatedAt = now,
                    qrStatus = BatchQrStatus.USED
                )
            )
        }
    }

    override suspend fun confirmPickup(
        batchId: String,
        courierId: String,
        evidence: BatchEvidence
    ) {
        require(batchId.isNotBlank()) {
            "El identificador del lote es obligatorio."
        }

        require(courierId.isNotBlank()) {
            "El identificador del mensajero es obligatorio."
        }

        require(evidence.storagePath.isNotBlank()) {
            "La evidencia de recogida es obligatoria."
        }

        val batchReference = batchesCollection.document(batchId)
        val now = currentTimeMillis()

        firestore.runTransaction {
            val snapshot = get(batchReference)

            check(snapshot.exists) {
                "El lote no existe."
            }

            val currentBatch = mapDocumentToBatch(snapshot)

            validateCourier(
                batch = currentBatch,
                courierId = courierId
            )

            check(currentBatch.status == BatchStatus.ACCEPTED) {
                "El lote no se encuentra en estado aceptado."
            }

            set(
                documentRef = batchReference,
                data = currentBatch.copy(
                    pickupEvidence = evidence.copy(
                        type = BatchEvidenceType.PICKUP,
                        uploadedAt = now,
                        uploadedBy = courierId
                    ),
                    pickupAt = now,
                    status = BatchStatus.PICKED_UP,
                    updatedAt = now
                )
            )
        }
    }

    override suspend fun startDeliveryRoute(
        batchId: String,
        courierId: String
    ) {
        require(batchId.isNotBlank()) {
            "El identificador del lote es obligatorio."
        }

        require(courierId.isNotBlank()) {
            "El identificador del mensajero es obligatorio."
        }

        val batchReference = batchesCollection.document(batchId)
        val now = currentTimeMillis()

        firestore.runTransaction {
            val snapshot = get(batchReference)

            check(snapshot.exists) {
                "El lote no existe."
            }

            val currentBatch = mapDocumentToBatch(snapshot)

            validateCourier(
                batch = currentBatch,
                courierId = courierId
            )

            check(currentBatch.status == BatchStatus.PICKED_UP) {
                "Primero debe confirmarse la recogida del lote."
            }

            set(
                documentRef = batchReference,
                data = currentBatch.copy(
                    status = BatchStatus.IN_TRANSIT,
                    inTransitAt = now,
                    updatedAt = now
                )
            )
        }
    }

    override suspend fun confirmDelivery(
        batchId: String,
        courierId: String,
        evidence: BatchEvidence
    ) {
        require(batchId.isNotBlank()) {
            "El identificador del lote es obligatorio."
        }

        require(courierId.isNotBlank()) {
            "El identificador del mensajero es obligatorio."
        }

        require(evidence.storagePath.isNotBlank()) {
            "La evidencia de entrega es obligatoria."
        }

        val batchReference = batchesCollection.document(batchId)
        val now = currentTimeMillis()

        firestore.runTransaction {
            val snapshot = get(batchReference)

            check(snapshot.exists) {
                "El lote no existe."
            }

            val currentBatch = mapDocumentToBatch(snapshot)

            validateCourier(
                batch = currentBatch,
                courierId = courierId
            )

            check(currentBatch.status == BatchStatus.IN_TRANSIT) {
                "El lote no se encuentra en ruta."
            }

            set(
                documentRef = batchReference,
                data = currentBatch.copy(
                    deliveryEvidence = evidence.copy(
                        type = BatchEvidenceType.DELIVERY,
                        uploadedAt = now,
                        uploadedBy = courierId
                    ),
                    deliveredAt = now,
                    status = BatchStatus.DELIVERED,
                    updatedAt = now
                )
            )
        }
    }

    private fun mapDocumentToBatch(
        document: DocumentSnapshot
    ): DeliveryBatch {
        return document
            .data<DeliveryBatch>()
            .copy(id = document.id)
    }

    private fun validateCourier(
        batch: DeliveryBatch,
        courierId: String
    ) {
        check(batch.courierId == courierId) {
            "El lote está asignado a otro mensajero."
        }
    }

    private fun currentTimeMillis(): Long {
        return Clock.System
            .now()
            .toEpochMilliseconds()
    }
}