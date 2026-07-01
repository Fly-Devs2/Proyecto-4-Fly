package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class CardStatus {
    AVAILABLE,
    RESERVED,
    SOLD,
}
