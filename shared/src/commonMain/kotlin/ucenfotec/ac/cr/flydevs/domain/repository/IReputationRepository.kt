package ucenfotec.ac.cr.flydevs.domain.repository

import kotlinx.coroutines.flow.Flow
import ucenfotec.ac.cr.flydevs.domain.model.ReputationReviewsPage
import ucenfotec.ac.cr.flydevs.domain.model.ReputationTimeframe
import ucenfotec.ac.cr.flydevs.domain.model.ReviewRole
import ucenfotec.ac.cr.flydevs.domain.model.User
import ucenfotec.ac.cr.flydevs.domain.model.UserRatingSummary

interface IReputationRepository {

    fun observeUser(
        userId: String
    ): Flow<User?>

    fun observeRatingSummary(
        userId: String
    ): Flow<UserRatingSummary>

    suspend fun getReviews(
        userId: String,
        role: ReviewRole,
        limit: Int,
        timeframe: ReputationTimeframe = ReputationTimeframe.ALL_TIME
    ): ReputationReviewsPage
}