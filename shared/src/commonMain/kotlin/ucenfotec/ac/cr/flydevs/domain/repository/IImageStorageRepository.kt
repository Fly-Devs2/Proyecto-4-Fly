package ucenfotec.ac.cr.flydevs.domain.repository

import ucenfotec.ac.cr.flydevs.domain.model.PickedImage

interface IImageStorageRepository {

    suspend fun uploadCardImage(image: PickedImage): String
    suspend fun uploadImage(image: PickedImage, folder: String): String
}
