package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class BatchStatus(val label: String) {
    READY_FOR_PICKUP("Disponible"),
    ACCEPTED("Aceptado"),
    PICKED_UP("En camino"),
    DELIVERED("Entregado"),
    CANCELLED("Cancelado");

    companion object {
        fun fromFirestore(value: String?): BatchStatus =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: READY_FOR_PICKUP
    }
}
