package ucenfotec.ac.cr.flydevs.presentation.shipmentLocation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.repository.IShipmentLocationRepository

class ShipmentLocationViewModel(
    private val shipmentLocationRepository: IShipmentLocationRepository,
    private val batchDocumentId: String
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            ShipmentLocationUiState()
        )

    val uiState: StateFlow<ShipmentLocationUiState> =
        _uiState.asStateFlow()

    private var locationJob: Job? = null

    init {
        println(
            "SHIPMENT_MAP_VM | CREATED | batch=$batchDocumentId"
        )
        observeLocation()
    }

    fun retry() {
        observeLocation()
    }

    private fun observeLocation() {
        println(
            "SHIPMENT_MAP_VM | observeLocation() | batch=$batchDocumentId"
        )
        locationJob?.cancel()

        locationJob = viewModelScope.launch {
            println(
                "SHIPMENT_MAP_VM | Coroutine started"
            )

            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            shipmentLocationRepository
                .observeShipmentLocation(
                    batchDocumentId = batchDocumentId
                )
                .catch { exception ->

                    println(
                        "SHIPMENT_MAP_ERROR | " +
                                exception.message
                    )

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage =
                                exception.message
                                    ?: "No se pudo cargar la ubicación."
                        )
                    }
                }
                .collectLatest { location ->

                    println(
                        "SHIPMENT_MAP_LOCATION | " +
                                "lat=${location?.latitude} | " +
                                "lng=${location?.longitude}"
                    )

                    _uiState.update {
                        it.copy(
                            location = location,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    override fun onCleared() {
        locationJob?.cancel()
        super.onCleared()
    }
}