package ucenfotec.ac.cr.flydevs.presentation.publishGameCard

import ucenfotec.ac.cr.flydevs.domain.model.GameCard

data class CardCatalogUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val cards: List<GameCard> = emptyList(),
)