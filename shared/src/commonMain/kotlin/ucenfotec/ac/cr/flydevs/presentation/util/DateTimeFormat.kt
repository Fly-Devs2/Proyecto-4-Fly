package ucenfotec.ac.cr.flydevs.presentation.util

import kotlin.time.Clock

private const val COSTA_RICA_OFFSET_SECONDS = -6L * 3600L
private const val SECONDS_PER_DAY = 86_400L

private val MONTH_ABBREVIATIONS = listOf(
    "ene", "feb", "mar", "abr", "may", "jun",
    "jul", "ago", "sep", "oct", "nov", "dic",
)

/** Formatea un epoch en milisegundos como `13 jun 2026 · 09:00` (hora de Costa Rica). */
fun formatDateTime(epochMillis: Long, placeholder: String = "—"): String {
    if (epochMillis <= 0L) return placeholder

    val localSeconds = epochMillis / 1000L + COSTA_RICA_OFFSET_SECONDS
    val days = floorDiv(localSeconds, SECONDS_PER_DAY)
    val secondOfDay = localSeconds - days * SECONDS_PER_DAY
    val (year, month, dayOfMonth) = civilFromDays(days)

    val hours = (secondOfDay / 3600L).toString().padStart(2, '0')
    val minutes = (secondOfDay % 3600L / 60L).toString().padStart(2, '0')

    return "$dayOfMonth ${MONTH_ABBREVIATIONS[month - 1]} $year · $hours:$minutes"
}

/** Formatea solo la fecha: `13 jun 2026`. */
fun formatDate(epochMillis: Long, placeholder: String = "—"): String {
    if (epochMillis <= 0L) return placeholder
    return formatDateTime(epochMillis).substringBefore(" · ")
}

/** `true` si ambos epochs caen en el mismo día natural de Costa Rica. */
fun isSameDay(firstEpochMillis: Long, secondEpochMillis: Long): Boolean {
    if (firstEpochMillis <= 0L || secondEpochMillis <= 0L) return false
    return localDay(firstEpochMillis) == localDay(secondEpochMillis)
}

/** Días naturales completos transcurridos entre dos epochs (hora de Costa Rica). */
fun daysBetween(fromEpochMillis: Long, toEpochMillis: Long): Int {
    if (fromEpochMillis <= 0L || toEpochMillis <= 0L) return 0
    return (localDay(toEpochMillis) - localDay(fromEpochMillis)).toInt()
}

private fun localDay(epochMillis: Long): Long =
    floorDiv(epochMillis / 1000L + COSTA_RICA_OFFSET_SECONDS, SECONDS_PER_DAY)

private fun floorDiv(value: Long, divisor: Long): Long {
    val quotient = value / divisor
    return if (value % divisor != 0L && (value xor divisor) < 0L) quotient - 1 else quotient
}

/** Algoritmo `civil_from_days` de Howard Hinnant: días desde epoch → (año, mes, día). */
private fun civilFromDays(daysSinceEpoch: Long): Triple<Int, Int, Int> {
    val z = daysSinceEpoch + 719_468L
    val era = floorDiv(z, 146_097L)
    val dayOfEra = z - era * 146_097L
    val yearOfEra = (dayOfEra - dayOfEra / 1_460L + dayOfEra / 36_524L - dayOfEra / 146_096L) / 365L
    val year = yearOfEra + era * 400L
    val dayOfYear = dayOfEra - (365L * yearOfEra + yearOfEra / 4L - yearOfEra / 100L)
    val mp = (5L * dayOfYear + 2L) / 153L
    val day = dayOfYear - (153L * mp + 2L) / 5L + 1L
    val month = if (mp < 10L) mp + 3L else mp - 9L

    return Triple(
        (if (month <= 2L) year + 1L else year).toInt(),
        month.toInt(),
        day.toInt(),
    )
}
fun getCurrentTimeMillis(): Long {
    return Clock.System.now().toEpochMilliseconds()
}
