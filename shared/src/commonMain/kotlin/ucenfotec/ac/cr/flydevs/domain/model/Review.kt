package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val id: String = "",

    val orderId: String = "",

    val reviewerId: String = "",
    val reviewerRole: ReviewRole = ReviewRole.BUYER,

    val reviewedUserId: String = "",
    val reviewedRole: ReviewRole = ReviewRole.SELLER,

    val rating: Double = 0.0,

    /**
     * Se mantiene como String vacío cuando no existe comentario.
     * Esto simplifica la serialización y las reglas de Firestore.
     */
    val comment: String = "",

    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val isActive: Boolean = true
)