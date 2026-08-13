package ucenfotec.ac.cr.flydevs.domain.model

/** Categorías de trazabilidad que el administrador puede consultar de un usuario. */
enum class TraceabilityTab(val label: String) {
    ORDERS("Órdenes"),
    SALES("Ventas"),
    RATINGS("Ratings"),
    ACTIVITY("Actividad");

    /** Encabezado de la columna izquierda de la tabla. */
    val columnHeader: String
        get() = when (this) {
            ORDERS -> "PEDIDO"
            SALES -> "VENTA"
            RATINGS -> "RATING"
            ACTIVITY -> "EVENTO"
        }
}

/** Semántica del estado de un registro; la pantalla la traduce a color. */
enum class TraceStatusTone { POSITIVE, NEUTRAL, NEGATIVE }

/**
 * Una fila de la tabla de trazabilidad.
 *
 * Solo contiene datos públicos del registro: nunca teléfonos, comprobantes SINPE,
 * evidencias ni firmas de QR.
 */
data class TraceabilityRecord(
    val code: String,
    val timestamp: Long,
    /** Texto corto que acompaña la fecha, p. ej. `3 cartas` o `Rating recibido`. */
    val detail: String,
    /** `null` cuando el evento no tiene monto asociado. */
    val amount: Long?,
    val statusLabel: String,
    val statusTone: TraceStatusTone,
)

data class TraceabilitySummary(
    val orderCount: Int = 0,
    val salesCount: Int = 0,
    val averageRating: Double = 0.0,
    val eventCount: Int = 0,
)

data class UserTraceability(
    val user: User? = null,
    val summary: TraceabilitySummary = TraceabilitySummary(),
    val records: Map<TraceabilityTab, List<TraceabilityRecord>> = emptyMap(),
) {
    fun recordsFor(tab: TraceabilityTab): List<TraceabilityRecord> = records[tab].orEmpty()
}
