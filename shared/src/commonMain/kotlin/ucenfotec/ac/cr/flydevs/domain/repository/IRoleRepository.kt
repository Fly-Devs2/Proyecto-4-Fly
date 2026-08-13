package ucenfotec.ac.cr.flydevs.domain.repository

import ucenfotec.ac.cr.flydevs.domain.model.AppRole

interface IRoleRepository {
    suspend fun getRoles(): List<AppRole>
    suspend fun getRoleSummaryCount(): Int
    suspend fun createRole(role: AppRole): Result<Unit>
    suspend fun updateRole(role: AppRole): Result<Unit>
    suspend fun deleteRole(roleId: String): Result<Unit>
}
