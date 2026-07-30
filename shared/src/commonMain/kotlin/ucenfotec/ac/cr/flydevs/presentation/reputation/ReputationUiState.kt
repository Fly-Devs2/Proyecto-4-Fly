package ucenfotec.ac.cr.flydevs.presentation.reputation

import ucenfotec.ac.cr.flydevs.domain.model.ReputationReviewItem
import ucenfotec.ac.cr.flydevs.domain.model.ReviewRole
import ucenfotec.ac.cr.flydevs.domain.model.RoleReputationSummary
import ucenfotec.ac.cr.flydevs.domain.model.UserRatingSummary
import ucenfotec.ac.cr.flydevs.domain.model.toRoleSummary

data class ReputationUiState(
    val userId: String = "",
    val userName: String = "",

    val selectedRole: ReviewRole =
        ReviewRole.SELLER,

    val ratingSummary: UserRatingSummary =
        UserRatingSummary(),

    val reviews: List<ReputationReviewItem> =
        emptyList(),

    val visibleReviewLimit: Int = 3,
    val hasMoreReviews: Boolean = false,

    val isLoading: Boolean = true,
    val isLoadingReviews: Boolean = false,

    val errorMessage: String? = null
) {
    val userInitial: String
        get() = userName
            .trim()
            .firstOrNull()
            ?.uppercaseChar()
            ?.toString()
            ?: "?"

    val roleSummary: RoleReputationSummary
        get() = ratingSummary.toRoleSummary(
            selectedRole
        )

    val hasReviews: Boolean
        get() = reviews.isNotEmpty()
}