package ucenfotec.ac.cr.flydevs.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLParameter
import io.ktor.http.isSuccess
import kotlin.random.Random
import kotlinx.serialization.Serializable
import ucenfotec.ac.cr.flydevs.data.config.FirebaseStorageConfig
import ucenfotec.ac.cr.flydevs.domain.model.PickedImage
import ucenfotec.ac.cr.flydevs.domain.repository.ImageStorageRepository

/**
 * Sube imágenes a Firebase Storage usando su API REST sobre Ktor, devuelve la URL pública de descarga
 */
class FirebaseStorageImageRepository(
    private val httpClient: HttpClient,
    private val bucket: String = FirebaseStorageConfig.BUCKET,
) : ImageStorageRepository {

    override suspend fun uploadCardImage(image: PickedImage): String {
        val objectPath = "cards/${randomId()}.${image.extension}"
        val encodedPath = objectPath.encodeURLParameter()

        val uploadUrl =
            "https://firebasestorage.googleapis.com/v0/b/$bucket/o?uploadType=media&name=$encodedPath"

        val response = httpClient.post(uploadUrl) {
            contentType(ContentType.parse(image.mimeType))
            setBody(image.bytes)
        }

        if (!response.status.isSuccess()) {
            error("Falló la subida a Firebase Storage (HTTP ${response.status.value})")
        }

        val token = response.body<UploadResponse>().downloadTokens
            ?.substringBefore(',')
            ?: error("Firebase Storage no devolvió token de descarga")

        return "https://firebasestorage.googleapis.com/v0/b/$bucket/o/$encodedPath?alt=media&token=$token"
    }

    private fun randomId(): String =
        Random.nextBytes(16).joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }
}

@Serializable
private data class UploadResponse(
    val name: String? = null,
    val downloadTokens: String? = null,
)
