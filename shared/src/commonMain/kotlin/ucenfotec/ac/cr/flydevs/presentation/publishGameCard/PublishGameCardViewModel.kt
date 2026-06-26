package ucenfotec.ac.cr.flydevs.presentation.publishGameCard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.model.CardCondition
import ucenfotec.ac.cr.flydevs.domain.model.CardGame
import ucenfotec.ac.cr.flydevs.domain.model.CardLanguage
import ucenfotec.ac.cr.flydevs.domain.model.PickedImage
import ucenfotec.ac.cr.flydevs.domain.repository.IExpansionRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IGameCardRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IImageStorageRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IRarityRepository
import ucenfotec.ac.cr.flydevs.domain.validation.GameCardValidationError
import ucenfotec.ac.cr.flydevs.domain.validation.GameCardValidator

// TODO(auth): reemplazar por el id del vendedor autenticado cuando exista sesión.
private const val TEMP_SELLER_ID = "seller-demo"

class PublishGameCardViewModel(
    private val repository: IGameCardRepository,
    private val imageStorage: IImageStorageRepository,
    private val rarityRepository: IRarityRepository,
    private val expansionRepository: IExpansionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublishCardUiState())
    val uiState: StateFlow<PublishCardUiState> = _uiState.asStateFlow()

    private fun updateForm(transform: (PublishCardUiState) -> PublishCardUiState) =
        _uiState.update { transform(it).copy(feedback = null) }

    fun onNameChange(value: String) = updateForm { it.copy(name = value) }

    fun onGameChange(value: CardGame) {
        updateForm {
            it.copy(
                game = value,
                expansion = null, expansionOptions = emptyList(), isLoadingExpansions = true,
                rarity = null, rarityOptions = emptyList(), isLoadingRarities = true,
            )
        }

        viewModelScope.launch {
            val options = runCatching { expansionRepository.getExpansions(value) }
                .getOrElse { error ->
                    println("[PublishGameCard] Falló la carga de expansiones: ${error.message}")
                    emptyList()
                }
            _uiState.update {
                if (it.game == value) it.copy(expansionOptions = options, isLoadingExpansions = false) else it
            }
        }
        viewModelScope.launch {
            val options = runCatching { rarityRepository.getRarities(value) }
                .getOrElse { error ->
                    println("[PublishGameCard] Falló la carga de rarezas: ${error.message}")
                    emptyList()
                }
            _uiState.update {
                if (it.game == value) it.copy(rarityOptions = options, isLoadingRarities = false) else it
            }
        }
    }

    fun onRarityChange(value: String) = updateForm { it.copy(rarity = value) }
    fun onExpansionChange(value: String) = updateForm { it.copy(expansion = value) }
    fun onConditionChange(value: CardCondition) = updateForm { it.copy(condition = value) }
    fun onLanguageChange(value: CardLanguage) = updateForm { it.copy(language = value) }
    fun onPriceChange(value: String) = updateForm { it.copy(price = value.filter(Char::isDigit)) }
    fun onDescriptionChange(value: String) = updateForm { it.copy(description = value) }
    fun increaseQuantity() = updateForm { it.copy(quantity = it.quantity + 1) }
    fun decreaseQuantity() = updateForm { it.copy(quantity = (it.quantity - 1).coerceAtLeast(1)) }

    fun onImagePicked(image: PickedImage) {
        if (_uiState.value.isUploadingImage) return

        _uiState.update {
            it.copy(
                isUploadingImage = true,
                imageError = null,
                imageUrl = null,
                feedback = null,
            )
        }

        viewModelScope.launch {
            runCatching { imageStorage.uploadCardImage(image) }
                .onSuccess { url ->
                    _uiState.update { it.copy(isUploadingImage = false, imageUrl = url) }
                }
                .onFailure { error ->
                    println("[PublishGameCard] Falló la subida de imagen: ${error.message}")
                    _uiState.update {
                        it.copy(isUploadingImage = false, imageError = ImageError.UPLOAD_FAILED)
                    }
                }
        }
    }

    fun publish() {
        val current = _uiState.value
        if (current.isLoading || current.isUploadingImage) return

        // Validación de reglas de negocio
        val draft = current.toDraftCard()
        val errors = GameCardValidator.validate(draft)
        if (errors.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    imageError = if (GameCardValidationError.IMAGE_REQUIRED in errors) ImageError.REQUIRED else null,
                    feedback = PublishFeedback.MISSING_FIELDS,
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, feedback = null) }

        val card = draft.copy(sellerId = TEMP_SELLER_ID)

        viewModelScope.launch {
            runCatching { repository.saveGameCard(card) }
                .onSuccess {
                    // Reset del formulario
                    _uiState.value = PublishCardUiState(feedback = PublishFeedback.SUCCESS)
                }
                .onFailure { error ->
                    println("[PublishGameCard] Falló la publicación: ${error.message}")
                    _uiState.update { it.copy(isLoading = false, feedback = PublishFeedback.PUBLISH_FAILED) }
                }
        }
    }
}
