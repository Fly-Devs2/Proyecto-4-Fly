package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ucenfotec.ac.cr.flydevs.domain.model.Order
import ucenfotec.ac.cr.flydevs.domain.model.OrderStatus
import ucenfotec.ac.cr.flydevs.domain.model.PickedImage
import ucenfotec.ac.cr.flydevs.domain.repository.IImageStorageRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IOrderRepository
import ucenfotec.ac.cr.flydevs.getEpochMillis

class OrderRepositoryImpl(
    private val imageStorage: IImageStorageRepository
) : IOrderRepository {
    private val firestore = Firebase.firestore

    override fun getOrdersForUser(userId: String): Flow<List<Order>> {
        println("DEBUG_ORDERS: Fetching orders for user: $userId")
        return firestore.collection("ORDERS")
            .snapshots.map { snapshot ->
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

    override fun getOrder(orderId: String): Flow<Order?> {
        return firestore.collection("ORDERS").document(orderId).snapshots.map { snapshot ->
            if (snapshot.exists) {
                snapshot.toOrder()
            } else null
        }
    }

    private fun DocumentSnapshot.toOrder(): Order? {
        return try {
            // Intento 1: Deserialización automática
            data(Order.serializer()).copy(id = id)
        } catch (e: Exception) {
            println("DEBUG_ORDERS: Auto-serialization failed for doc $id: ${e.message}")
            // Intento 2: Acceso manual campo por campo para evitar problemas con Timestamps
            try {
                Order(
                    id = id,
                    cardId = safeGet<String>("cardId") ?: "",
                    buyerId = safeGet<String>("buyerId") ?: "",
                    sellerId = safeGet<String>("sellerId") ?: "",
                    cardName = safeGet<String>("cardName") ?: "Carta sin nombre",
                    cardImageUrl = safeGet<String>("cardImageUrl") ?: "",
                    cardPrice = safeGet<Long>("cardPrice") ?: 0L,
                    status = OrderStatus.fromString(safeGet<String>("status") ?: ""),
                    sinpePaid = safeGet<Boolean>("sinpePaid") ?: false,
                    sinpeReceiptUrl = safeGet<String>("sinpeReceiptUrl"),
                    sellerEvidenceUrl = safeGet<String>("sellerEvidenceUrl"),
                    buyerEvidenceUrl = safeGet<String>("buyerEvidenceUrl"),
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

    override suspend fun updateOrderStatus(orderId: String, status: OrderStatus) {
        val updates = mapOf(
            "status" to status.name,
            "modifiedAt" to getEpochMillis()
        )
        firestore.collection("ORDERS").document(orderId).update(updates)
    }

    override suspend fun uploadSinpeReceipt(orderId: String, image: PickedImage) {
        val url = imageStorage.uploadImage(image, "receipts")
        val updates = mapOf(
            "sinpeReceiptUrl" to url,
            "sinpePaid" to true,
            "status" to OrderStatus.WAITING_PAYMENT.name,
            "modifiedAt" to getEpochMillis()
        )
        firestore.collection("ORDERS").document(orderId).update(updates)
    }

    override suspend fun uploadSellerEvidence(orderId: String, image: PickedImage, note: String?) {
        val url = imageStorage.uploadImage(image, "evidence/seller")
        val updates = mutableMapOf<String, Any>(
            "sellerEvidenceUrl" to url,
            "status" to OrderStatus.WAITING_PAYMENT.name,
            "modifiedAt" to getEpochMillis()
        )
        firestore.collection("ORDERS").document(orderId).update(updates)
    }

    override suspend fun uploadBuyerEvidence(orderId: String, image: PickedImage) {
        val url = imageStorage.uploadImage(image, "evidence/buyer")
        val updates = mapOf(
            "buyerEvidenceUrl" to url,
            "status" to OrderStatus.PICKED_UP.name,
            "modifiedAt" to getEpochMillis()
        )
        firestore.collection("ORDERS").document(orderId).update(updates)
    }
}
