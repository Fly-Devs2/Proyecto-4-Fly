package ucenfotec.ac.cr.flydevs.presentation.AdminIncident

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.model.AdminIncidentFilter
import ucenfotec.ac.cr.flydevs.domain.model.AdminIncidentItem
import ucenfotec.ac.cr.flydevs.domain.model.IncidentStatus
import ucenfotec.ac.cr.flydevs.domain.repository.IIncidentRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IOrderRepository

class AdminIncidentsViewModel(
    private val incidentRepository: IIncidentRepository,
    private val orderRepository: IOrderRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(AdminIncidentsUiState())

    val uiState: StateFlow<AdminIncidentsUiState> =
        _uiState.asStateFlow()

    private var incidentsJob: Job? = null

    init {
        observeIncidents()
    }

    /**
     * Actualiza la búsqueda y vuelve a mostrar
     * las primeras cinco coincidencias.
     */
    fun onSearchQueryChange(
        query: String
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                visibleLimit = INITIAL_VISIBLE_LIMIT
            )
        }

        updateVisibleIncidents()
    }

    /**
     * Cambia el filtro seleccionado.
     */
    fun onFilterSelected(
        filter: AdminIncidentFilter
    ) {
        if (
            _uiState.value.selectedFilter == filter
        ) {
            return
        }

        _uiState.update { currentState ->
            currentState.copy(
                selectedFilter = filter,
                visibleLimit = INITIAL_VISIBLE_LIMIT
            )
        }

        updateVisibleIncidents()
    }

    /**
     * Muestra cinco incidencias adicionales.
     *
     * No hace otra consulta a Firestore porque el listado
     * administrativo ya se está observando en tiempo real.
     */
    fun loadMoreIncidents() {
        val currentState = _uiState.value

        if (!currentState.hasMoreIncidents) {
            return
        }

        _uiState.update {
            it.copy(
                visibleLimit =
                    it.visibleLimit + PAGE_SIZE
            )
        }

        updateVisibleIncidents()
    }

    fun retry() {
        observeIncidents()
    }

    private fun observeIncidents() {
        incidentsJob?.cancel()

        incidentsJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            incidentRepository
                .observeAllIncidents()
                .catch { exception ->
                    println(
                        "ADMIN_INCIDENTS_ERROR | " +
                                "No se pudieron observar las incidencias: " +
                                exception.message
                    )

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage =
                                exception.message
                                    ?: "No se pudieron cargar las incidencias."
                        )
                    }
                }
                .collectLatest { incidents ->
                    /*
                     * Obtenemos todas las órdenes relacionadas
                     * en una sola operación de repositorio.
                     */
                    val orderIds = incidents
                        .map { it.orderId.trim() }
                        .filter { it.isNotBlank() }
                        .distinct()

                    val orders = runCatching {
                        orderRepository.getOrdersByIds(
                            orderIds = orderIds
                        )
                    }.getOrElse { exception ->
                        /*
                         * Una falla cargando órdenes no impide
                         * mostrar las incidencias básicas.
                         */
                        println(
                            "ADMIN_INCIDENTS_ORDER_ERROR | " +
                                    "No se pudieron cargar las órdenes: " +
                                    exception.message
                        )

                        emptyList()
                    }

                    val ordersById =
                        orders.associateBy { it.id }

                    val enrichedIncidents =
                        incidents
                            .map { incident ->
                                val order =
                                    ordersById[incident.orderId]

                                AdminIncidentItem(
                                    incident = incident,

                                    cardNames = order
                                        ?.cards
                                        ?.map { card ->
                                            card.name.trim()
                                        }
                                        ?.filter { cardName ->
                                            cardName.isNotBlank()
                                        }
                                        .orEmpty(),

                                    buyerId =
                                        order?.buyerId.orEmpty(),

                                    buyerName = order
                                        ?.buyerName
                                        ?.trim()
                                        .orEmpty(),

                                    sellerId =
                                        order?.sellerId.orEmpty(),

                                    sellerName = order
                                        ?.sellerName
                                        ?.trim()
                                        .orEmpty()
                                )
                            }
                            .sortedByDescending {
                                it.incident.createdAt
                            }

                    val totalCount =
                        enrichedIncidents.size

                    /*
                     * URGENT se interpreta como OPEN por
                     * compatibilidad con documentos anteriores.
                     */
                    val openCount =
                        enrichedIncidents.count {
                            it.effectiveStatus ==
                                    IncidentStatus.OPEN
                        }

                    val inReviewCount =
                        enrichedIncidents.count {
                            it.effectiveStatus ==
                                    IncidentStatus.IN_REVIEW
                        }

                    _uiState.update { currentState ->
                        currentState.copy(
                            allIncidents =
                                enrichedIncidents,

                            totalCount = totalCount,
                            openCount = openCount,
                            inReviewCount =
                                inReviewCount,

                            isLoading = false,
                            errorMessage = null
                        )
                    }

                    updateVisibleIncidents()
                }
        }
    }

    private fun updateVisibleIncidents() {
        val currentState =
            _uiState.value

        val filteredIncidents =
            currentState
                .allIncidents
                .applyIncidentFilter(
                    currentState.selectedFilter
                )
                .searchIncidents(
                    currentState.searchQuery
                )
                .sortedByDescending {
                    it.incident.createdAt
                }

        val visibleIncidents =
            filteredIncidents.take(
                currentState.visibleLimit
            )

        _uiState.update {
            it.copy(
                visibleIncidents =
                    visibleIncidents,

                hasMoreIncidents =
                    filteredIncidents.size >
                            visibleIncidents.size
            )
        }
    }

    override fun onCleared() {
        incidentsJob?.cancel()
        super.onCleared()
    }

    private companion object {
        const val INITIAL_VISIBLE_LIMIT = 5
        const val PAGE_SIZE = 5
    }
}