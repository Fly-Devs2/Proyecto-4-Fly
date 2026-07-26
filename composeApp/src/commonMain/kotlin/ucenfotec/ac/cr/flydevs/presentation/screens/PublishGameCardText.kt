package ucenfotec.ac.cr.flydevs.presentation.screens

import ucenfotec.ac.cr.flydevs.domain.validation.GameCardValidationError
import ucenfotec.ac.cr.flydevs.presentation.publishGameCard.ImageError
import ucenfotec.ac.cr.flydevs.presentation.publishGameCard.PublishCardUiState
import ucenfotec.ac.cr.flydevs.presentation.publishGameCard.PublishFeedback

internal fun photoTitle(state: PublishCardUiState): String = when {
    state.isUploadingImages -> "Subiendo fotos..."
    state.pendingImages.isNotEmpty() -> "${state.pendingImages.size} foto(s) lista(s) ✓"
    else -> "Agregar fotos de la carta"
}

internal fun photoSubtitle(state: PublishCardUiState): String = when {
    state.isUploadingImages -> "Espera un momento"
    state.pendingImages.isNotEmpty() -> "Mantén presionado y arrastra para reordenar"
    else -> "Toma una o varias fotos"
}

internal fun expansionPlaceholder(state: PublishCardUiState): String = when {
    state.game == null -> "Elige un juego primero"
    state.isLoadingExpansions -> "Cargando expansiones..."
    else -> "Seleccionar expansión"
}

internal fun rarityPlaceholder(state: PublishCardUiState): String = when {
    state.game == null -> "Elige un juego primero"
    state.isLoadingRarities -> "Cargando rarezas..."
    else -> "Seleccionar rareza"
}

internal fun publishButtonText(state: PublishCardUiState): String = when {
    state.isUploadingImages -> "Subiendo fotos..."
    state.isLoading -> "Publicando..."
    else -> "Publicar carta"
}

internal fun feedbackText(feedback: PublishFeedback): String = when (feedback) {
    PublishFeedback.SUCCESS -> "¡Carta publicada en el marketplace!"
    PublishFeedback.MISSING_FIELDS -> "Revisa los campos obligatorios."
    PublishFeedback.PUBLISH_FAILED -> "No se pudo publicar la carta. Revisa tu conexión e intenta de nuevo."
}

internal fun imageErrorText(error: ImageError): String = when (error) {
    ImageError.UPLOAD_FAILED -> "No se pudo subir la imagen. Revisa tu conexión e intenta de nuevo."
    ImageError.REQUIRED -> "Agrega una foto de la carta"
}

internal fun missingFieldsText(errors: List<GameCardValidationError>): String =
    "Para publicar, agrega: ${errors.joinToString(", ") { it.fieldLabel() }}."

private fun GameCardValidationError.fieldLabel(): String = when (this) {
    GameCardValidationError.NAME_REQUIRED -> "nombre"
    GameCardValidationError.GAME_REQUIRED -> "tipo de juego"
    GameCardValidationError.EXPANSION_REQUIRED -> "expansión"
    GameCardValidationError.RARITY_REQUIRED -> "rareza"
    GameCardValidationError.PRICE_NOT_POSITIVE -> "precio"
    GameCardValidationError.IMAGE_REQUIRED -> "foto"
    GameCardValidationError.SOURCE_STORE_REQUIRED -> "tienda de origen"
}
