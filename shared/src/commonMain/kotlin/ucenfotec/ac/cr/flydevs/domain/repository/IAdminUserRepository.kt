package ucenfotec.ac.cr.flydevs.domain.repository

import kotlinx.coroutines.flow.Flow
import ucenfotec.ac.cr.flydevs.domain.model.User
import ucenfotec.ac.cr.flydevs.domain.model.UserAdminStats

interface IAdminUserRepository {
    fun observeAllUsers(): Flow<List<User>>
    suspend fun updateUserStatus(uid: String, isActive: Boolean): Result<Unit>
    suspend fun deleteUser(uid: String): Result<Unit>
    fun getUserStats(uid: String): Flow<UserAdminStats>
}
