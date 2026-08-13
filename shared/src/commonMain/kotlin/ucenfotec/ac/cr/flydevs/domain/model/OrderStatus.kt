package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class OrderStatus(val label: String) {
    RESERVED("Reservado"),
    WAITING_SELLER_DELIVERY("Esperando entrega del vendedor"),
    WAITING_PAYMENT("Esperando pago"),
    AWAITING_SINPE_VALIDATION("Esperando validación del SINPE"),
    WAITING_STORE_SHIPMENT("Esperando envío entre tiendas"),
    IN_TRANSIT("Tránsito entre tiendas"),
    DELIVERED_TO_STORE("Entregado en destino"),
    PICKED_UP("Carta recogida"),
    DISPUTED("Disputa abierta"),
    CANCELLED("Cancelado");

    companion object {
        fun fromString(value: String): OrderStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true) } 
                ?: WAITING_SELLER_DELIVERY
        }
    }
}
