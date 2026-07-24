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

        val activeBatch = DeliveryBatch(
            batchId = "L-2208",
            orderIds = listOf(TEST_ORDER_ID),

            sourceStoreId = "perron_store",
            sourceStoreName = "Perrón Store",
            sourceStoreAddress = "San Pedro, San José",

            destinationStoreId = "arzu_bc",
            destinationStoreName = "Arzu Barrio Chino",
            destinationStoreAddress = "Escazú, San José",

            courierId = courierId,
            courierName = courierName,

            status = BatchStatus.PICKED_UP,
            courierReward = 2800L,

            qrStatus = BatchQrStatus.USED,

            pickupEvidence = BatchEvidence(
                storagePath =
                    "batch-evidence/test_batch_active/pickup/test.jpg",
                downloadUrl =
                    "https://example.com/test-pickup.jpg",
                uploadedAt = now - 1_800_000L,
                uploadedBy = courierId,
                type = BatchEvidenceType.PICKUP,
                note = "Evidencia de prueba"
            ),

            createdAt = now - 7_200_000L,
            acceptedAt = now - 3_600_000L,
            pickupAt = now - 1_800_000L,
            updatedAt = now - 1_800_000L
        )

        val upcomingBatchOne = DeliveryBatch(
            batchId = "L-2210",
            orderIds = listOf(TEST_ORDER_ID),

            sourceStoreId = "kira_store",
            sourceStoreName = "Kira",
            sourceStoreAddress = "Curridabat, San José",

            destinationStoreId = "vortex_store",
            destinationStoreName = "Vortex",
            destinationStoreAddress = "Heredia centro",

            courierId = courierId,
            courierName = courierName,

            status = BatchStatus.ACCEPTED,
            courierReward = 1800L,

            qrStatus = BatchQrStatus.USED,

            createdAt = now - 3_600_000L,
            acceptedAt = now - 900_000L,
            updatedAt = now - 900_000L
        )

        val upcomingBatchTwo = DeliveryBatch(
            batchId = "L-2211",
            orderIds = listOf(TEST_ORDER_ID),

            sourceStoreId = "vortex_store",
            sourceStoreName = "Vortex",
            sourceStoreAddress = "Curridabat, San José",

            destinationStoreId = "kira_store",
            destinationStoreName = "Kira",
            destinationStoreAddress = "Heredia centro",

            courierId = courierId,
            courierName = courierName,

            status = BatchStatus.ACCEPTED,
            courierReward = 2100L,

            qrStatus = BatchQrStatus.USED,

            createdAt = now - 1_800_000L,
            acceptedAt = now - 600_000L,
            updatedAt = now - 600_000L
        )

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
                storagePath =
                    "batch-evidence/test_batch_delivered/pickup/test.jpg",
                downloadUrl =
                    "https://example.com/test-pickup.jpg",
                uploadedAt = now - 18_000_000L,
                uploadedBy = courierId,
                type = BatchEvidenceType.PICKUP
            ),

            deliveryEvidence = BatchEvidence(
                storagePath =
                    "batch-evidence/test_batch_delivered/delivery/test.jpg",
                downloadUrl =
                    "https://example.com/test-delivery.jpg",
                uploadedAt = now - 14_400_000L,
                uploadedBy = courierId,
                type = BatchEvidenceType.DELIVERY
            ),

            createdAt = now - 21_600_000L,
            acceptedAt = now - 19_800_000L,
            pickupAt = now - 18_000_000L,
            inTransitAt = now - 16_200_000L,
            deliveredAt = now - 14_400_000L,
            updatedAt = now - 14_400_000L
        )

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

        batchesCollection
            .document("test_batch_active")
            .set(activeBatch)

        batchesCollection
            .document("test_batch_upcoming_1")
            .set(upcomingBatchOne)

        batchesCollection
            .document("test_batch_upcoming_2")
            .set(upcomingBatchTwo)

        batchesCollection
            .document("test_batch_delivered")
            .set(deliveredBatch)

        batchesCollection
            .document("test_batch_available")
            .set(availableBatch)
    }
}