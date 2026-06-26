package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class CardStatus(val label: String) {
    AVAILABLE("Disponible"),
    RESERVED("Reservada"),
    SOLD("Vendida"),
}
