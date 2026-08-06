package ucenfotec.ac.cr.flydevs.presentation.rolePermission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.model.AppPermission
import ucenfotec.ac.cr.flydevs.domain.model.AppRole
import ucenfotec.ac.cr.flydevs.domain.model.DEFAULT_PERMISSIONS
import ucenfotec.ac.cr.flydevs.domain.repository.IRoleRepository

class RolePermissionViewModel(
    private val roleRepository: IRoleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RolePermissionUiState())
    val uiState: StateFlow<RolePermissionUiState> = _uiState.asStateFlow()

    init {
        loadRoles()
    }

    private fun loadRoles() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                val roles = roleRepository.getRoles()
                val totalAccounts = roleRepository.getRoleSummaryCount()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    roles = roles,
                    totalAccounts = totalAccounts
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Error al cargar roles"
                )
            }
        }
    }

    fun onShowCreateRole() {
        _uiState.value = _uiState.value.copy(
            showCreateRoleSheet = true,
            editingRole = null,
            newRoleName = "",
            newRoleColorHex = "#A855F7",
            newRolePermissions = DEFAULT_PERMISSIONS
        )
    }

    fun onDismissCreateRole() {
        _uiState.value = _uiState.value.copy(showCreateRoleSheet = false, editingRole = null)
    }

    fun onEditRole(role: AppRole) {
        _uiState.value = _uiState.value.copy(
            showCreateRoleSheet = true,
            editingRole = role,
            newRoleName = role.name,
            newRoleColorHex = role.colorHex,
            newRolePermissions = role.permissions
        )
    }

    fun onRoleNameChange(name: String) {
        _uiState.value = _uiState.value.copy(newRoleName = name)
    }

    fun onColorSelected(colorHex: String) {
        _uiState.value = _uiState.value.copy(newRoleColorHex = colorHex)
    }

    fun onPermissionToggled(permissionId: String) {
        val updatedPermissions = _uiState.value.newRolePermissions.map { permission ->
            if (permission.id == permissionId) {
                permission.copy(isEnabled = !permission.isEnabled)
            } else {
                permission
            }
        }
        _uiState.value = _uiState.value.copy(newRolePermissions = updatedPermissions)
    }

    fun onCreateRole() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingRole = true, errorMessage = null)

            val currentEditing = _uiState.value.editingRole

            if (currentEditing != null) {
                val updatedRole = currentEditing.copy(
                    name = _uiState.value.newRoleName,
                    colorHex = _uiState.value.newRoleColorHex,
                    permissions = _uiState.value.newRolePermissions
                )

                roleRepository.updateRole(updatedRole)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isCreatingRole = false,
                            createRoleSuccess = true,
                            showCreateRoleSheet = false,
                            editingRole = null
                        )
                        loadRoles()
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isCreatingRole = false,
                            errorMessage = error.message ?: "Error al actualizar rol"
                        )
                    }
            } else {
                val newRole = AppRole(
                    id = "",
                    name = _uiState.value.newRoleName,
                    colorHex = _uiState.value.newRoleColorHex,
                    accountCount = 0,
                    permissions = _uiState.value.newRolePermissions
                )

                roleRepository.createRole(newRole)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isCreatingRole = false,
                            createRoleSuccess = true,
                            showCreateRoleSheet = false
                        )
                        loadRoles()
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isCreatingRole = false,
                            errorMessage = error.message ?: "Error al crear rol"
                        )
                    }
            }
        }
    }

    fun onDeleteRoleRequested(role: AppRole) {
        _uiState.value = _uiState.value.copy(
            showDeleteConfirmDialog = true,
            roleToDelete = role
        )
    }

    fun onDeleteRoleConfirmed() {
        val role = _uiState.value.roleToDelete ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showDeleteConfirmDialog = false, roleToDelete = null)
            roleRepository.deleteRole(role.id)
                .onSuccess { loadRoles() }
                .onFailure { e -> _uiState.value = _uiState.value.copy(errorMessage = "Error al eliminar: ${e.message}") }
        }
    }

    fun onDeleteRoleDismissed() {
        _uiState.value = _uiState.value.copy(showDeleteConfirmDialog = false, roleToDelete = null)
    }

    fun onErrorDismissed() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun onCreateRoleSuccessDismissed() {
        _uiState.value = _uiState.value.copy(createRoleSuccess = false)
    }
}
