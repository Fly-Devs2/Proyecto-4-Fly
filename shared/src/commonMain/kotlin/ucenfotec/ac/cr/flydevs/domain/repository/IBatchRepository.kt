package ucenfotec.ac.cr.flydevs.domain.repository

import kotlinx.coroutines.flow.Flow
import ucenfotec.ac.cr.flydevs.domain.model.Batch
import ucenfotec.ac.cr.flydevs.domain.model.BatchGroup

interface IBatchRepository {
    fun getBatchesForCourier(courierId: String): Flow<List<Batch>>

    fun getBatch(documentId: String): Flow<Batch?>

    fun getBatchGroups(): Flow<List<BatchGroup>>
}
