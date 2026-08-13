package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Store(
    val id: String = "",
    val name: String = ""
)
