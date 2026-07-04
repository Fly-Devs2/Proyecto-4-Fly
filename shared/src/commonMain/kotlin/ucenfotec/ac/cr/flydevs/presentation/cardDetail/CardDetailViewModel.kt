package ucenfotec.ac.cr.flydevs.presentation.cardDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.repository.ICardCatalogRepository
import ucenfotec.ac.cr.flydevs.domain.repository.ICardEnvelopeRepository

class CardDetailViewModel(
    private val repository: ICardCatalogRepository,
    private val cardEnvelopeRepository: ICardEnvelopeRepository
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

    fun addToEnvelope(userId: String,cardId: String) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isAddingToEnvelope = true,
                    actionErrorMessage = null,
                    actionSuccessMessage = null,
                    shouldOpenEnvelope = false
                )
            }
            try {
                cardEnvelopeRepository.addCardToEnvelope(
                    userId = userId,
                    cardId = cardId
                )

                _uiState.update { currentState ->
                    currentState.copy(
                        isAddingToEnvelope = false,
                        addedToEnvelope = true,
                        actionSuccessMessage = "Carta agregada al sobre correctamente.",
                        shouldOpenEnvelope = true
                    )
                }
            } catch (exception: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isAddingToEnvelope = false,
                        actionErrorMessage = exception.message ?: "No se pudo agregar la carta al sobre."
                    )
                }
            }
        }

    }
    fun clearEnvelopeNavigation() {
        _uiState.update { currentState ->
            currentState.copy(
                shouldOpenEnvelope = false,
                actionSuccessMessage = null,
                actionErrorMessage = null
            )
        }
    }


    fun reserveCard() {
        _uiState.value = _uiState.value.copy(reserved = true)
    }
}
