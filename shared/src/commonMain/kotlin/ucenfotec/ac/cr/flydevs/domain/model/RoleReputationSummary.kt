package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RoleReputationSummary(
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0,
    val commentCount: Int = 0,
    val completedTransactionCount: Int = 0, // Count for specific role (Buyer/Seller)
    val salesCount: Int = 0, // Count for status PICKED_UP (Only for Seller role)

    val fiveStarCount: Int = 0,
    val fourStarCount: Int = 0,
    val threeStarCount: Int = 0,
    val twoStarCount: Int = 0,
    val oneStarCount: Int = 0
) {
    fun countForStars(stars: Int): Int {
        return when (stars) {
            5 -> fiveStarCount
            4 -> fourStarCount
            3 -> threeStarCount
            2 -> twoStarCount
            1 -> oneStarCount
            else -> 0
        }
    }
}

fun UserRatingSummary.toRoleSummary(
    role: ReviewRole,
    timeframe: ReputationTimeframe = ReputationTimeframe.ALL_TIME
): RoleReputationSummary {
    val summary = when (timeframe) {
        ReputationTimeframe.LAST_30_DAYS -> last30Days
        ReputationTimeframe.LAST_YEAR -> lastYear
        ReputationTimeframe.ALL_TIME -> allTime
    }

    return when (role) {
        ReviewRole.SELLER -> {
            RoleReputationSummary(
                averageRating = summary.sellerAverageRating,
                reviewCount = summary.sellerReviewCount,
                commentCount = summary.sellerCommentCount,
                completedTransactionCount = summary.sellerCompletedTransactionCount,
                salesCount = summary.sellerSalesCount,
                fiveStarCount = summary.sellerFiveStarCount,
                fourStarCount = summary.sellerFourStarCount,
                threeStarCount = summary.sellerThreeStarCount,
                twoStarCount = summary.sellerTwoStarCount,
                oneStarCount = summary.sellerOneStarCount
            )
        }

        ReviewRole.BUYER -> {
            RoleReputationSummary(
                averageRating = summary.buyerAverageRating,
                reviewCount = summary.buyerReviewCount,
                commentCount = summary.buyerCommentCount,
                completedTransactionCount = summary.buyerCompletedTransactionCount,
                salesCount = 0, // Only sellers have sales
                fiveStarCount = summary.buyerFiveStarCount,
                fourStarCount = summary.buyerFourStarCount,
                threeStarCount = summary.buyerThreeStarCount,
                twoStarCount = summary.buyerTwoStarCount,
                oneStarCount = summary.buyerOneStarCount
            )
        }
    }
}
