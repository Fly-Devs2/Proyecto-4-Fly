package ucenfotec.ac.cr.flydevs.presentation.cardDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.repository.ICardCatalogRepository

class CardDetailViewModel(
    private val repository: ICardCatalogRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardDetailUiState())
    val uiState: StateFlow<CardDetailUiState> = _uiState.asStateFlow()

    fun loadCard(cardId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching { repository.getCardCatalog().first { it.id == cardId } }
                .onSuccess { card ->
                    _uiState.value = _uiState.value.copy(isLoading = false, card = card)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "No se pudo cargar la carta",
                    )
                }
        }
    }

    fun toggleFavorite() {
        _uiState.value = _uiState.value.copy(isFavorite = !_uiState.value.isFavorite)
    }

    fun onIdCopied() {
        _uiState.value = _uiState.value.copy(idCopied = true)
    }

    fun clearIdCopied() {
        _uiState.value = _uiState.value.copy(idCopied = false)
    }

    fun addToEnvelope() {
        _uiState.value = _uiState.value.copy(addedToEnvelope = true)
    }

    fun reserveCard() {
        _uiState.value = _uiState.value.copy(reserved = true)
    }
}
