package ucenfotec.ac.cr.flydevs.domain.repository

import kotlinx.coroutines.flow.Flow
import ucenfotec.ac.cr.flydevs.domain.model.Review
import ucenfotec.ac.cr.flydevs.domain.model.ReviewEligibility
import ucenfotec.ac.cr.flydevs.domain.model.UserRatingSummary

interface IReviewRepository {
    /**
     * Observa la reseña realizada por un usuario dentro de un pedido.
     * Si todavía no ha calificado, retorna null.
     */
    fun observeReview(
        orderId: String,

    ): Flow<Review?>

    /**
     * Determina si el usuario puede calificar a la otra parte.
     */
    suspend fun getReviewEligibility(
        orderId: String,

    ): ReviewEligibility

    /**
     * Crea o actualiza una reseña.
     *
     * El repositorio obtiene el pedido y determina automáticamente:
     * - quién es el comprador;
     * - quién es el vendedor;
     * - quién debe recibir la reseña.
     */
    suspend fun saveReview(
        orderId: String,
        rating: Double,
        comment: String,
        existingCreatedAt: Long? = null
    ): Result<Unit>

    /**
     * Obtiene los promedios públicos del usuario.
     */
    fun observeUserRatingSummary(
        userId: String
    ): Flow<UserRatingSummary>
}