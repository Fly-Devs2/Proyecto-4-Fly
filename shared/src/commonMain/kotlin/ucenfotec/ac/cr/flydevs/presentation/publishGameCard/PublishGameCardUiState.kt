package ucenfotec.ac.cr.flydevs.presentation.publishGameCard

import ucenfotec.ac.cr.flydevs.domain.model.CardCondition
import ucenfotec.ac.cr.flydevs.domain.model.CardGame
import ucenfotec.ac.cr.flydevs.domain.model.CardLanguage
import ucenfotec.ac.cr.flydevs.domain.model.GameCard
import ucenfotec.ac.cr.flydevs.domain.model.PickedImage
import ucenfotec.ac.cr.flydevs.domain.model.Store
import ucenfotec.ac.cr.flydevs.domain.validation.GameCardValidationError
import ucenfotec.ac.cr.flydevs.domain.validation.GameCardValidator

data class PublishCardUiState(
    // ── Inputs ──
    val name: String = "",
    val game: CardGame? = null,
    val expansion: String? = null,
    val rarity: String? = null,
    val condition: CardCondition = CardCondition.NEAR_MINT,
    val language: CardLanguage = CardLanguage.EN,
    val price: String = "",
    val quantity: Int = 1,
    val description: String = "",

    val expansionOptions: List<String> = emptyList(),
    val isLoadingExpansions: Boolean = false,
    val rarityOptions: List<String> = emptyList(),
    val isLoadingRarities: Boolean = false,

    val stores: List<Store> = emptyList(),
    val selectedStore: Store? = null,

    // ── Imágenes ──
    val pendingImages: List<PickedImage> = emptyList(),
    val isUploadingImages: Boolean = false,
    val imageError: ImageError? = null,

    // ── Proceso de publicación ──
    val isLoading: Boolean = false,
    val feedback: PublishFeedback? = null,
) {

    fun toDraftCard(imageUrls: List<String> = emptyList()): GameCard = GameCard(
        name = name.trim(),
        game = game,
        expansion = expansion,
        rarity = rarity,
        condition = condition,
        language = language,
        price = price.toLongOrNull() ?: 0L,
        quantity = quantity,
        description = description.trim(),
        imageUrls = imageUrls.takeIf { it.isNotEmpty() } ?: emptyList(),
        sourceStore = selectedStore?.id ?: "",
    )

    val validationErrors: List<GameCardValidationError>
        get() = GameCardValidator.validate(
            toDraftCard(imageUrls = if (pendingImages.isNotEmpty()) listOf(LOCAL_IMAGE_MARKER) else emptyList())
        )
}

private const val LOCAL_IMAGE_MARKER = "local"

