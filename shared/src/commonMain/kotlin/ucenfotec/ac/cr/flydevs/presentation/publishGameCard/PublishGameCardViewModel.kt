package ucenfotec.ac.cr.flydevs.presentation.publishGameCard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.model.CardCondition
import ucenfotec.ac.cr.flydevs.domain.model.CardGame
import ucenfotec.ac.cr.flydevs.domain.model.CardLanguage
import ucenfotec.ac.cr.flydevs.domain.model.PickedImage
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IExpansionRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IGameCardRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IImageStorageRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IRarityRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IStoreRepository
import ucenfotec.ac.cr.flydevs.domain.validation.GameCardValidationError

class PublishGameCardViewModel(
    private val repository: IGameCardRepository,
    private val imageStorage: IImageStorageRepository,
    private val rarityRepository: IRarityRepository,
    private val expansionRepository: IExpansionRepository,
    private val authRepository: IAuthRepository,
    private val storeRepository: IStoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublishCardUiState())
    val uiState: StateFlow<PublishCardUiState> = _uiState.asStateFlow()

    init {
        loadStores()
    }

    private fun loadStores() {
        viewModelScope.launch {
            val stores = runCatching { storeRepository.getStores() }.getOrElse { emptyList() }
            _uiState.update { it.copy(stores = stores) }
        }
    }

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
    fun onStoreChange(value: ucenfotec.ac.cr.flydevs.domain.model.Store) = updateForm { it.copy(selectedStore = value) }
    fun increaseQuantity() = updateForm { it.copy(quantity = it.quantity + 1) }
    fun decreaseQuantity() = updateForm { it.copy(quantity = (it.quantity - 1).coerceAtLeast(1)) }

    fun onImagePicked(image: PickedImage) {
        _uiState.update {
            it.copy(
                pendingImages = it.pendingImages + image,
                imageError = null,
                feedback = null
            )
        }
    }

    fun removeImage(index: Int) {
        _uiState.update {
            it.copy(
                pendingImages = it.pendingImages.filterIndexed { i, _ -> i != index }
            )
        }
    }

    fun moveImage(fromIndex: Int, toIndex: Int) {
        val current = _uiState.value.pendingImages.toMutableList()
        if (fromIndex >= 0 && fromIndex < current.size && toIndex >= 0 && toIndex < current.size) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            _uiState.update { it.copy(pendingImages = current) }
        }
    }

    fun publish() {
        val current = _uiState.value
        if (current.isLoading) return

        // Validación de reglas de negocio
        val errors = current.validationErrors
        if (errors.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    imageError = if (GameCardValidationError.IMAGE_REQUIRED in errors) ImageError.REQUIRED else null,
                    feedback = PublishFeedback.MISSING_FIELDS,
                )
            }
            return
        }

        if (current.pendingImages.isEmpty()) {
            _uiState.update { it.copy(imageError = ImageError.REQUIRED, feedback = PublishFeedback.MISSING_FIELDS) }
            return
        }

        val currentUid = authRepository.getCurrentUserUid()
        if (currentUid == null) {
            _uiState.update { it.copy(feedback = PublishFeedback.PUBLISH_FAILED) }
            return
        }

        _uiState.update {
            it.copy(isLoading = true, isUploadingImages = true, feedback = null, imageError = null)
        }

        viewModelScope.launch {
            val uploadedUrls = coroutineScope {
                current.pendingImages.mapIndexed { index, image ->
                    async {
                        index to (runCatching { imageStorage.uploadCardImage(image) }
                            .getOrNull())
                    }
                }
                    .map { it.await() }
                    .filter { it.second != null }
                    .sortedBy { it.first }
                    .map { it.second!! }
            }

            if (uploadedUrls.isEmpty()) {
                _uiState.update {
                    it.copy(isLoading = false, isUploadingImages = false, imageError = ImageError.UPLOAD_FAILED)
                }
                return@launch
            }

            _uiState.update { it.copy(isUploadingImages = false) }

            val card = current.toDraftCard(imageUrls = uploadedUrls).copy(sellerId = currentUid)
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
