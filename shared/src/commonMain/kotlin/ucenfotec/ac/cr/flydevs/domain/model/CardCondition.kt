package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class CardCondition(val label: String) {
    NEAR_MINT("Near Mint (NM)"),
    LIGHTLY_PLAYED("Lightly Played (LP)"),
    MODERATELY_PLAYED("Moderately Played (MP)"),
    HEAVILY_PLAYED("Heavily Played (HP)"),
    DAMAGED("Damaged (DMG)"),
}
