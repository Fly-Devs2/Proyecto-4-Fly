package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ucenfotec.ac.cr.flydevs.domain.model.ReputationTimeframe
import ucenfotec.ac.cr.flydevs.domain.model.ReputationReviewItem
import ucenfotec.ac.cr.flydevs.domain.model.ReputationReviewsPage
import ucenfotec.ac.cr.flydevs.domain.model.Review
import ucenfotec.ac.cr.flydevs.domain.model.ReviewRole
import ucenfotec.ac.cr.flydevs.domain.model.User
import ucenfotec.ac.cr.flydevs.domain.model.TimeframeSummary
import ucenfotec.ac.cr.flydevs.domain.model.UserRatingSummary
import ucenfotec.ac.cr.flydevs.domain.repository.IReputationRepository
import ucenfotec.ac.cr.flydevs.getEpochMillis

class ReputationRepositoryImpl(
    private val firestore: FirebaseFirestore
) : IReputationRepository {

    private val usersCollection =
        firestore.collection("users")

    private val reviewsCollection =
        firestore.collection("reviews")

    private val userRatingsCollection =
        firestore.collection("user_ratings")

    override fun observeUser(
        userId: String
    ): Flow<User?> {
        return usersCollection
            .document(userId)
            .snapshots
            .map { snapshot ->
                if (snapshot.exists) {
                    snapshot.data<User>()
                } else {
                    null
                }
            }
    }

    override fun observeRatingSummary(
        userId: String
    ): Flow<UserRatingSummary> {
        return userRatingsCollection
            .document(userId)
            .snapshots
            .map { snapshot ->
                if (snapshot.exists) {
                    mapDocumentToRatingSummary(snapshot)
                } else {
                    UserRatingSummary(
                        userId = userId
                    )
                }
            }
    }

    private fun mapDocumentToRatingSummary(snapshot: DocumentSnapshot): UserRatingSummary {
        val autoMapped = runCatching { snapshot.data<UserRatingSummary>() }.getOrNull()
        
        // If allTime is empty, it's likely a legacy document or hasn't been updated yet.
        val isLegacy = autoMapped == null || 
                (autoMapped.allTime.sellerReviewCount == 0 && 
                 autoMapped.allTime.buyerReviewCount == 0 && 
                 autoMapped.allTime.sellerCompletedTransactionCount == 0 &&
                 autoMapped.allTime.buyerCompletedTransactionCount == 0)

        if (isLegacy) {
            val sellerTrans = snapshot.getSafeInt("sellerCompletedTransactionCount")
            val buyerTrans = snapshot.getSafeInt("buyerCompletedTransactionCount")
            val sellerSales = snapshot.getSafeInt("sellerSalesCount")
            
            val legacySummary = UserRatingSummary(
                userId = snapshot.id,
                totalCompletedTransactionCount = sellerTrans + buyerTrans,
                totalSalesCount = sellerSales
            )
            
            return legacySummary.copy(
                allTime = TimeframeSummary(
                    sellerAverageRating = snapshot.getSafeDouble("sellerAverageRating"),
                    sellerReviewCount = snapshot.getSafeInt("sellerReviewCount"),
                    sellerCommentCount = snapshot.getSafeInt("sellerCommentCount"),
                    sellerCompletedTransactionCount = sellerTrans,
                    sellerSalesCount = sellerSales,
                    sellerFiveStarCount = snapshot.getSafeInt("sellerFiveStarCount"),
                    sellerFourStarCount = snapshot.getSafeInt("sellerFourStarCount"),
                    sellerThreeStarCount = snapshot.getSafeInt("sellerThreeStarCount"),
                    sellerTwoStarCount = snapshot.getSafeInt("sellerTwoStarCount"),
                    sellerOneStarCount = snapshot.getSafeInt("sellerOneStarCount"),
                    
                    buyerAverageRating = snapshot.getSafeDouble("buyerAverageRating"),
                    buyerReviewCount = snapshot.getSafeInt("buyerReviewCount"),
                    buyerCommentCount = snapshot.getSafeInt("buyerCommentCount"),
                    buyerCompletedTransactionCount = buyerTrans,
                    buyerFiveStarCount = snapshot.getSafeInt("buyerFiveStarCount"),
                    buyerFourStarCount = snapshot.getSafeInt("buyerFourStarCount"),
                    buyerThreeStarCount = snapshot.getSafeInt("buyerThreeStarCount"),
                    buyerTwoStarCount = snapshot.getSafeInt("buyerTwoStarCount"),
                    buyerOneStarCount = snapshot.getSafeInt("buyerOneStarCount")
                ),
                updatedAt = snapshot.getSafeLong("updatedAt")
            )
        }

        return autoMapped ?: UserRatingSummary(userId = snapshot.id)
    }

    private fun DocumentSnapshot.getSafeInt(field: String): Int =
        runCatching { get<Int>(field) }.getOrElse { 
            // Handle cases where it might be stored as Long
            runCatching { get<Long>(field).toInt() }.getOrElse { 0 }
        }

    private fun DocumentSnapshot.getSafeLong(field: String): Long =
        runCatching { get<Long>(field) }.getOrElse { 0L }

    private fun DocumentSnapshot.getSafeDouble(field: String): Double =
        runCatching { get<Double>(field) }.getOrElse { 0.0 }

    override suspend fun getReviews(
        userId: String,
        role: ReviewRole,
        limit: Int,
        timeframe: ReputationTimeframe
    ): ReputationReviewsPage {
        require(userId.isNotBlank()) {
            "El identificador del usuario no puede estar vacío."
        }

        require(limit > 0) {
            "El límite de reseñas debe ser mayor que cero."
        }

        /*
         * Solicitamos un documento adicional para saber
         * si debe mostrarse el botón "Ver más".
         */
        val requestedLimit = limit + 1

        val threshold = when (timeframe) {
            ReputationTimeframe.LAST_30_DAYS -> getCurrentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            ReputationTimeframe.LAST_YEAR -> getCurrentTimeMillis() - (365L * 24 * 60 * 60 * 1000)
            ReputationTimeframe.ALL_TIME -> 0L
        }

        val snapshot = reviewsCollection
            .where {
                "reviewedUserId" equalTo userId
            }
            .where {
                "reviewedRole" equalTo role.name
            }
            .where {
                "isActive" equalTo true
            }
            .get()

        val reviewDocuments =
            snapshot.documents

        /*
         * Filtramos en memoria para ser resilientes a documentos antiguos
         * que pueden no tener el campo updatedAt o usar createdAt.
         */
        val allMatchingReviews = reviewDocuments
            .map { it.data<Review>() }
            .filter { review ->
                if (threshold <= 0) true
                else {
                    val timestamp = if (review.updatedAt > 0) review.updatedAt else review.createdAt
                    timestamp >= threshold
                }
            }
            .sortedByDescending { if (it.updatedAt > 0) it.updatedAt else it.createdAt }

        val hasMore =
            allMatchingReviews.size > limit

        val visibleReviews =
            allMatchingReviews
                .take(limit)

        /*
         * Obtenemos únicamente el nombre público del autor.
         *
         * Firestore devuelve el documento completo de users,
         * pero este repositorio no expone email ni teléfono
         * hacia la interfaz de reputación.
         */
        val reviewerNames =
            loadReviewerNames(
                reviewerIds = visibleReviews
                    .map { it.reviewerId }
                    .distinct()
            )

        val reviewItems =
            visibleReviews.map { review ->
                ReputationReviewItem(
                    review = review,
                    reviewerName =
                        reviewerNames[review.reviewerId]
                            ?: "Usuario"
                )
            }

        return ReputationReviewsPage(
            reviews = reviewItems,
            hasMore = hasMore
        )
    }

    private fun getCurrentTimeMillis(): Long {
        return getEpochMillis()
    }

    private suspend fun loadReviewerNames(
        reviewerIds: List<String>
    ): Map<String, String> {
        val result =
            mutableMapOf<String, String>()

        for (reviewerId in reviewerIds) {
            if (reviewerId.isBlank()) {
                continue
            }

            try {
                val snapshot =
                    usersCollection
                        .document(reviewerId)
                        .get()

                val name =
                    if (snapshot.exists) {
                        snapshot
                            .data<User>()
                            .name
                            .trim()
                            .ifBlank { "Usuario" }
                    } else {
                        "Usuario"
                    }

                result[reviewerId] = name
            } catch (exception: Exception) {
                println(
                    "REPUTATION_DEBUG | " +
                            "Could not load reviewer " +
                            "$reviewerId: ${exception.message}"
                )

                result[reviewerId] = "Usuario"
            }
        }

        return result
    }
}