package ucenfotec.ac.cr.flydevs.data.debug

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import ucenfotec.ac.cr.flydevs.domain.model.BatchEvidence
import ucenfotec.ac.cr.flydevs.domain.model.BatchEvidenceType
import ucenfotec.ac.cr.flydevs.domain.model.BatchQrStatus
import ucenfotec.ac.cr.flydevs.domain.model.BatchStatus
import ucenfotec.ac.cr.flydevs.domain.model.DeliveryBatch
import kotlin.time.Clock

object BatchTestDataSeeder {

    private const val TEST_ORDER_ID =
        "GvLYmudq9cbsQ24aUdcj"

    private val batchesCollection =
        Firebase.firestore.collection("batches")

    suspend fun seed(
        courierId: String,
        courierName: String
    ) {
        require(courierId.isNotBlank()) {
            "El UID del mensajero es obligatorio."
        }

        val now = Clock.System
            .now()
            .toEpochMilliseconds()

        // 1. First, check if "test_batch_available" needs to be reset
        try {
            val availableDoc = batchesCollection.document("test_batch_available").get()
            val existingCourierId = if (availableDoc.exists) availableDoc.get<String?>("courierId") else null
            
            if (!availableDoc.exists || existingCourierId.isNullOrBlank()) {
                val availableBatch = DeliveryBatch(
                    batchId = "L-2212",
                    orderIds = listOf(TEST_ORDER_ID),

                    sourceStoreId = "vortex_store",
                    sourceStoreName = "Vortex",
                    sourceStoreAddress = "Curridabat, San José",

                    destinationStoreId = "kira_store",
                    destinationStoreName = "Kira",
                    destinationStoreAddress = "Heredia centro",

                    courierId = null,
                    courierName = null,

                    status = BatchStatus.READY_FOR_PICKUP,
                    courierReward = 2500L,

                    qrStatus = BatchQrStatus.ACTIVE,
                    qrExpiresAt = now + 86_400_000L,

                    createdAt = now,
                    updatedAt = now
                )
                batchesCollection.document("test_batch_available").set(availableBatch)
                println("DEBUG_SEEDER: Reset test_batch_available (was empty)")
            } else {
                println("DEBUG_SEEDER: Skipped reset of test_batch_available (already accepted by $existingCourierId)")
            }
        } catch (e: Exception) {
            println("DEBUG_SEEDER: Error checking available batch: ${e.message}")
        }

        // 2. Seed your history (this is safe as it's separate from the scanned batch)
        val deliveredBatch = DeliveryBatch(
            batchId = "L-2197",
            orderIds = listOf(TEST_ORDER_ID),

            sourceStoreId = "vortex_store",
            sourceStoreName = "Vortex",
            sourceStoreAddress = "Curridabat, San José",

            destinationStoreId = "kira_store",
            destinationStoreName = "Kira",
            destinationStoreAddress = "Heredia centro",

            courierId = courierId,
            courierName = courierName,

            status = BatchStatus.DELIVERED,
            courierReward = 2200L,

            qrStatus = BatchQrStatus.USED,

            pickupEvidence = BatchEvidence(
                storagePath = "batch-evidence/test_batch_delivered/pickup/test.jpg",
                downloadUrl = "https://images.unsplash.com/photo-1566576721346-d4a3b4eaad55?q=80&w=1000&auto=format&fit=crop",
                uploadedAt = now - 18_000_000L,
                uploadedBy = courierId,
                type = BatchEvidenceType.PICKUP
            ),

            deliveryEvidence = BatchEvidence(
                storagePath = "batch-evidence/test_batch_delivered/delivery/test.jpg",
                downloadUrl = "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?q=80&w=1000&auto=format&fit=crop",
                uploadedAt = now - 14_400_000L,
                uploadedBy = courierId,
                type = BatchEvidenceType.DELIVERY,
                note = "Entregado a recepcionista en Vortex"
            ),

            createdAt = now - 21_600_000L,
            acceptedAt = now - 19_800_000L,
            pickupAt = now - 18_000_000L,
            inTransitAt = now - 16_200_000L,
            deliveredAt = now - 14_400_000L,
            updatedAt = now - 14_400_000L
        )

        batchesCollection.document("batch_delivered_$courierId").set(deliveredBatch)
    }

    /**
     * Genera un payload de QR para pruebas.
     */
    fun generateTestQrPayload(batchDocumentId: String): String {
        return "flydevs://batch-qr?bid=$batchDocumentId"
    }
}
