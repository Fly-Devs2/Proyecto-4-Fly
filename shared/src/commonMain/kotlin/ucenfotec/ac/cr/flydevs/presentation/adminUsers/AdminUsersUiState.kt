package ucenfotec.ac.cr.flydevs.presentation.adminUsers

import ucenfotec.ac.cr.flydevs.domain.model.User
import ucenfotec.ac.cr.flydevs.domain.model.UserRole

data class AdminUsersUiState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val filteredUsers: List<User> = emptyList(),
    val searchQuery: String = "",
    val selectedRole: UserRole? = null,
    val errorMessage: String? = null
) {
    val totalCount: Int get() = users.size
}
