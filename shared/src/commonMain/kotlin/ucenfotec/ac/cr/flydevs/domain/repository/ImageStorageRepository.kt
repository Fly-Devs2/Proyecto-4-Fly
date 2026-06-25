package ucenfotec.ac.cr.flydevs.domain.repository

import ucenfotec.ac.cr.flydevs.domain.model.PickedImage

interface ImageStorageRepository {

    /**
     * Sube la imagen y devuelve la URL pública con la que quedará asociada a la carta.
     */
    suspend fun uploadCardImage(image: PickedImage): String
}
