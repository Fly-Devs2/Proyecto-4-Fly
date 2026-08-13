package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import ucenfotec.ac.cr.flydevs.domain.model.FlatUserRatingSummary
import ucenfotec.ac.cr.flydevs.domain.model.User
import ucenfotec.ac.cr.flydevs.domain.model.UserAdminStats
import ucenfotec.ac.cr.flydevs.domain.model.toNestedSummary
import ucenfotec.ac.cr.flydevs.domain.repository.IAdminUserRepository

class AdminUserRepositoryImpl(
    private val firestore: FirebaseFirestore
) : IAdminUserRepository {

    private val usersCollection = firestore.collection("users")
    private val incidentsCollection = firestore.collection("incidents")
    private val userRatingsCollection = firestore.collection("user_ratings")

    override fun observeAllUsers(): Flow<List<User>> {
        return usersCollection.snapshots.map { querySnapshot ->
            querySnapshot.documents.map { it.data<User>() }
        }
    }

    override suspend fun updateUserStatus(uid: String, isActive: Boolean): Result<Unit> {
        return runCatching {
            usersCollection.document(uid).set(mapOf("isActive" to isActive), merge = true)
        }
    }

    override suspend fun deleteUser(uid: String): Result<Unit> {
        return runCatching {
            usersCollection.document(uid).delete()
        }
    }

    override fun getUserStats(uid: String): Flow<UserAdminStats> {
        val ratingsFlow = userRatingsCollection.document(uid).snapshots.map { snapshot ->
            if (snapshot.exists) {
                val flat = snapshot.data<FlatUserRatingSummary>()
                val nested = flat.toNestedSummary()
                Triple(nested.allTime.buyerCompletedTransactionCount, nested.allTime.sellerSalesCount, nested.allTime.sellerAverageRating to nested.allTime.sellerReviewCount)
            } else {
                Triple(0, 0, 0.0 to 0)
            }
        }

        val reportsFlow = incidentsCollection.where { "counterpartId" equalTo uid }.snapshots.map { querySnapshot ->
            querySnapshot.documents.size
        }

        return combine(ratingsFlow, reportsFlow) { (purchases, sales, ratingInfo), reportCount ->
            UserAdminStats(
                uid = uid,
                reportCount = reportCount,
                completedPurchases = purchases,
                publishedSales = sales,
                averageRating = ratingInfo.first,
                totalReviews = ratingInfo.second
            )
        }
    }
}
