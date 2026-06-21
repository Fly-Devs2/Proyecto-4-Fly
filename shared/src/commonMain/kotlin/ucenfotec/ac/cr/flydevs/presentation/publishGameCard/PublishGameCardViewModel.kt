package ucenfotec.ac.cr.flydevs.presentation.publishGameCard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.model.GameCard
import ucenfotec.ac.cr.flydevs.domain.repository.GameCardRepository

class PublishGameCardViewModel(private val repository: GameCardRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PublishCardUiState())
    val uiState: StateFlow<PublishCardUiState> = _uiState.asStateFlow()

    fun publishGameCard(gameCard: GameCard) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = PublishCardUiState(isLoading = true)

            runCatching {
                repository.saveGameCard(gameCard)
            }.onSuccess {
                _uiState.value = PublishCardUiState(
                    isPublished = true,
                    publishedCardId = gameCard.id,
                )
            }.onFailure { error ->
                _uiState.value = PublishCardUiState(
                    errorMessage = error.message ?: "No se pudo publicar la carta",
                )
            }
        }
    }
}
