package ucenfotec.ac.cr.flydevs.presentation.rolePermission

import kotlinx.serialization.Serializable
import ucenfotec.ac.cr.flydevs.domain.model.AppPermission
import ucenfotec.ac.cr.flydevs.domain.model.AppRole
import ucenfotec.ac.cr.flydevs.domain.model.DEFAULT_PERMISSIONS

@Serializable
data class RolePermissionUiState(
    val isLoading: Boolean = false,
    val roles: List<AppRole> = emptyList(),
    val totalAccounts: Int = 0,
    val errorMessage: String? = null,
    val showCreateRoleSheet: Boolean = false,
    val editingRole: AppRole? = null,
    val showDeleteConfirmDialog: Boolean = false,
    val roleToDelete: AppRole? = null,
    val newRoleName: String = "",
    val newRoleColorHex: String = "#A855F7",
    val newRolePermissions: List<AppPermission> = DEFAULT_PERMISSIONS,
    val isCreatingRole: Boolean = false,
    val createRoleSuccess: Boolean = false
)
