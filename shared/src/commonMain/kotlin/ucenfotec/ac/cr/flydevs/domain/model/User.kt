package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val uid: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String = UserRole.USER.name, // Default role as seen in your theme tokens
    val storeId: String? = null,
    val storeName: String? = null,
    val createdAt: Long = 0L

){
    val userRole: UserRole
        get() = UserRole.fromFirestore(role)
}

