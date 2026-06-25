package ucenfotec.ac.cr.flydevs.presentation.publishGameCard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.repository.CardCatalogRepository

class CardCatalogViewModel(private val repository: CardCatalogRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CardCatalogUiState())
    val uiState: StateFlow<CardCatalogUiState> = _uiState.asStateFlow()

    init {
        loadCards()
    }

    fun loadCards() {
        viewModelScope.launch {
            println("DEBUG_CATALOG: ViewModel loadCards started")

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val cards = repository.getCardCatalog()

                println("DEBUG_CATALOG: ViewModel received cards: ${cards.size}")

                val availableCards = cards.filter {
                    it.status.trim().equals("available", ignoreCase = true)
                }

                println("DEBUG_CATALOG: Available cards: ${availableCards.size}")

                _uiState.value = CardCatalogUiState(
                    isLoading = false,
                    cards = availableCards,
                    errorMessage = null
                )
            } catch (e: Exception) {
                println("DEBUG_CATALOG: Error type: ${e::class.simpleName}")
                println("DEBUG_CATALOG: Error message: ${e.message}")
                e.printStackTrace()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error loading cards"
                )
            }
        }
    }
}