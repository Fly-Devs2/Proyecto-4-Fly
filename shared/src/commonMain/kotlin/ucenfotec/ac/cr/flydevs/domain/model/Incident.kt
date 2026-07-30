package ucenfotec.ac.cr.flydevs.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/** Largo máximo de la descripción que puede escribir el usuario. */
const val INCIDENT_DESCRIPTION_MAX_LENGTH = 1000

@Serializable
enum class IncidentStatus(val label: String) {
    OPEN("Abierta"),
    IN_REVIEW("En revisión"),
    URGENT("Urgente"),
    RESOLVED("Resuelta");

    companion object {
        fun fromString(value: String?): IncidentStatus =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: OPEN
    }
}

@Serializable
data class Incident(
    // El ID es el del documento de Firestore, no se duplica dentro del documento.
    @Transient
    val id: String = "",

    val orderId: String = "",
    /** Código corto de la orden (`FA-1042`), el mismo que ve el usuario en la app. */
    val orderCode: String = "",

    val reporterId: String = "",
    val reporterName: String = "",
    /** Contraparte de la orden, para que el admin sepa contra quién es el reclamo. */
    val counterpartId: String = "",
    val counterpartName: String = "",

    val description: String = "",
    val orderStatusAtReport: String = "",

    val status: IncidentStatus = IncidentStatus.OPEN,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)
