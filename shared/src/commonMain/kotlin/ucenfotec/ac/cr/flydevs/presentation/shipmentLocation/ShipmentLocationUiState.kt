package ucenfotec.ac.cr.flydevs.presentation.shipmentLocation

import ucenfotec.ac.cr.flydevs.domain.model.ShipmentLocation

data class ShipmentLocationUiState(
    val location: ShipmentLocation? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val hasValidLocation: Boolean
        get() {
            val currentLocation = location ?: return false

            return currentLocation.latitude != 0.0 &&
                    currentLocation.longitude != 0.0
        }
}