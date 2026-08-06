package ucenfotec.ac.cr.flydevs.domain.model

data class AdminIncidentItem(
    val incident: Incident,

    val cardNames: List<String> = emptyList(),

    val buyerId: String = "",
    val buyerName: String = "",

    val sellerId: String = "",
    val sellerName: String = ""
) {
    val displayIncidentId: String
        get() = incident.id
            .takeLast(8)
            .uppercase()
            .let { "INC-$it" }

    val primaryCardName: String
        get() = when {
            cardNames.isEmpty() ->
                "Carta no identificada"

            cardNames.size == 1 ->
                cardNames.first()

            else ->
                "${cardNames.first()} y ${cardNames.size - 1} más"
        }

    /**
     * Compatibilidad con documentos antiguos donde URGENT
     * todavía estaba guardado como estado.
     */
    val effectiveStatus: IncidentStatus
        get() = if (
            incident.status == IncidentStatus.URGENT
        ) {
            IncidentStatus.OPEN
        } else {
            incident.status
        }

    val effectivePriority: IncidentPriority
        get() = if (
            incident.status == IncidentStatus.URGENT
        ) {
            IncidentPriority.URGENT
        } else {
            incident.priority
        }
}