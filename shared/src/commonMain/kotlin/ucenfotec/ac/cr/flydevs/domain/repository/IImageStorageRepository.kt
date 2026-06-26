package ucenfotec.ac.cr.flydevs.domain.repository

import ucenfotec.ac.cr.flydevs.domain.model.PickedImage

interface IImageStorageRepository {

    suspend fun uploadCardImage(image: PickedImage): String
}
