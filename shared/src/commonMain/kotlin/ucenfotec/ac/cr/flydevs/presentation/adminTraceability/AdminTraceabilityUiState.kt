package ucenfotec.ac.cr.flydevs.presentation.adminTraceability

import ucenfotec.ac.cr.flydevs.domain.model.TraceabilityRecord
import ucenfotec.ac.cr.flydevs.domain.model.TraceabilitySummary
import ucenfotec.ac.cr.flydevs.domain.model.TraceabilityTab
import ucenfotec.ac.cr.flydevs.domain.model.User

data class AdminTraceabilityUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val summary: TraceabilitySummary = TraceabilitySummary(),
    val selectedTab: TraceabilityTab = TraceabilityTab.ORDERS,
    /** Registros de [selectedTab] únicamente. */
    val records: List<TraceabilityRecord> = emptyList(),
    val errorMessage: String? = null,
)
