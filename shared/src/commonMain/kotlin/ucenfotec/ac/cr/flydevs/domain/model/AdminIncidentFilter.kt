package ucenfotec.ac.cr.flydevs.domain.model

enum class AdminIncidentFilter(
    val label: String
) {
    ALL("Todas"),
    OPEN("Abiertas"),
    IN_REVIEW("En investigación"),
    RESOLVED("Resueltas")
}