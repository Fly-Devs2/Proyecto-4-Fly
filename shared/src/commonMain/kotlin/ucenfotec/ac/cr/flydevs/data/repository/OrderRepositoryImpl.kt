package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ucenfotec.ac.cr.flydevs.domain.model.Order
import ucenfotec.ac.cr.flydevs.domain.model.OrderCardSnapshot
import ucenfotec.ac.cr.flydevs.domain.model.OrderStatus
import ucenfotec.ac.cr.flydevs.domain.model.PickedImage
import ucenfotec.ac.cr.flydevs.domain.repository.IImageStorageRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IOrderRepository
import ucenfotec.ac.cr.flydevs.getEpochMillis

class OrderRepositoryImpl(
    private val imageStorage: IImageStorageRepository
) : IOrderRepository {
    private val firestore = Firebase.firestore
    private val ordersCollection = firestore.collection("orders")

    override fun getOrdersForUser(userId: String): Flow<List<Order>> {
        println("DEBUG_ORDERS: Fetching orders for user: $userId")
        return ordersCollection.snapshots.map { snapshot ->
            println("DEBUG_ORDERS: Received snapshot with ${snapshot.documents.size} documents")
            snapshot.documents.mapNotNull { doc ->
                doc.toOrder()
            }.filter { 
                val match = it.buyerId.trim() == userId.trim() || it.sellerId.trim() == userId.trim()
                if (match) println("DEBUG_ORDERS: Match found for order ${it.id}")
                match
            }
        }
    }

    override fun getOrdersForUserHomePage(userId: String): Flow<List<Order>> {
        println("DEBUG_ORDERS: Fetching orders for user: $userId")
        return ordersCollection.snapshots.map { snapshot ->
            println("DEBUG_ORDERS: Received snapshot with ${snapshot.documents.size} documents")
            snapshot.documents.mapNotNull { doc ->
                doc.toOrder()
            }.filter {
                val match = it.buyerId.trim() == userId.trim() || it.sellerId.trim() == userId.trim()
                if (match) println("DEBUG_ORDERS: Match found for order ${it.id}")
                match
            }.sortedByDescending { it.createdAt }.take(3)
        }
    }

    override fun getOrder(orderId: String): Flow<Order?> {
        return ordersCollection.document(orderId).snapshots.map { snapshot ->
            if (snapshot.exists) {
                snapshot.toOrder()
            } else null
        }
    }

    override suspend fun getOrdersByIds(orderIds: List<String>): List<Order> = coroutineScope {
        orderIds
            .map { orderId -> async { runCatching { fetchOrderOnce(orderId) }.getOrNull() } }
            .awaitAll()
            .filterNotNull()
    }

    private suspend fun fetchOrderOnce(orderId: String): Order? {
        val snapshot = ordersCollection.document(orderId).get()
        return if (snapshot.exists) snapshot.toOrder() else null
    }

    private fun DocumentSnapshot.toOrder(): Order? {
        return try {
            data(Order.serializer()).copy(id = id)
        } catch (e: Exception) {
            println("DEBUG_ORDERS: Auto-serialization failed for doc $id: ${e.message}")
            try {
                Order(
                    id = id,
                    cards = safeGetCards("cards") ?: emptyList(),
                    buyerId = safeGet<String>("buyerId") ?: "",
                    sellerId = safeGet<String>("sellerId") ?: "",
                    status = OrderStatus.fromString(safeGet<String>("status") ?: ""),
                    sinpePaid = safeGet<Boolean>("sinpePaid") ?: false,
                    sinpeReceiptUrl = safeGet<String>("sinpeReceiptUrl"),
                    sellerEvidenceUrls = safeGetList("sellerEvidenceUrls") ?: emptyList(),
                    buyerEvidenceUrls = safeGetList("buyerEvidenceUrls") ?: emptyList(),
                    sellerName = safeGet<String>("sellerName") ?: "Vendedor",
                    buyerName = safeGet<String>("buyerName") ?: "Comprador",
                    sobreId = safeGet<String>("sobreId") ?: "",
                    createdAt = 0L, 
                    modifiedAt = 0L
                )
            } catch (e2: Exception) {
                println("DEBUG_ORDERS: Manual mapping failed for doc $id: ${e2.message}")
                null
            }
        }
    }

    private inline fun <reified T> DocumentSnapshot.safeGet(field: String): T? {
        return try {
            get<T>(field)
        } catch (e: Exception) {
            null
        }
    }

    private fun DocumentSnapshot.safeGetList(field: String): List<String>? {
        return try {
            get<List<String>>(field)
        } catch (e: Exception) {
            // Fallback: try to read single string if old data exists
            try {
                val single = get<String>(field.removeSuffix("s"))
                listOf(single)
            } catch (e2: Exception) {
                null
            }
        }
    }

    private fun DocumentSnapshot.safeGetCards(field: String): List<OrderCardSnapshot>? {
        return try {
            val cards = get<List<OrderCardSnapshot>>(field)
            // Nuevas órdenes con imageUrls
            cards
        } catch (e: Exception) {
            // Fallback: órdenes antiguas con imageUrl (String) en lugar de imageUrls (List<String>)
            try {
                val rawCards = get<List<Map<String, Any?>>>(field)
                rawCards.map { cardMap ->
                    val imageUrls = when {
                        // Si tiene imageUrls (nuevo formato)
                        cardMap.containsKey("imageUrls") && cardMap["imageUrls"] is List<*> -> {
                            @Suppress("UNCHECKED_CAST")
                            (cardMap["imageUrls"] as List<String>).takeIf { it.isNotEmpty() } ?: emptyList()
                        }
                        // Si tiene imageUrl (antiguo formato)
                        cardMap.containsKey("imageUrl") && cardMap["imageUrl"] is String -> {
                            val imageUrl = cardMap["imageUrl"] as String
                            if (imageUrl.isNotEmpty()) listOf(imageUrl) else emptyList()
                        }
                        else -> emptyList()
                    }
                    OrderCardSnapshot(
                        cardId = cardMap["cardId"] as? String ?: "",
                        name = cardMap["name"] as? String ?: "",
                        imageUrls = imageUrls,
                        price = when (val p = cardMap["price"]) {
                            is Long -> p
                            is Number -> p.toLong()
                            else -> 0L
                        },
                        condition = cardMap["condition"] as? String ?: "",
                        game = cardMap["game"] as? String ?: ""
                    )
                }
            } catch (e2: Exception) {
                println("DEBUG_ORDERS: Failed to parse cards from $field: ${e2.message}")
                null
            }
        }
    }

    override suspend fun updateOrderStatus(orderId: String, status: OrderStatus) {
        val updates = mapOf(
            "status" to status.name,
            "modifiedAt" to getEpochMillis()
        )
        ordersCollection.document(orderId).update(updates)
    }

    override suspend fun submitSellerEvidence(orderId: String, evidenceUrl: String): Order {
        val current = fetchOrderOnce(orderId)
            ?: throw IllegalStateException("Orden no encontrada: $orderId")
        
        val newStatus = if (current.status == OrderStatus.WAITING_SELLER_DELIVERY) {
            OrderStatus.WAITING_PAYMENT
        } else {
            current.status
        }
        val updated = current.copy(
            sellerEvidenceUrls = current.sellerEvidenceUrls + evidenceUrl,
            status = newStatus,
            modifiedAt = getEpochMillis()
        )
        ordersCollection.document(orderId).set(Order.serializer(), updated)
        return updated
    }

    override suspend fun submitSinpeProof(orderId: String, proofUrl: String): Order {
        val current = fetchOrderOnce(orderId)
            ?: throw IllegalStateException("Orden no encontrada: $orderId")

        val updated = current.copy(
            sinpeReceiptUrl = proofUrl,
            status = OrderStatus.AWAITING_SINPE_VALIDATION,
            sinpeRejected = false,
            modifiedAt = getEpochMillis()
        )
        ordersCollection.document(orderId).set(Order.serializer(), updated)
        return updated
    }

    override suspend fun approveSinpeProof(orderId: String): Order {
        val current = fetchOrderOnce(orderId)
            ?: throw IllegalStateException("Orden no encontrada: $orderId")

        val updated = current.copy(
            sinpePaid = true,
            status = OrderStatus.WAITING_STORE_SHIPMENT,
            sinpeRejected = false,
            modifiedAt = getEpochMillis()
        )
        ordersCollection.document(orderId).set(Order.serializer(), updated)
        return updated
    }

    override suspend fun rejectSinpeProof(orderId: String): Order {
        val current = fetchOrderOnce(orderId)
            ?: throw IllegalStateException("Orden no encontrada: $orderId")

        val updated = current.copy(
            sinpePaid = false,
            status = OrderStatus.AWAITING_SINPE_VALIDATION,
            sinpeRejected = true,
            modifiedAt = getEpochMillis()
        )
        ordersCollection.document(orderId).set(Order.serializer(), updated)
        return updated
    }

    override suspend fun addBuyerEvidence(orderId: String, evidenceUrl: String): Order {
        val current = fetchOrderOnce(orderId)
            ?: throw IllegalStateException("Orden no encontrada: $orderId")

        val updated = current.copy(
            buyerEvidenceUrls = current.buyerEvidenceUrls + evidenceUrl,
            modifiedAt = getEpochMillis()
        )
        ordersCollection.document(orderId).set(Order.serializer(), updated)
        return updated
    }

    override suspend fun markAsShipped(orderId: String): Order {
        val current = fetchOrderOnce(orderId)
            ?: throw IllegalStateException("Orden no encontrada: $orderId")

        val updated = current.copy(
            status = OrderStatus.IN_TRANSIT,
            modifiedAt = getEpochMillis()
        )
        ordersCollection.document(orderId).set(Order.serializer(), updated)
        return updated
    }

    override suspend fun markAsDeliveredToStore(orderId: String): Order {
        val current = fetchOrderOnce(orderId)
            ?: throw IllegalStateException("Orden no encontrada: $orderId")

        val updated = current.copy(
            status = OrderStatus.DELIVERED_TO_STORE,
            modifiedAt = getEpochMillis()
        )
        ordersCollection.document(orderId).set(Order.serializer(), updated)
        return updated
    }

    override suspend fun cancelOrder(orderId: String): Order {
        val current = fetchOrderOnce(orderId)
            ?: throw IllegalStateException("Orden no encontrada: $orderId")

        // Validar que es cancelable
        if (current.sinpePaid) {
            throw IllegalStateException("No se puede cancelar una orden pagada confirmada")
        }
        if (current.status !in listOf(
            OrderStatus.WAITING_SELLER_DELIVERY,
            OrderStatus.WAITING_PAYMENT,
            OrderStatus.AWAITING_SINPE_VALIDATION
        )) {
            throw IllegalStateException("La orden no está en estado cancelable: ${current.status}")
        }

        // UPDATE parcial: solo cambiar status y modifiedAt
        val updates = mapOf(
            "status" to OrderStatus.CANCELLED.name,
            "modifiedAt" to getEpochMillis()
        )
        ordersCollection.document(orderId).update(updates)

        return current.copy(
            status = OrderStatus.CANCELLED,
            modifiedAt = getEpochMillis()
        )
    }
}
