package ucenfotec.ac.cr.flydevs.presentation.adminUserDetail

import ucenfotec.ac.cr.flydevs.domain.model.User
import ucenfotec.ac.cr.flydevs.domain.model.UserAdminStats

data class AdminUserDetailUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val stats: UserAdminStats? = null,
    val isActionLoading: Boolean = false,
    val actionSuccess: Boolean = false,
    val errorMessage: String? = null
)
