package ucenfotec.ac.cr.flydevs.presentation.myBatches

import ucenfotec.ac.cr.flydevs.domain.model.Batch
import ucenfotec.ac.cr.flydevs.domain.model.BatchStatus

data class BatchGroupItem(
    val key: String,
    val storeDestinationName: String,
    val batches: List<Batch> = emptyList(),
) {
    val batchCount: Int get() = batches.size

    val orderCount: Int get() = batches.sumOf { it.orderCount }

    val deliveredCount: Int get() = batches.count { it.status == BatchStatus.DELIVERED }
}

data class MyBatchesUiState(
    val isLoading: Boolean = true,
    val groups: List<BatchGroupItem> = emptyList(),
    val expandedKeys: Set<String> = emptySet(),
    val page: Int = 0,
    val errorMessage: String? = null,
) {
    val totalPages: Int
        get() = if (groups.isEmpty()) 1 else (groups.size + GROUPS_PER_PAGE - 1) / GROUPS_PER_PAGE

    val visibleGroups: List<BatchGroupItem>
        get() = groups.drop(page * GROUPS_PER_PAGE).take(GROUPS_PER_PAGE)

    val totalBatches: Int get() = groups.sumOf { it.batchCount }

    fun isExpanded(key: String): Boolean = key in expandedKeys

    companion object {
        const val GROUPS_PER_PAGE = 4
    }
}
