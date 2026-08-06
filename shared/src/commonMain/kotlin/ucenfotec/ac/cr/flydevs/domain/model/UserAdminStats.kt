package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserAdminStats(
    val uid: String = "",
    val reportCount: Int = 0,
    val completedPurchases: Int = 0,
    val publishedSales: Int = 0,
    val averageRating: Double = 0.0,
    val totalReviews: Int = 0
) {
    val reputationPercentage: Int
        get() = if (totalReviews > 0) ((averageRating / 5.0) * 100).toInt() else 100
}
