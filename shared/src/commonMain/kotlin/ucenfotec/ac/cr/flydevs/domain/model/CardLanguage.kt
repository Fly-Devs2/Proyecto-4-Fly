package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class CardLanguage(val label: String) {
    EN("Inglés (EN)"),
    ES("Español (ES)"),
    JP("Japonés (JP)"),
    DE("Alemán (DE)"),
    FR("Francés (FR)"),
    IT("Italiano (IT)"),
}
