package ucenfotec.ac.cr.flydevs.presentation.publishGameCard

import kotlinx.serialization.Serializable

@Serializable
data class PublishCardUiState(
    val isLoading: Boolean = false,
    val isPublished: Boolean = false,
    val publishedCardId: String? = null,
    val errorMessage: String? = null,
)
