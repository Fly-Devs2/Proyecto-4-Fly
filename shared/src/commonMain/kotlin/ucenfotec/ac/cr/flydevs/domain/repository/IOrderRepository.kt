package ucenfotec.ac.cr.flydevs.domain.repository

import kotlinx.coroutines.flow.Flow
import ucenfotec.ac.cr.flydevs.domain.model.Order
import ucenfotec.ac.cr.flydevs.domain.model.OrderStatus

interface IOrderRepository {
    fun getOrdersForUser(userId: String): Flow<List<Order>>
    fun getOrdersForUserHomePage(userId: String): Flow<List<Order>>
    fun getOrder(orderId: String): Flow<Order?>
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus)
    
    /**
     * El vendedor sube la foto del envío. Pasa el estado a WAITING_PAYMENT.
     */
    suspend fun submitSellerEvidence(orderId: String, evidenceUrl: String): Order

    /**
     * El comprador sube el comprobante SINPE. Pasa el estado a WAITING_STORE_SHIPMENT.
     */
    suspend fun submitSinpeProof(orderId: String, proofUrl: String): Order

    /**
     * Permite al comprador añadir evidencia adicional (fotos).
     */
    suspend fun addBuyerEvidence(orderId: String, evidenceUrl: String): Order

    /**
     * Marcar el sobre como enviado (Tránsito entre tiendas).
     */
    suspend fun markAsShipped(orderId: String): Order

    /**
     * Marcar el sobre como entregado en la tienda destino.
     */
    suspend fun markAsDeliveredToStore(orderId: String): Order
}
