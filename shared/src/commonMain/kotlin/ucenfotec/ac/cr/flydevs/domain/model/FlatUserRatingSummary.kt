package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FlatUserRatingSummary(
    val userId: String = "",
    val totalCompletedTransactionCount: Int = 0,
    val totalSalesCount: Int = 0,
    val updatedAt: Long = 0L,

    // ── All Time (Seller) ──
    @SerialName("allTime.sellerAverageRating") val allTime_sellerAverageRating: Double = 0.0,
    @SerialName("allTime.sellerReviewCount") val allTime_sellerReviewCount: Int = 0,
    @SerialName("allTime.sellerCommentCount") val allTime_sellerCommentCount: Int = 0,
    @SerialName("allTime.sellerCompletedTransactionCount") val allTime_sellerCompletedTransactionCount: Int = 0,
    @SerialName("allTime.sellerSalesCount") val allTime_sellerSalesCount: Int = 0,
    @SerialName("allTime.sellerFiveStarCount") val allTime_sellerFiveStarCount: Int = 0,
    @SerialName("allTime.sellerFourStarCount") val allTime_sellerFourStarCount: Int = 0,
    @SerialName("allTime.sellerThreeStarCount") val allTime_sellerThreeStarCount: Int = 0,
    @SerialName("allTime.sellerTwoStarCount") val allTime_sellerTwoStarCount: Int = 0,
    @SerialName("allTime.sellerOneStarCount") val allTime_sellerOneStarCount: Int = 0,

    // ── All Time (Buyer) ──
    @SerialName("allTime.buyerAverageRating") val allTime_buyerAverageRating: Double = 0.0,
    @SerialName("allTime.buyerReviewCount") val allTime_buyerReviewCount: Int = 0,
    @SerialName("allTime.buyerCommentCount") val allTime_buyerCommentCount: Int = 0,
    @SerialName("allTime.buyerCompletedTransactionCount") val allTime_buyerCompletedTransactionCount: Int = 0,
    @SerialName("allTime.buyerFiveStarCount") val allTime_buyerFiveStarCount: Int = 0,
    @SerialName("allTime.buyerFourStarCount") val allTime_buyerFourStarCount: Int = 0,
    @SerialName("allTime.buyerThreeStarCount") val allTime_buyerThreeStarCount: Int = 0,
    @SerialName("allTime.buyerTwoStarCount") val allTime_buyerTwoStarCount: Int = 0,
    @SerialName("allTime.buyerOneStarCount") val allTime_buyerOneStarCount: Int = 0,

    // ── Last Year (Seller) ──
    @SerialName("lastYear.sellerAverageRating") val lastYear_sellerAverageRating: Double = 0.0,
    @SerialName("lastYear.sellerReviewCount") val lastYear_sellerReviewCount: Int = 0,
    @SerialName("lastYear.sellerCommentCount") val lastYear_sellerCommentCount: Int = 0,
    @SerialName("lastYear.sellerCompletedTransactionCount") val lastYear_sellerCompletedTransactionCount: Int = 0,
    @SerialName("lastYear.sellerSalesCount") val lastYear_sellerSalesCount: Int = 0,
    @SerialName("lastYear.sellerFiveStarCount") val lastYear_sellerFiveStarCount: Int = 0,
    @SerialName("lastYear.sellerFourStarCount") val lastYear_sellerFourStarCount: Int = 0,
    @SerialName("lastYear.sellerThreeStarCount") val lastYear_sellerThreeStarCount: Int = 0,
    @SerialName("lastYear.sellerTwoStarCount") val lastYear_sellerTwoStarCount: Int = 0,
    @SerialName("lastYear.sellerOneStarCount") val lastYear_sellerOneStarCount: Int = 0,

    // ── Last Year (Buyer) ──
    @SerialName("lastYear.buyerAverageRating") val lastYear_buyerAverageRating: Double = 0.0,
    @SerialName("lastYear.buyerReviewCount") val lastYear_buyerReviewCount: Int = 0,
    @SerialName("lastYear.buyerCommentCount") val lastYear_buyerCommentCount: Int = 0,
    @SerialName("lastYear.buyerCompletedTransactionCount") val lastYear_buyerCompletedTransactionCount: Int = 0,
    @SerialName("lastYear.buyerFiveStarCount") val lastYear_buyerFiveStarCount: Int = 0,
    @SerialName("lastYear.buyerFourStarCount") val lastYear_buyerFourStarCount: Int = 0,
    @SerialName("lastYear.buyerThreeStarCount") val lastYear_buyerThreeStarCount: Int = 0,
    @SerialName("lastYear.buyerTwoStarCount") val lastYear_buyerTwoStarCount: Int = 0,
    @SerialName("lastYear.buyerOneStarCount") val lastYear_buyerOneStarCount: Int = 0,

    // ── Last 30 Days (Seller) ──
    @SerialName("last30Days.sellerAverageRating") val last30Days_sellerAverageRating: Double = 0.0,
    @SerialName("last30Days.sellerReviewCount") val last30Days_sellerReviewCount: Int = 0,
    @SerialName("last30Days.sellerCommentCount") val last30Days_sellerCommentCount: Int = 0,
    @SerialName("last30Days.sellerCompletedTransactionCount") val last30Days_sellerCompletedTransactionCount: Int = 0,
    @SerialName("last30Days.sellerSalesCount") val last30Days_sellerSalesCount: Int = 0,
    @SerialName("last30Days.sellerFiveStarCount") val last30Days_sellerFiveStarCount: Int = 0,
    @SerialName("last30Days.sellerFourStarCount") val last30Days_sellerFourStarCount: Int = 0,
    @SerialName("last30Days.sellerThreeStarCount") val last30Days_sellerThreeStarCount: Int = 0,
    @SerialName("last30Days.sellerTwoStarCount") val last30Days_sellerTwoStarCount: Int = 0,
    @SerialName("last30Days.sellerOneStarCount") val last30Days_sellerOneStarCount: Int = 0,

    // ── Last 30 Days (Buyer) ──
    @SerialName("last30Days.buyerAverageRating") val last30Days_buyerAverageRating: Double = 0.0,
    @SerialName("last30Days.buyerReviewCount") val last30Days_buyerReviewCount: Int = 0,
    @SerialName("last30Days.buyerCommentCount") val last30Days_buyerCommentCount: Int = 0,
    @SerialName("last30Days.buyerCompletedTransactionCount") val last30Days_buyerCompletedTransactionCount: Int = 0,
    @SerialName("last30Days.buyerFiveStarCount") val last30Days_buyerFiveStarCount: Int = 0,
    @SerialName("last30Days.buyerFourStarCount") val last30Days_buyerFourStarCount: Int = 0,
    @SerialName("last30Days.buyerThreeStarCount") val last30Days_buyerThreeStarCount: Int = 0,
    @SerialName("last30Days.buyerTwoStarCount") val last30Days_buyerTwoStarCount: Int = 0,
    @SerialName("last30Days.buyerOneStarCount") val last30Days_buyerOneStarCount: Int = 0
)

