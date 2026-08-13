package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserRatingSummary(
    val userId: String = "",

    // Global counts (Across all time)
    val totalCompletedTransactionCount: Int = 0, // Sum of Buyer + Seller transactions (SINPE + Evidence)
    val totalSalesCount: Int = 0, // Sum of Seller sales (PICKED_UP)

    // Nested summaries for timeframes
    val allTime: TimeframeSummary = TimeframeSummary(),
    val lastYear: TimeframeSummary = TimeframeSummary(),
    val last30Days: TimeframeSummary = TimeframeSummary(),

    val updatedAt: Long = 0L
)

@Serializable
data class TimeframeSummary(
    // Reputation as Seller
    val sellerAverageRating: Double = 0.0,
    val sellerReviewCount: Int = 0,
    val sellerCommentCount: Int = 0,
    val sellerCompletedTransactionCount: Int = 0, // SINPE + Evidence
    val sellerSalesCount: Int = 0, // PICKED_UP

    val sellerFiveStarCount: Int = 0,
    val sellerFourStarCount: Int = 0,
    val sellerThreeStarCount: Int = 0,
    val sellerTwoStarCount: Int = 0,
    val sellerOneStarCount: Int = 0,

    // Reputation as Buyer
    val buyerAverageRating: Double = 0.0,
    val buyerReviewCount: Int = 0,
    val buyerCommentCount: Int = 0,
    val buyerCompletedTransactionCount: Int = 0, // SINPE + Evidence

    val buyerFiveStarCount: Int = 0,
    val buyerFourStarCount: Int = 0,
    val buyerThreeStarCount: Int = 0,
    val buyerTwoStarCount: Int = 0,
    val buyerOneStarCount: Int = 0
)
