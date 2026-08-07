package ucenfotec.ac.cr.flydevs.presentation.adminTraceability

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import ucenfotec.ac.cr.flydevs.domain.model.TraceabilityTab
import ucenfotec.ac.cr.flydevs.domain.model.UserTraceability
import ucenfotec.ac.cr.flydevs.domain.repository.ITraceabilityRepository

class AdminTraceabilityViewModel(
    private val traceabilityRepository: ITraceabilityRepository,
    private val userId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminTraceabilityUiState())
    val uiState: StateFlow<AdminTraceabilityUiState> = _uiState.asStateFlow()

    /** Última trazabilidad recibida; cambiar de pestaña no vuelve a consultar Firestore. */
    private var traceability: UserTraceability = UserTraceability()

    init {
        observeTraceability()
    }

    private fun observeTraceability() {
        traceabilityRepository.observeUserTraceability(userId)
            .onEach { result ->
                traceability = result
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        user = result.user,
                        summary = result.summary,
                        records = result.recordsFor(state.selectedTab),
                        errorMessage = null,
                    )
                }
            }
            .catch { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
            .launchIn(viewModelScope)
    }

    fun selectTab(tab: TraceabilityTab) {
        _uiState.update { it.copy(selectedTab = tab, records = traceability.recordsFor(tab)) }
    }
}