fun FlatUserRatingSummary.toNestedSummary(): UserRatingSummary {
    return UserRatingSummary(
        userId = this.userId,
        totalCompletedTransactionCount = this.totalCompletedTransactionCount,
        totalSalesCount = this.totalSalesCount,
        allTime = TimeframeSummary(
            sellerAverageRating = this.allTime_sellerAverageRating,
            sellerReviewCount = this.allTime_sellerReviewCount,
            sellerCommentCount = this.allTime_sellerCommentCount,
            sellerCompletedTransactionCount = this.allTime_sellerCompletedTransactionCount,
            sellerSalesCount = this.allTime_sellerSalesCount,
            sellerFiveStarCount = this.allTime_sellerFiveStarCount,
            sellerFourStarCount = this.allTime_sellerFourStarCount,
            sellerThreeStarCount = this.allTime_sellerThreeStarCount,
            sellerTwoStarCount = this.allTime_sellerTwoStarCount,
            sellerOneStarCount = this.allTime_sellerOneStarCount,
            buyerAverageRating = this.allTime_buyerAverageRating,
            buyerReviewCount = this.allTime_buyerReviewCount,
            buyerCommentCount = this.allTime_buyerCommentCount,
            buyerCompletedTransactionCount = this.allTime_buyerCompletedTransactionCount,
            buyerFiveStarCount = this.allTime_buyerFiveStarCount,
            buyerFourStarCount = this.allTime_buyerFourStarCount,
            buyerThreeStarCount = this.allTime_buyerThreeStarCount,
            buyerTwoStarCount = this.allTime_buyerTwoStarCount,
            buyerOneStarCount = this.allTime_buyerOneStarCount
        ),
        lastYear = TimeframeSummary(
            sellerAverageRating = this.lastYear_sellerAverageRating,
            sellerReviewCount = this.lastYear_sellerReviewCount,
            sellerCommentCount = this.lastYear_sellerCommentCount,
            sellerCompletedTransactionCount = this.lastYear_sellerCompletedTransactionCount,
            sellerSalesCount = this.lastYear_sellerSalesCount,
            sellerFiveStarCount = this.lastYear_sellerFiveStarCount,
            sellerFourStarCount = this.lastYear_sellerFourStarCount,
            sellerThreeStarCount = this.lastYear_sellerThreeStarCount,
            sellerTwoStarCount = this.lastYear_sellerTwoStarCount,
            sellerOneStarCount = this.lastYear_sellerOneStarCount,
            buyerAverageRating = this.lastYear_buyerAverageRating,
            buyerReviewCount = this.lastYear_buyerReviewCount,
            buyerCommentCount = this.lastYear_buyerCommentCount,
            buyerCompletedTransactionCount = this.lastYear_buyerCompletedTransactionCount,
            buyerFiveStarCount = this.lastYear_buyerFiveStarCount,
            buyerFourStarCount = this.lastYear_buyerFourStarCount,
            buyerThreeStarCount = this.lastYear_buyerThreeStarCount,
            buyerTwoStarCount = this.lastYear_buyerTwoStarCount,
            buyerOneStarCount = this.lastYear_buyerOneStarCount
        ),
        last30Days = TimeframeSummary(
            sellerAverageRating = this.last30Days_sellerAverageRating,
            sellerReviewCount = this.last30Days_sellerReviewCount,
            sellerCommentCount = this.last30Days_sellerCommentCount,
            sellerCompletedTransactionCount = this.last30Days_sellerCompletedTransactionCount,
            sellerSalesCount = this.last30Days_sellerSalesCount,
            sellerFiveStarCount = this.last30Days_sellerFiveStarCount,
            sellerFourStarCount = this.last30Days_sellerFourStarCount,
            sellerThreeStarCount = this.last30Days_sellerThreeStarCount,
            sellerTwoStarCount = this.last30Days_sellerTwoStarCount,
            sellerOneStarCount = this.last30Days_sellerOneStarCount,
            buyerAverageRating = this.last30Days_buyerAverageRating,
            buyerReviewCount = this.last30Days_buyerReviewCount,
            buyerCommentCount = this.last30Days_buyerCommentCount,
            buyerCompletedTransactionCount = this.last30Days_buyerCompletedTransactionCount,
            buyerFiveStarCount = this.last30Days_buyerFiveStarCount,
            buyerFourStarCount = this.last30Days_buyerFourStarCount,
            buyerThreeStarCount = this.last30Days_buyerThreeStarCount,
            buyerTwoStarCount = this.last30Days_buyerTwoStarCount,
            buyerOneStarCount = this.last30Days_buyerOneStarCount
        ),
        updatedAt = this.updatedAt
    )
}
