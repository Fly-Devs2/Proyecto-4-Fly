package ucenfotec.ac.cr.flydevs.domain.repository

import kotlinx.coroutines.flow.Flow
import ucenfotec.ac.cr.flydevs.domain.model.Order
import ucenfotec.ac.cr.flydevs.domain.model.PickedImage

interface IOrderRepository {
    fun getOrdersForUser(userId: String): Flow<List<Order>>
    fun getOrder(orderId: String): Flow<Order?>
    suspend fun updateOrderStatus(orderId: String, status: ucenfotec.ac.cr.flydevs.domain.model.OrderStatus)
    suspend fun uploadSinpeReceipt(orderId: String, image: PickedImage)
    suspend fun uploadSellerEvidence(orderId: String, image: PickedImage, note: String?)
    suspend fun uploadBuyerEvidence(orderId: String, image: PickedImage)
}
