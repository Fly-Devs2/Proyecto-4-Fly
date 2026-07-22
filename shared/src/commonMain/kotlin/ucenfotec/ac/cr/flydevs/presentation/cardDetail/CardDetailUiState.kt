package ucenfotec.ac.cr.flydevs.presentation.cardDetail

import ucenfotec.ac.cr.flydevs.domain.model.GameCard
import ucenfotec.ac.cr.flydevs.domain.model.User

data class CardDetailUiState(
    val isLoading: Boolean = false,
    val card: GameCard? = null,
    val isFavorite: Boolean = false,
    val errorMessage: String? = null,
    val idCopied: Boolean = false,
    val addedToEnvelope: Boolean = false,
    val reserved: Boolean = false,
    val isAddingToEnvelope: Boolean = false,
    val actionErrorMessage: String? = null,
    val actionSuccessMessage: String? = null,
    val shouldOpenEnvelope: Boolean = false,
    val targetEnvelopeId: String? = null,
    val seller: User? = null,
    val sourceStoreName: String? = null
)
