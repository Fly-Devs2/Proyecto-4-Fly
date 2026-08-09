package ucenfotec.ac.cr.flydevs.presentation.shipmentLocation

import ucenfotec.ac.cr.flydevs.domain.model.BatchStatus
import ucenfotec.ac.cr.flydevs.domain.model.ShipmentLocation


data class ShipmentLocationUiState(
    val location: ShipmentLocation? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val hasValidLocation: Boolean
        get() {
            val current = location ?: return false

            return current.latitude != 0.0 &&
                    current.longitude != 0.0 &&
                    current.trackingActive &&
                    current.batchStatus == BatchStatus.IN_TRANSIT.name
        }
}