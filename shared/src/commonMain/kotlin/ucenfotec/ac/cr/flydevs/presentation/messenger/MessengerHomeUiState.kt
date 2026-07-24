package ucenfotec.ac.cr.flydevs.presentation.messenger

import ucenfotec.ac.cr.flydevs.domain.model.DeliveryBatch

data class MessengerHomeUiState(

    // Información general de la pantalla
    val isLoading: Boolean = false,

    // Información del mensajero autenticado
    val courierId: String = "",
    val courierName: String = "",
    val courierRating: Double = 0.0,

    // Estadísticas del día
    val todayEarnings: Long = 0L,
    val completedDeliveriesToday: Int = 0,
    val assignedDeliveriesToday: Int = 0,

    // Lote que el mensajero está transportando actualmente
    val activeBatch: DeliveryBatch? = null,

    // Lotes ya asignados que todavía no son el lote activo
    val upcomingBatches: List<DeliveryBatch> = emptyList(),

    // Lotes que aún no tienen mensajero asignado
    val availableBatches: List<DeliveryBatch> = emptyList(),

    // Estados de operaciones
    val isAcceptingBatch: Boolean = false,
    val isUpdatingBatch: Boolean = false,
    val isUploadingEvidence: Boolean = false,

    // Progreso de subida de fotografía entre 0 y 1
    val evidenceUploadProgress: Float = 0f,

    // Mensajes para Snackbar o AlertDialog
    val successMessage: String? = null,
    val errorMessage: String? = null
)