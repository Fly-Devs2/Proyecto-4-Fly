package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ucenfotec.ac.cr.flydevs.domain.model.ReputationReviewItem
import ucenfotec.ac.cr.flydevs.domain.model.ReputationReviewsPage
import ucenfotec.ac.cr.flydevs.domain.model.Review
import ucenfotec.ac.cr.flydevs.domain.model.ReviewRole
import ucenfotec.ac.cr.flydevs.domain.model.User
import ucenfotec.ac.cr.flydevs.domain.model.UserRatingSummary
import ucenfotec.ac.cr.flydevs.domain.repository.IReputationRepository

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
                    snapshot.data<UserRatingSummary>()
                } else {
                    UserRatingSummary(
                        userId = userId
                    )
                }
            }
    }

    override suspend fun getReviews(
        userId: String,
        role: ReviewRole,
        limit: Int
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
            .orderBy(
                field = "updatedAt",
                direction = Direction.DESCENDING
            )
            .limit(requestedLimit)
            .get()

        val reviewDocuments =
            snapshot.documents

        val hasMore =
            reviewDocuments.size > limit

        val visibleReviews =
            reviewDocuments
                .take(limit)
                .map { document ->
                    document.data<Review>()
                }

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