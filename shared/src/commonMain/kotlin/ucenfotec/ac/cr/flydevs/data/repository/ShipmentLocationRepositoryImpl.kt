package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ucenfotec.ac.cr.flydevs.domain.model.BatchStatus
import ucenfotec.ac.cr.flydevs.domain.model.ShipmentLocation
import ucenfotec.ac.cr.flydevs.domain.repository.IShipmentLocationRepository
import ucenfotec.ac.cr.flydevs.getEpochMillis

class ShipmentLocationRepositoryImpl :
    IShipmentLocationRepository {

    private val locationsCollection =
        Firebase.firestore.collection(
            "shipment_locations"
        )

    override fun observeShipmentLocation(
        batchDocumentId: String
    ): Flow<ShipmentLocation?> {
        require(batchDocumentId.isNotBlank()) {
            "El identificador del lote es obligatorio."
        }

        return locationsCollection
            .document(batchDocumentId)
            .snapshots
            .map { snapshot ->
                if (!snapshot.exists) {
                    null
                } else {
                    snapshot.data<ShipmentLocation>()
                }
            }
    }

    override suspend fun publishShipmentLocation(
        location: ShipmentLocation
    ) {
        require(
            location.batchDocumentId.isNotBlank()
        ) {
            "El identificador del lote es obligatorio."
        }

        require(location.courierId.isNotBlank()) {
            "El mensajero es obligatorio."
        }

        require(
            location.latitude in -90.0..90.0
        ) {
            "La latitud no es válida."
        }

        require(
            location.longitude in -180.0..180.0
        ) {
            "La longitud no es válida."
        }

        locationsCollection
            .document(location.batchDocumentId)
            .set(
                location.copy(
                    batchStatus =
                        BatchStatus.IN_TRANSIT.name,

                    trackingActive = true,
                    updatedAt = getEpochMillis()
                )
            )
    }

    override suspend fun stopTracking(
        batchDocumentId: String,
        finalStatus: BatchStatus
    ) {
        require(batchDocumentId.isNotBlank()) {
            "El identificador del lote es obligatorio."
        }

        val reference =
            locationsCollection.document(
                batchDocumentId
            )

        val snapshot = reference.get()

        if (!snapshot.exists) {
            return
        }

        reference.update(
            mapOf(
                "trackingActive" to false,
                "batchStatus" to finalStatus.name,
                "updatedAt" to getEpochMillis()
            )
        )
    }
    override suspend fun initializeTracking(
        batchDocumentId: String,
        courierId: String,
        buyerIds: List<String>
    ) {
        require(batchDocumentId.isNotBlank()) {
            "El identificador del lote es obligatorio."
        }

        require(courierId.isNotBlank()) {
            "El mensajero es obligatorio."
        }

        val now = getEpochMillis()

        val shipmentLocation =
            ShipmentLocation(
                batchDocumentId = batchDocumentId,
                courierId = courierId,
                buyerIds = buyerIds
                    .filter { it.isNotBlank() }
                    .distinct(),

                /*
                 * Todavía no tenemos GPS.
                 * El servicio reemplazará estos valores
                 * cuando reciba la primera posición.
                 */
                latitude = 0.0,
                longitude = 0.0,
                accuracyMeters = 0.0,

                batchStatus =
                    BatchStatus.IN_TRANSIT.name,

                trackingActive = true,
                updatedAt = now
            )

        locationsCollection
            .document(batchDocumentId)
            .set(shipmentLocation)
    }
}