package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import ucenfotec.ac.cr.flydevs.domain.model.FlatUserRatingSummary
import ucenfotec.ac.cr.flydevs.domain.model.toNestedSummary
import ucenfotec.ac.cr.flydevs.domain.model.Order
import ucenfotec.ac.cr.flydevs.domain.model.Review
import ucenfotec.ac.cr.flydevs.domain.model.ReviewEligibility
import ucenfotec.ac.cr.flydevs.domain.model.ReviewRole
import ucenfotec.ac.cr.flydevs.domain.model.TimeframeSummary
import ucenfotec.ac.cr.flydevs.domain.model.UserRatingSummary
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IReviewRepository
import ucenfotec.ac.cr.flydevs.domain.validation.ReviewValidator.MAX_REVIEW_COMMENT_LENGTH
import ucenfotec.ac.cr.flydevs.domain.validation.ReviewValidator.isValidReviewRating
import ucenfotec.ac.cr.flydevs.presentation.util.getCurrentTimeMillis

class ReviewRepositoryImpl(
    private val authRepository: IAuthRepository,
    private val firestore: FirebaseFirestore,

): IReviewRepository {
    private val reviewsCollection =
       firestore.collection("reviews")

    private val ordersCollection =
       firestore.collection("orders")

    private val userRatingsCollection =
       firestore.collection("user_ratings")

    private fun reviewDebug(message: String) {
        println("REVIEW_DEBUG | $message")
    }



    private fun buildReviewId(
        orderId: String,
        reviewerId: String
    ): String {
        return "${orderId}_${reviewerId}"
    }
    override fun observeReview(
        orderId: String
    ): Flow<Review?> = flow {
        reviewDebug("========== OBSERVE REVIEW START ==========")
        reviewDebug("Order ID: $orderId")

        val currentUserId =
            requireCurrentUserId()

        val reviewId =
            buildReviewId(
                orderId = orderId,
                reviewerId = currentUserId
            )

        reviewDebug("Authenticated UID: $currentUserId")
        reviewDebug("Generated reviewId: $reviewId")
        reviewDebug(
            "Reading Firestore path: reviews/$reviewId"
        )

        emitAll(
            reviewsCollection
                .document(reviewId)
                .snapshots
                .map { snapshot ->
                    reviewDebug(
                        "Review snapshot received. Exists: ${snapshot.exists}"
                    )

                    if (snapshot.exists) {
                        val review =
                            snapshot.data<Review>()

                        reviewDebug(
                            "Existing review rating: ${review.rating}"
                        )
                        reviewDebug(
                            "Existing review author: ${review.reviewerId}"
                        )

                        review
                    } else {
                        reviewDebug(
                            "No review exists for this order and user."
                        )
                        null
                    }
                }
                .catch { exception ->
                    reviewDebug(
                        "OBSERVE REVIEW ERROR: ${exception::class.simpleName}"
                    )
                    reviewDebug(
                        "OBSERVE REVIEW message: ${exception.message}"
                    )
                    exception.printStackTrace()

                    throw exception
                }
        )
    }


    override suspend fun getReviewEligibility(
        orderId: String
    ): ReviewEligibility {
        return try {
            reviewDebug("========== ELIGIBILITY START ==========")
            reviewDebug("Order ID received: $orderId")
            val currentUserId = requireCurrentUserId()

            reviewDebug(
                "Reading Firestore document: orders/$orderId"
            )
            val orderSnapshot = ordersCollection
                .document(orderId)
                .get()

            reviewDebug(
                "Order snapshot exists: ${orderSnapshot.exists}"
            )

            if (!orderSnapshot.exists) {
                reviewDebug("Eligibility result: InvalidOrder")
                return ReviewEligibility.InvalidOrder
            }

            val order = orderSnapshot.data<Order>()
            reviewDebug("Order model loaded:")
            reviewDebug("order.id: ${order.id}")
            reviewDebug("order.buyerId: ${order.buyerId}")
            reviewDebug("order.sellerId: ${order.sellerId}")
            reviewDebug("order.sinpePaid: ${order.sinpePaid}")
            reviewDebug("currentUserId: $currentUserId")

            val isBuyer =
                currentUserId == order.buyerId


            val isSeller =
                currentUserId == order.sellerId
            reviewDebug("currentUserId == buyerId: $isBuyer")
            reviewDebug("currentUserId == sellerId: $isSeller")

            if (!isBuyer && !isSeller) {
                return ReviewEligibility.UserNotParticipant
            }

            if (!order.sinpePaid) {
                reviewDebug(
                    "Eligibility result: SinpeNotPaid"
                )
                return ReviewEligibility.SinpeNotPaid
            }

            reviewDebug("Eligibility result: Available")
            reviewDebug("========== ELIGIBILITY END ==========")

            ReviewEligibility.Available
        } catch (exception: Exception) {
            ReviewEligibility.InvalidOrder
        }
    }
    override suspend fun saveReview(
        orderId: String,
        rating: Double,
        comment: String,
        existingCreatedAt: Long?
    ): Result<Unit> {
        return try {
            reviewDebug("========== SAVE REVIEW START ==========")
            reviewDebug("Order ID received: $orderId")
            reviewDebug("Rating received: $rating")
            reviewDebug("Comment length: ${comment.length}")

            val currentUserId =
                requireCurrentUserId()

            reviewDebug(
                "Authenticated UID: $currentUserId"
            )

            if (!rating.isValidReviewRating()) {
                reviewDebug(
                    "Invalid rating. Save stopped."
                )

                return Result.failure(
                    IllegalArgumentException(
                        "La calificación debe estar entre 1 y 5, " +
                                "en incrementos de 0.5."
                    )
                )
            }

            val sanitizedComment =
                comment.trim()

            reviewDebug(
                "Sanitized comment length: ${sanitizedComment.length}"
            )

            if (
                sanitizedComment.length >
                MAX_REVIEW_COMMENT_LENGTH
            ) {
                reviewDebug(
                    "Comment exceeds maximum length."
                )

                return Result.failure(
                    IllegalArgumentException(
                        "El comentario no puede superar " +
                                "$MAX_REVIEW_COMMENT_LENGTH caracteres."
                    )
                )
            }

            reviewDebug(
                "Reading Firestore path: orders/$orderId"
            )

            val orderSnapshot =
                ordersCollection
                    .document(orderId)
                    .get()

            reviewDebug(
                "Order exists: ${orderSnapshot.exists}"
            )

            if (!orderSnapshot.exists) {
                return Result.failure(
                    IllegalStateException(
                        "El pedido no existe."
                    )
                )
            }

            val order =
                orderSnapshot.data<Order>()

            reviewDebug("Order data:")
            reviewDebug("order.id: ${order.id}")
            reviewDebug("order.buyerId: ${order.buyerId}")
            reviewDebug("order.sellerId: ${order.sellerId}")
            reviewDebug("order.sinpePaid: ${order.sinpePaid}")
            reviewDebug("order.status: ${order.status}")

            reviewDebug(
                "Auth UID matches buyerId: " +
                        (currentUserId == order.buyerId)
            )

            reviewDebug(
                "Auth UID matches sellerId: " +
                        (currentUserId == order.sellerId)
            )

            if (!order.sinpePaid) {
                reviewDebug(
                    "SINPE is not paid. Save stopped."
                )

                return Result.failure(
                    IllegalStateException(
                        "Debes completar el pago por SINPE " +
                                "antes de calificar."
                    )
                )
            }

            val reviewerRole: ReviewRole
            val reviewedUserId: String
            val reviewedRole: ReviewRole

            when (currentUserId) {
                order.buyerId -> {
                    reviewerRole = ReviewRole.BUYER
                    reviewedUserId = order.sellerId
                    reviewedRole = ReviewRole.SELLER

                    reviewDebug(
                        "Current user identified as BUYER."
                    )
                }

                order.sellerId -> {
                    reviewerRole = ReviewRole.SELLER
                    reviewedUserId = order.buyerId
                    reviewedRole = ReviewRole.BUYER

                    reviewDebug(
                        "Current user identified as SELLER."
                    )
                }

                else -> {
                    reviewDebug(
                        "ERROR: Authenticated UID is not part of the order."
                    )

                    return Result.failure(
                        IllegalStateException(
                            "El usuario autenticado no pertenece " +
                                    "a este pedido."
                        )
                    )
                }
            }

            val reviewId =
                buildReviewId(
                    orderId = orderId,
                    reviewerId = currentUserId
                )

            val expectedReviewId =
                "${orderId}_${currentUserId}"

            reviewDebug("Generated reviewId: $reviewId")
            reviewDebug("Expected reviewId: $expectedReviewId")
            reviewDebug(
                "Review IDs match: " +
                        (reviewId == expectedReviewId)
            )

            val reviewDocument =
                reviewsCollection.document(reviewId)

            val currentTime =
                getCurrentTimeMillis()

            val createdAt =
                existingCreatedAt ?: currentTime

            reviewDebug(
                "Review mode: " +
                        if (existingCreatedAt == null) {
                            "CREATE"
                        } else {
                            "UPDATE"
                        }
            )

            reviewDebug("createdAt used: $createdAt")
            reviewDebug("updatedAt used: $currentTime")

            val review =
                Review(
                    id = reviewId,
                    orderId = orderId,
                    reviewerId = currentUserId,
                    reviewerRole = reviewerRole,
                    reviewedUserId = reviewedUserId,
                    reviewedRole = reviewedRole,
                    rating = rating,
                    isActive = true,
                    comment = sanitizedComment,
                    createdAt = createdAt,
                    updatedAt = currentTime
                )

            reviewDebug("Review document to save:")
            reviewDebug("id: ${review.id}")
            reviewDebug("orderId: ${review.orderId}")
            reviewDebug("reviewerId: ${review.reviewerId}")
            reviewDebug("reviewerRole: ${review.reviewerRole}")
            reviewDebug("reviewedUserId: ${review.reviewedUserId}")
            reviewDebug("reviewedRole: ${review.reviewedRole}")
            reviewDebug("rating: ${review.rating}")
            reviewDebug("comment length: ${review.comment.length}")
            reviewDebug("createdAt: ${review.createdAt}")
            reviewDebug("updatedAt: ${review.updatedAt}")

            reviewDebug("Rule comparison values:")
            reviewDebug(
                "reviewerId == auth UID: " +
                        (review.reviewerId == currentUserId)
            )
            reviewDebug(
                "reviewId == review.id: " +
                        (reviewId == review.id)
            )
            reviewDebug(
                "createdAt == updatedAt for create: " +
                        (review.createdAt == review.updatedAt)
            )
            reviewDebug(
                "rating valid: " +
                        review.rating.isValidReviewRating()
            )
            reviewDebug(
                "comment <= 500: " +
                        (review.comment.length <=
                                MAX_REVIEW_COMMENT_LENGTH)
            )

            reviewDebug(
                "Attempting Firestore write: reviews/$reviewId"
            )

            try {
                reviewDocument.set(review)

                reviewDebug(
                    "Firestore write SUCCESS."
                )
            } catch (firestoreException: Exception) {
                reviewDebug(
                    "Firestore write FAILED."
                )
                reviewDebug(
                    "Exception type: " +
                            firestoreException::class.simpleName
                )
                reviewDebug(
                    "Exception message: " +
                            firestoreException.message
                )

                firestoreException.printStackTrace()

                throw firestoreException
            }

            reviewDebug("========== SAVE REVIEW END ==========")

            Result.success(Unit)
        } catch (exception: Exception) {
            reviewDebug(
                "SAVE REVIEW FINAL ERROR: " +
                        exception::class.simpleName
            )
            reviewDebug(
                "SAVE REVIEW FINAL MESSAGE: " +
                        exception.message
            )

            exception.printStackTrace()

            Result.failure(exception)
        }
    }
    private suspend fun requireCurrentUserId(): String {
        val currentUserId = authRepository.getCurrentUserUid()
        reviewDebug(
            "Authenticated UID returned by IAuthRepository: $currentUserId"
        )
        return currentUserId
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException(
                "No existe un usuario autenticado."
            )
    }

    override fun observeUserRatingSummary(
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
        println("REPUTATION_DEBUG | (ReviewRepo) Document ID: ${snapshot.id}")

        return try {
            val flatSummary = snapshot.data<FlatUserRatingSummary>()
            val nested = flatSummary.toNestedSummary()

            println("REPUTATION_DEBUG | (ReviewRepo) Flat Mapping SUCCESS")
            println("REPUTATION_DEBUG | (ReviewRepo) AllTime Rating: ${nested.allTime.sellerAverageRating}")

            nested
        } catch (e: Exception) {
            println("REPUTATION_DEBUG | (ReviewRepo) Flat Mapping FAILED: ${e.message}")
            e.printStackTrace()
            UserRatingSummary(userId = snapshot.id)
        }
    }

}