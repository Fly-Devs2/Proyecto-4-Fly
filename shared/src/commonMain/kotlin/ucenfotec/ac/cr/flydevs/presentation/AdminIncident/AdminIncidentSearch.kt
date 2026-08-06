package ucenfotec.ac.cr.flydevs.presentation.AdminIncident

import ucenfotec.ac.cr.flydevs.domain.model.AdminIncidentFilter
import ucenfotec.ac.cr.flydevs.domain.model.AdminIncidentItem
import ucenfotec.ac.cr.flydevs.domain.model.IncidentStatus

internal fun List<AdminIncidentItem>.applyIncidentFilter(
    filter: AdminIncidentFilter
): List<AdminIncidentItem> {
    return when (filter) {
        AdminIncidentFilter.ALL ->
            this

        AdminIncidentFilter.OPEN ->
            filter {
                it.effectiveStatus == IncidentStatus.OPEN
            }

        AdminIncidentFilter.IN_REVIEW ->
            filter {
                it.effectiveStatus == IncidentStatus.IN_REVIEW
            }

        AdminIncidentFilter.RESOLVED ->
            filter {
                it.effectiveStatus == IncidentStatus.RESOLVED
            }
    }
}

internal fun List<AdminIncidentItem>.searchIncidents(
    query: String
): List<AdminIncidentItem> {
    val normalizedQuery =
        query.trim().lowercase()

    if (normalizedQuery.isBlank()) {
        return this
    }

    return filter { item ->
        val incident = item.incident

        listOf(
            incident.id,
            item.displayIncidentId,
            incident.orderId,
            incident.orderCode,
            incident.reporterName,
            incident.counterpartName,
            item.buyerName,
            item.sellerName,
            incident.description,
            incident.type.label
        )
            .plus(item.cardNames)
            .any { value ->
                value.lowercase()
                    .contains(normalizedQuery)
            }
    }
}