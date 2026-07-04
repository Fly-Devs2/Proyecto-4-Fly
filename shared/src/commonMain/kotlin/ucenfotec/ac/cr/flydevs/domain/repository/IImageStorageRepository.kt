package ucenfotec.ac.cr.flydevs.domain.repository

import ucenfotec.ac.cr.flydevs.domain.model.PickedImage

interface IImageStorageRepository {

    /** Imágenes de catálogo (publicación de cartas) → carpeta `cards/`. */
    suspend fun uploadCardImage(image: PickedImage): String

    /** Evidencias del flujo de intercambio (foto de entrega, comprobante SINPE) → carpeta `evidence/`. */
    suspend fun uploadEvidenceImage(image: PickedImage): String
    suspend fun uploadImage(image: PickedImage, folder: String): String
}
