package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class CardExpansion(val label: String) {
    ALPHA("Alpha"),
    BETA("Beta"),
    UNLIMITED("Unlimited"),
    REVISED("Revised"),
    ARABIAN_NIGHTS("Arabian Nights"),
    LEGENDS("Legends"),
}
