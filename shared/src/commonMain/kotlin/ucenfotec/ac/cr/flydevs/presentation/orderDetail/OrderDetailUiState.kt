package ucenfotec.ac.cr.flydevs.presentation.orderDetail

import kotlinx.serialization.Serializable
import ucenfotec.ac.cr.flydevs.domain.model.Order
import ucenfotec.ac.cr.flydevs.domain.model.UserRole


@Serializable
data class OrderDetailUiState(
    val isLoading: Boolean = true,
    val order: Order? = null,
    val userRole: UserRole = UserRole.USER,
    val errorMessage: String? = null
)
