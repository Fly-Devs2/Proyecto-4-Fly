package ucenfotec.ac.cr.flydevs.presentation.myCollection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.repository.ICardCatalogRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository

class MyCollectionViewModel(
    private val repository: ICardCatalogRepository,
    private val authRepository: IAuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyCollectionUiState())
    val uiState: StateFlow<MyCollectionUiState> = _uiState.asStateFlow()

    init { loadCards() }

    fun loadCards() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                val uid = authRepository.getCurrentUserUid() ?: ""
                repository.getCardCatalog().filter { it.sellerId == uid }
            }
                .onSuccess { cards ->
                    _uiState.value = _uiState.value.copy(isLoading = false, cards = cards)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "No se pudo cargar el catálogo",
                    )
                }
        }
    }

    fun setFilter(filter: CollectionFilter) {
        _uiState.value = _uiState.value.copy(activeFilter = filter)
    }

    fun requestDelete(cardId: String) {
        _uiState.value = _uiState.value.copy(pendingDeleteCardId = cardId)
    }

    fun cancelDelete() {
        _uiState.value = _uiState.value.copy(pendingDeleteCardId = null, deleteError = null)
    }

    fun confirmDelete() {
        _uiState.value = _uiState.value.copy(pendingDeleteCardId = null)
    }
}
