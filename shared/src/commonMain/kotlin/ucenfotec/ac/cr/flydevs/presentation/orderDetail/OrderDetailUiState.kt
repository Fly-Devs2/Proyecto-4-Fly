package ucenfotec.ac.cr.flydevs.presentation.orderDetail

import kotlinx.serialization.Serializable
import ucenfotec.ac.cr.flydevs.domain.model.Order

@Serializable
enum class UserRole { BUYER, SELLER, UNKNOWN }

@Serializable
data class OrderDetailUiState(
    val isLoading: Boolean = true,
    val order: Order? = null,
    val userRole: UserRole = UserRole.UNKNOWN,
    val errorMessage: String? = null
)
