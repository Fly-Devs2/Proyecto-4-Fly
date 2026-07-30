package ucenfotec.ac.cr.flydevs.domain.model

data class ReputationReviewItem(
    val review: Review,
    val reviewerName: String
) {
    val reviewerInitial: String
        get() = reviewerName
            .trim()
            .firstOrNull()
            ?.uppercaseChar()
            ?.toString()
            ?: "?"
}