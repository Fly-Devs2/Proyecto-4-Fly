package ucenfotec.ac.cr.flydevs.presentation.review

import ucenfotec.ac.cr.flydevs.domain.model.Review
import ucenfotec.ac.cr.flydevs.domain.model.ReviewEligibility

data class OrderReviewUiState(
    val isLoading: Boolean = true,
    val eligibility: ReviewEligibility =
        ReviewEligibility.InvalidOrder,

    val existingReview: Review? = null,

    val selectedRating: Double = 0.0,
    val comment: String = "",

    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
) {
    val canSubmitReview: Boolean
        get() =
            eligibility == ReviewEligibility.Available &&
                    selectedRating >= 1.0 &&
                    !isSaving

    val isEditingReview: Boolean
        get() = existingReview != null
}