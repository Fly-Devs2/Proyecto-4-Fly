package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ucenfotec.ac.cr.flydevs.domain.model.Batch
import ucenfotec.ac.cr.flydevs.domain.model.BatchEvidence
import ucenfotec.ac.cr.flydevs.domain.model.BatchGroup
import ucenfotec.ac.cr.flydevs.domain.model.BatchStatus
import ucenfotec.ac.cr.flydevs.domain.repository.IBatchRepository

class BatchRepositoryImpl : IBatchRepository {
    private val firestore = Firebase.firestore
    private val batchesCollection = firestore.collection("batches")
    private val batchGroupsCollection = firestore.collection("batch_group")

    override fun getBatchesForCourier(courierId: String): Flow<List<Batch>> {
        return batchesCollection.snapshots.map { snapshot ->
            snapshot.documents
                .mapNotNull { it.toBatch() }
                .filter { it.courierId?.trim() == courierId.trim() }
                .sortedByDescending { it.deliveredAt.takeIf { at -> at > 0L } ?: it.createdAt }
        }
    }

    override fun getBatch(documentId: String): Flow<Batch?> {
        return batchesCollection.document(documentId).snapshots.map { snapshot ->
            if (snapshot.exists) snapshot.toBatch() else null
        }
    }

    override fun getBatchGroups(): Flow<List<BatchGroup>> {
        return batchGroupsCollection.snapshots.map { snapshot ->
            snapshot.documents.mapNotNull { it.toBatchGroup() }
        }
    }

    private fun DocumentSnapshot.toBatch(): Batch? {
        runCatching { data(Batch.serializer()) }.getOrNull()?.let { return it.copy(documentId = id) }

        return runCatching {
            Batch(
                documentId = id,
                batchId = safeGet<String>("batchId").orEmpty(),
                status = BatchStatus.fromFirestore(safeGet<String>("status")),
                orderIds = safeGet<List<String>>("orderIds") ?: emptyList(),
                courierId = safeGet<String>("courierId"),
                courierName = safeGet<String>("courierName"),
                courierReward = safeGet<Long>("courierReward") ?: 0L,
                sourceStoreId = safeGet<String>("sourceStoreId").orEmpty(),
                sourceStoreName = safeGet<String>("sourceStoreName").orEmpty(),
                sourceStoreAddress = safeGet<String>("sourceStoreAddress").orEmpty(),
                destinationStoreId = safeGet<String>("destinationStoreId").orEmpty(),
                destinationStoreName = safeGet<String>("destinationStoreName").orEmpty(),
                destinationStoreAddress = safeGet<String>("destinationStoreAddress").orEmpty(),
                pickupEvidence = safeGet<BatchEvidence>("pickupEvidence"),
                deliveryEvidence = safeGet<BatchEvidence>("deliveryEvidence"),
                createdAt = safeGet<Long>("createdAt") ?: 0L,
                updatedAt = safeGet<Long>("updatedAt") ?: 0L,
                acceptedAt = safeGet<Long>("acceptedAt") ?: 0L,
                inTransitAt = safeGet<Long>("inTransitAt") ?: 0L,
                pickupAt = safeGet<Long>("pickupAt") ?: 0L,
                deliveredAt = safeGet<Long>("deliveredAt") ?: 0L,
                qrStatus = safeGet<String>("qrStatus").orEmpty(),
                qrImageUrl = safeGet<String>("qrImageUrl").orEmpty(),
                qrImagePath = safeGet<String>("qrImagePath").orEmpty(),
                qrExpiresAt = safeGet<Long>("qrExpiresAt"),
            )
        }.onFailure {
            println("DEBUG_BATCHES: No se pudo mapear el lote $id: ${it.message}")
        }.getOrNull()
    }

    private fun DocumentSnapshot.toBatchGroup(): BatchGroup? {
        runCatching { data(BatchGroup.serializer()) }.getOrNull()
            ?.let { return it.copy(documentId = id) }

        return runCatching {
            BatchGroup(
                documentId = id,
                id = safeGet<String>("id").orEmpty(),
                batchList = batchList(),
                storeDestination = safeGet<String>("storeDestination").orEmpty(),
            )
        }.onFailure {
            println("DEBUG_BATCHES: No se pudo mapear el grupo $id: ${it.message}")
        }.getOrNull()
    }

    /** Acepta `batchList` como array o como un único string. */
    private fun DocumentSnapshot.batchList(): List<String> {
        safeGet<List<String>>("batchList")?.let { return it }
        safeGet<String>("batchList")?.let { return listOf(it) }
        return emptyList()
    }

    private inline fun <reified T> DocumentSnapshot.safeGet(field: String): T? =
        runCatching { get<T>(field) }.getOrNull()
}
