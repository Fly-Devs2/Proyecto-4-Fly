package ucenfotec.ac.cr.flydevs.presentation.AdminIncidentDetail

import ucenfotec.ac.cr.flydevs.domain.model.Incident
import ucenfotec.ac.cr.flydevs.domain.model.IncidentPriority
import ucenfotec.ac.cr.flydevs.domain.model.IncidentStatus
import ucenfotec.ac.cr.flydevs.domain.model.IncidentUpdate
import ucenfotec.ac.cr.flydevs.domain.model.Order

data class AdminIncidentDetailUiState(
    val incidentId: String = "",

    val incident: Incident? = null,
    val order: Order? = null,
    val updates: List<IncidentUpdate> = emptyList(),

    val adminId: String = "",
    val adminName: String = "",

    val noteText: String = "",
    val isInternalNote: Boolean = true,

    val isLoading: Boolean = true,
    val isUpdatingStatus: Boolean = false,
    val isUpdatingPriority: Boolean = false,
    val isAddingNote: Boolean = false,
    val isResolving: Boolean = false,

    val successMessage: String? = null,
    val errorMessage: String? = null
) {
    val displayIncidentId: String
        get() {
            val currentId = incident?.id.orEmpty()

            return if (currentId.isBlank()) {
                "INC-"
            } else {
                "INC-${currentId.takeLast(8).uppercase()}"
            }
        }

    val displayOrderCode: String
        get() {
            val currentIncident = incident ?: return ""

            return currentIncident.orderCode
                .trim()
                .ifBlank {
                    currentIncident.orderId
                        .takeLast(8)
                        .uppercase()
                }
        }

    /**
     * Compatibilidad con incidencias antiguas donde
     * URGENT todavía era utilizado como estado.
     */
    val effectiveStatus: IncidentStatus
        get() {
            return when (incident?.status) {
                IncidentStatus.URGENT ->
                    IncidentStatus.OPEN

                null ->
                    IncidentStatus.OPEN

                else ->
                    incident.status
            }
        }

    val effectivePriority: IncidentPriority
        get() {
            return if (
                incident?.status == IncidentStatus.URGENT
            ) {
                IncidentPriority.URGENT
            } else {
                incident?.priority
                    ?: IncidentPriority.MEDIUM
            }
        }

    val cardNames: List<String>
        get() {
            return order
                ?.cards
                ?.map { card ->
                    card.name.trim()
                }
                ?.filter { cardName ->
                    cardName.isNotBlank()
                }
                .orEmpty()
        }

    val buyerName: String
        get() {
            return order
                ?.buyerName
                ?.trim()
                ?.ifBlank {
                    incident?.reporterName.orEmpty()
                }
                .orEmpty()
                .ifBlank {
                    "Comprador no identificado"
                }
        }

    val sellerName: String
        get() {
            return order
                ?.sellerName
                ?.trim()
                ?.ifBlank {
                    incident?.counterpartName.orEmpty()
                }
                .orEmpty()
                .ifBlank {
                    "Vendedor no identificado"
                }
        }

    val isResolved: Boolean
        get() = effectiveStatus == IncidentStatus.RESOLVED
}