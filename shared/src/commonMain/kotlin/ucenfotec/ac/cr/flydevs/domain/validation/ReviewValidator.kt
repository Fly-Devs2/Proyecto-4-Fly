package ucenfotec.ac.cr.flydevs.domain.validation

object ReviewValidator {
    private val allowedReviewRatings = setOf(
        1.0,
        1.5,
        2.0,
        2.5,
        3.0,
        3.5,
        4.0,
        4.5,
        5.0
    )

    const val MAX_REVIEW_COMMENT_LENGTH = 500

    fun Double.isValidReviewRating(): Boolean {
        return this in allowedReviewRatings
    }

    fun String.isValidReviewComment(): Boolean {
        return trim().length <= MAX_REVIEW_COMMENT_LENGTH
    }
}