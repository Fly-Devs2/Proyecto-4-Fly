package ucenfotec.ac.cr.flydevs.presentation.publishGameCard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.model.GameCard
import ucenfotec.ac.cr.flydevs.domain.model.PickedImage
import ucenfotec.ac.cr.flydevs.domain.repository.GameCardRepository
import ucenfotec.ac.cr.flydevs.domain.repository.ImageStorageRepository

// TODO(auth): reemplazar por el id del vendedor autenticado cuando exista sesión.
private const val TEMP_SELLER_ID = "seller-demo"

class PublishGameCardViewModel(
    private val repository: GameCardRepository,
    private val imageStorage: ImageStorageRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublishCardUiState())
    val uiState: StateFlow<PublishCardUiState> = _uiState.asStateFlow()

    // ── Actualización de campos ──
    private fun updateForm(transform: (PublishCardUiState) -> PublishCardUiState) =
        _uiState.update { transform(it).copy(successMessage = null, errorMessage = null) }

    fun onNameChange(value: String) = updateForm { it.copy(name = value, nameError = null) }
    fun onExpansionChange(value: String) = updateForm { it.copy(expansion = value) }
    fun onConditionChange(value: String) = updateForm { it.copy(condition = value) }
    fun onLanguageChange(value: String) = updateForm { it.copy(language = value) }
    fun onPriceChange(value: String) = updateForm { it.copy(price = value.filter(Char::isDigit), priceError = null) }
    fun onDescriptionChange(value: String) = updateForm { it.copy(description = value) }
    fun increaseQuantity() = updateForm { it.copy(quantity = it.quantity + 1) }
    fun decreaseQuantity() = updateForm { it.copy(quantity = (it.quantity - 1).coerceAtLeast(1)) }

    /**
     * El usuario eligió una imagen: la subimos a Firebase Storage y guardamos la URL pública.
     */
    fun onImagePicked(image: PickedImage) {
        if (_uiState.value.isUploadingImage) return

        _uiState.update {
            it.copy(
                isUploadingImage = true,
                imageFileName = image.fileName,
                imageError = null,
                imageUrl = null,
                successMessage = null,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            runCatching { imageStorage.uploadCardImage(image) }
                .onSuccess { url ->
                    _uiState.update { it.copy(isUploadingImage = false, imageUrl = url) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isUploadingImage = false,
                            imageFileName = null,
                            imageError = "No se pudo subir la imagen. Intenta de nuevo.",
                            errorMessage = error.message ?: "Error al subir la imagen.",
                        )
                    }
                }
        }
    }

    fun publish() {
        val current = _uiState.value
        if (current.isLoading || current.isUploadingImage) return

        // ── Validación de los inputs requeridos ──
        val price = current.price.toLongOrNull()
        val nameError = if (current.name.isBlank()) "El nombre es obligatorio" else null
        val priceError = when {
            current.price.isBlank() -> "El precio es obligatorio"
            price == null || price <= 0L -> "Ingresa un precio válido"
            else -> null
        }
        val imageError = if (current.imageUrl == null) "Agrega una foto de la carta" else null

        if (nameError != null || priceError != null || imageError != null) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    priceError = priceError,
                    imageError = imageError,
                    errorMessage = "Revisa los campos obligatorios.",
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        val card = GameCard(
            sellerId = TEMP_SELLER_ID,
            name = current.name.trim(),
            expansion = current.expansion,
            condition = current.condition,
            language = current.language,
            price = price!!,
            quantity = current.quantity,
            description = current.description.trim(),
            imageUrl = current.imageUrl!!,
        )

        viewModelScope.launch {
            runCatching { repository.saveGameCard(card) }
                .onSuccess { savedCard ->
                    // Reset del formulario
                    _uiState.value = PublishCardUiState(
                        isPublished = true,
                        publishedCardId = savedCard.id,
                        successMessage = "¡Carta publicada en el marketplace!",
                    )
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "No se pudo publicar la carta.",
                        )
                    }
                }
        }
    }
}
