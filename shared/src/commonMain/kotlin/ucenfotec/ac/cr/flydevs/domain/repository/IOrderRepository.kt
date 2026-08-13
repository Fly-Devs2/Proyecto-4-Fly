package ucenfotec.ac.cr.flydevs.domain.repository

import kotlinx.coroutines.flow.Flow
import ucenfotec.ac.cr.flydevs.domain.model.Order
import ucenfotec.ac.cr.flydevs.domain.model.OrderStatus

interface IOrderRepository {
    fun getOrdersForUser(userId: String): Flow<List<Order>>
    fun getOrdersForUserHomePage(userId: String): Flow<List<Order>>
    fun getOrder(orderId: String): Flow<Order?>

    /** Órdenes cuya tienda destino es [storeId]; alimenta el panel de la tienda. */
    fun observeStoreOrders(storeId: String): Flow<List<Order>>

    /** Resuelve los sobres de un lote a partir de `orderIds`; omite los que ya no existen. */
    suspend fun getOrdersByIds(orderIds: List<String>): List<Order>
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus)
    
    /**
     * El vendedor sube la foto del envío. Pasa el estado a WAITING_PAYMENT.
     */
    suspend fun submitSellerEvidence(orderId: String, evidenceUrl: String): Order

    /**
     * El comprador sube el comprobante SINPE.
     *
     * La orden pasa al estado AWAITING_SINPE_VALIDATION mientras
     * el vendedor valida o rechaza el comprobante.
     */
    suspend fun submitSinpeProof(orderId: String, proofUrl: String): Order

    /**
     * El vendedor aprueba el comprobante SINPE subido por el comprador.
     * Pasa la orden a WAITING_STORE_SHIPMENT y marca sinpePaid = true.
     */
    suspend fun approveSinpeProof(orderId: String): Order

    /**
     * El vendedor rechaza el comprobante SINPE. Mantiene AWAITING_SINPE_VALIDATION
     * y asegura sinpePaid = false.
     */
    suspend fun rejectSinpeProof(orderId: String): Order

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

    /**
     * La tienda valida el QR de retiro del comprador: la orden pasa a PICKED_UP
     * y sus cartas quedan marcadas como vendidas.
     *
     * @param qrSignature firma HMAC que viaja dentro del QR; se compara contra la
     *   almacenada en la orden cuando ambas están disponibles.
     */
    suspend fun confirmStorePickup(
        orderId: String,
        storeId: String,
        qrSignature: String?
    ): Order

    /**
     * El comprador cancela la orden si aún no ha sido pagada confirmada.
     * Solo cancelable en estados WAITING_SELLER_DELIVERY, WAITING_PAYMENT, AWAITING_SINPE_VALIDATION.
     */
    suspend fun cancelOrder(orderId: String): Order
}
