package ucenfotec.ac.cr.flydevs.domain.model

sealed interface ReviewEligibility {

    data object Available : ReviewEligibility

    data object SinpeNotPaid : ReviewEligibility

    data object UserNotParticipant : ReviewEligibility

    data object InvalidOrder : ReviewEligibility
}