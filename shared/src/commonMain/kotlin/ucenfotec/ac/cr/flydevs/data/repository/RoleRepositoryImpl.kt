package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import ucenfotec.ac.cr.flydevs.domain.model.AppRole
import ucenfotec.ac.cr.flydevs.domain.model.AppPermission
import ucenfotec.ac.cr.flydevs.domain.model.DEFAULT_PERMISSIONS
import ucenfotec.ac.cr.flydevs.domain.repository.IRoleRepository

class RoleRepositoryImpl : IRoleRepository {
    private val firestore = Firebase.firestore

    override suspend fun getRoles(): List<AppRole> {
        return try {
            println("RoleRepo DEBUG: Starting getRoles()")
            val snapshot = firestore.collection("rols").get()
            println("RoleRepo DEBUG: document count = ${snapshot.documents.size}")

            snapshot.documents.mapNotNull { doc ->
                try {
                    println("RoleRepo DEBUG: Processing ${doc.id}")

                    val name: String = doc.get<String?>("name")
                        ?: doc.get<String?>("displayName")
                        ?: doc.id

                    val colorHex: String = doc.get<String?>("colorHex") ?: "#A855F7"

                    // Read accountCount safely without using Any
                    val accountCount: Int = try {
                        doc.get<Long?>("accountCount")?.toInt() ?: 0
                    } catch (e: Exception) {
                        try {
                            doc.get<Double?>("accountCount")?.toInt() ?: 0
                        } catch (e2: Exception) {
                            0
                        }
                    }

                    // Read permissions as a raw map using the serializer the project already uses
                    val role = doc.data(AppRole.serializer())
                    val permissions = if (role.permissions.isNotEmpty()) {
                        role.permissions
                    } else {
                        DEFAULT_PERMISSIONS
                    }

                    println("RoleRepo DEBUG: Built role = $name, permissions = ${permissions.size}")

                    AppRole(
                        id = doc.id,
                        name = name,
                        colorHex = colorHex,
                        accountCount = accountCount,
                        permissions = permissions
                    )
                } catch (e: Exception) {
                    println("RoleRepo DEBUG: EXCEPTION on ${doc.id}: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            println("RoleRepo DEBUG: EXCEPTION fetching: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getRoleSummaryCount(): Int {
        return try {
            val roles = getRoles()
            roles.sumOf { it.accountCount }
        } catch (e: Exception) {
            println("DEBUG_ROLE: Error calculating role summary count: ${e.message}")
            0
        }
    }

    override suspend fun createRole(role: AppRole): Result<Unit> {
        return try {
            val roleId = role.id.ifBlank { role.name.lowercase().replace(" ", "_") }
            val roleToSave = role.copy(id = roleId)
            firestore.collection("rols").document(roleId).set(AppRole.serializer(), roleToSave)
            Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG_ROLE: Error creating role: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateRole(role: AppRole): Result<Unit> {
        return try {
            firestore.collection("rols").document(role.id).set(AppRole.serializer(), role)
            Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG_ROLE: Error updating role: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun deleteRole(roleId: String): Result<Unit> {
        return try {
            firestore.collection("rols").document(roleId).delete()
            Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG_ROLE: Error deleting role: ${e.message}")
            Result.failure(e)
        }
    }
}
