package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserRatingSummary(
    val userId: String = "",

    // Reputación como vendedor
    val sellerAverageRating: Double = 0.0,
    val sellerReviewCount: Int = 0,
    val sellerCommentCount: Int = 0,
    val sellerCompletedTransactionCount: Int = 0,

    val sellerFiveStarCount: Int = 0,
    val sellerFourStarCount: Int = 0,
    val sellerThreeStarCount: Int = 0,
    val sellerTwoStarCount: Int = 0,
    val sellerOneStarCount: Int = 0,

    // Reputación como comprador
    val buyerAverageRating: Double = 0.0,
    val buyerReviewCount: Int = 0,
    val buyerCommentCount: Int = 0,
    val buyerCompletedTransactionCount: Int = 0,

    val buyerFiveStarCount: Int = 0,
    val buyerFourStarCount: Int = 0,
    val buyerThreeStarCount: Int = 0,
    val buyerTwoStarCount: Int = 0,
    val buyerOneStarCount: Int = 0,

    val updatedAt: Long = 0L
)
