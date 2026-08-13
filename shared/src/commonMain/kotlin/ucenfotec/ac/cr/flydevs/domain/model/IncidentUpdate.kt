package ucenfotec.ac.cr.flydevs.domain.model


import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

const val INCIDENT_UPDATE_MAX_LENGTH = 1000

@Serializable
data class IncidentUpdate(
    @Transient
    val id: String = "",

    val incidentId: String = "",

    val authorId: String = "",
    val authorName: String = "",
    val authorRole: String = "",

    val message: String = "",

    /**
     * true: visible únicamente para administradores.
     * false: podrá mostrarse posteriormente a comprador y vendedor.
     */
    val isInternal: Boolean = true,

    val createdAt: Long = 0L
)