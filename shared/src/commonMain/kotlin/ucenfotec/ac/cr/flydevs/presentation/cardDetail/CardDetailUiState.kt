package ucenfotec.ac.cr.flydevs.presentation.cardDetail

import ucenfotec.ac.cr.flydevs.domain.model.GameCard

data class CardDetailUiState(
    val isLoading: Boolean = false,
    val card: GameCard? = null,
    val isFavorite: Boolean = false,
    val errorMessage: String? = null,
    val idCopied: Boolean = false,
    val addedToEnvelope: Boolean = false,
    val reserved: Boolean = false,
)
