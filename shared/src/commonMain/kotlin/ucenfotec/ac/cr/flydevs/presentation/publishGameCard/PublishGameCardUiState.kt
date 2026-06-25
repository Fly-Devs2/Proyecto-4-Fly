package ucenfotec.ac.cr.flydevs.presentation.publishGameCard

data class PublishCardUiState(
    val name: String = "",
    val expansion: String = "",
    val condition: String = "Near Mint (NM)",
    val language: String = "Inglés (EN)",
    val price: String = "",
    val quantity: Int = 1,
    val description: String = "",
    val imageUrl: String? = null,        // URL pública de descarga una vez subida la foto
    val imageFileName: String? = null,   // nombre del archivo elegido, para feedback
    val isUploadingImage: Boolean = false,

    // ── Errores de validación ──
    val nameError: String? = null,
    val priceError: String? = null,
    val imageError: String? = null,

    // ── Estado del proceso ──
    val isLoading: Boolean = false,
    val isPublished: Boolean = false,
    val publishedCardId: String? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null,
) {
    private val priceValue: Long? get() = price.toLongOrNull()

    /** Lista de obligatorios que aún faltan, para guiar al usuario en vivo. */
    val missingRequired: List<String>
        get() = buildList {
            if (name.isBlank()) add("nombre")
            if ((priceValue ?: 0L) <= 0L) add("precio")
            if (imageUrl == null) add("foto")
        }

    /** Habilita el botón solo cuando no falta ningún obligatorio, nada está subiendo ni cargando. */
    val canPublish: Boolean
        get() = missingRequired.isEmpty() && !isLoading && !isUploadingImage
}
