package ucenfotec.ac.cr.flydevs.presentation.myBatches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ucenfotec.ac.cr.flydevs.domain.model.Batch
import ucenfotec.ac.cr.flydevs.domain.model.BatchGroup
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IBatchRepository

class MyBatchesViewModel(
    private val authRepository: IAuthRepository,
    private val batchRepository: IBatchRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyBatchesUiState())
    val uiState: StateFlow<MyBatchesUiState> = _uiState.asStateFlow()

    init {
        loadBatches()
    }

    fun toggleGroup(key: String) {
        val expanded = _uiState.value.expandedKeys
        _uiState.value = _uiState.value.copy(
            expandedKeys = if (key in expanded) expanded - key else expanded + key,
        )
    }

    fun nextPage() {
        val state = _uiState.value
        if (state.page + 1 < state.totalPages) {
            _uiState.value = state.copy(page = state.page + 1)
        }
    }

    fun previousPage() {
        val state = _uiState.value
        if (state.page > 0) {
            _uiState.value = state.copy(page = state.page - 1)
        }
    }

    private fun loadBatches() {
        val courierId = authRepository.getCurrentUserUid()
        if (courierId == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Iniciá sesión para ver tus lotes.",
            )
            return
        }

        val groupsFlow = batchRepository.getBatchGroups()
            .catch { error ->
                println("DEBUG_BATCHES: batch_group no disponible (${error.message})")
                emit(emptyList())
            }

        combine(
            batchRepository.getBatchesForCourier(courierId),
            groupsFlow,
        ) { batches, groups -> buildGroupItems(batches, groups) }
            .onEach { items ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    groups = items,
                    page = _uiState.value.page.coerceAtMost(lastPageIndex(items.size)),
                    errorMessage = null,
                )
            }
            .catch { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Error al cargar los lotes",
                )
            }
            .launchIn(viewModelScope)
    }

    private fun buildGroupItems(batches: List<Batch>, groups: List<BatchGroup>): List<BatchGroupItem> {
        val grouped = groups.map { group ->
            val members = batches.filter { it.documentId in group.batchList || it.batchId in group.batchList }
            BatchGroupItem(
                key = group.documentId,
                storeDestinationName = groupStoreName(group, members),
                batches = members.sortedByDescending { it.deliveredAt },
            )
        }

        // Un lote sin grupo desaparecería de la vista, así que lo agrupamos por su tienda destino.
        val claimed = grouped.flatMap { it.batches }.map { it.documentId }.toSet()
        val orphans = batches.filterNot { it.documentId in claimed }
            .groupBy { it.destinationStoreId }
            .map { (storeId, storeBatches) ->
                BatchGroupItem(
                    key = "store:$storeId",
                    storeDestinationName = storeBatches.firstOrNull { it.destinationStoreName.isNotBlank() }
                        ?.destinationStoreName
                        ?: storeId.ifBlank { "Sin tienda destino" },
                    batches = storeBatches.sortedByDescending { it.deliveredAt },
                )
            }

        return (grouped + orphans)
            .filter { it.batches.isNotEmpty() }
            .sortedBy { it.storeDestinationName }
    }

    /** `storeDestination` del grupo puede traer un id o un nombre suelto; el lote es más fiable. */
    private fun groupStoreName(group: BatchGroup, members: List<Batch>): String =
        members.firstOrNull { it.destinationStoreName.isNotBlank() }?.destinationStoreName
            ?: group.storeDestination.ifBlank { "Sin tienda destino" }

    private fun lastPageIndex(groupCount: Int): Int =
        if (groupCount == 0) 0
        else (groupCount + MyBatchesUiState.GROUPS_PER_PAGE - 1) / MyBatchesUiState.GROUPS_PER_PAGE - 1
}
