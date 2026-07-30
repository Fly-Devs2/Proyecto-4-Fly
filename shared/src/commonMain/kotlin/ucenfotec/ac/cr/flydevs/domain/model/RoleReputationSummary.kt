package ucenfotec.ac.cr.flydevs.domain.model

data class RoleReputationSummary(
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0,
    val commentCount: Int = 0,
    val completedTransactionCount: Int = 0,

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
    role: ReviewRole
): RoleReputationSummary {
    return when (role) {
        ReviewRole.SELLER -> {
            RoleReputationSummary(
                averageRating = sellerAverageRating,
                reviewCount = sellerReviewCount,
                commentCount = sellerCommentCount,
                completedTransactionCount =
                    sellerCompletedTransactionCount,

                fiveStarCount = sellerFiveStarCount,
                fourStarCount = sellerFourStarCount,
                threeStarCount = sellerThreeStarCount,
                twoStarCount = sellerTwoStarCount,
                oneStarCount = sellerOneStarCount
            )
        }

        ReviewRole.BUYER -> {
            RoleReputationSummary(
                averageRating = buyerAverageRating,
                reviewCount = buyerReviewCount,
                commentCount = buyerCommentCount,
                completedTransactionCount =
                    buyerCompletedTransactionCount,

                fiveStarCount = buyerFiveStarCount,
                fourStarCount = buyerFourStarCount,
                threeStarCount = buyerThreeStarCount,
                twoStarCount = buyerTwoStarCount,
                oneStarCount = buyerOneStarCount
            )
        }
    }
}