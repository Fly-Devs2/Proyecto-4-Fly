package ucenfotec.ac.cr.flydevs.domain.model

data class ReputationReviewsPage(
    val reviews: List<ReputationReviewItem> = emptyList(),
    val hasMore: Boolean = false
)