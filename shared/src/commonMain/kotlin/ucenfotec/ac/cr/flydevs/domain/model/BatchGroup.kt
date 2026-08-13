package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

// Documento de la colección `batch_group`; agrupa lotes con la misma tienda destino.
@Serializable
data class BatchGroup(
    @Transient
    val documentId: String = "",
    val id: String = "",
    val batchList: List<String> = emptyList(),
    val storeDestination: String = "",
)
