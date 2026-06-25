package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val uid: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String = "Usuario", // Default role as seen in your theme tokens
    val createdAt: Long = 0L
)
