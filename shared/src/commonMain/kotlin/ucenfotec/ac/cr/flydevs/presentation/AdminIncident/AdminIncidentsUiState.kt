package ucenfotec.ac.cr.flydevs.presentation.AdminIncident

import ucenfotec.ac.cr.flydevs.domain.model.AdminIncidentFilter
import ucenfotec.ac.cr.flydevs.domain.model.AdminIncidentItem

data class AdminIncidentsUiState(
    val allIncidents: List<AdminIncidentItem> =
        emptyList(),

    val visibleIncidents: List<AdminIncidentItem> =
        emptyList(),

    val searchQuery: String = "",

    val selectedFilter: AdminIncidentFilter =
        AdminIncidentFilter.ALL,

    val visibleLimit: Int = 5,
    val hasMoreIncidents: Boolean = false,

    val totalCount: Int = 0,
    val openCount: Int = 0,
    val inReviewCount: Int = 0,

    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,

    val errorMessage: String? = null
)