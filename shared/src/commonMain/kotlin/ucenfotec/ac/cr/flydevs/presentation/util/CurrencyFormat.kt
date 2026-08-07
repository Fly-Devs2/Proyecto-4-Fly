package ucenfotec.ac.cr.flydevs.presentation.util

/** Formatea un monto en colones con separador de miles por espacio: `₡625 500`. */
fun formatColones(amount: Long): String {
    val digits = amount.toString()
    val negative = digits.startsWith("-")
    val grouped = digits.removePrefix("-")
        .reversed()
        .chunked(3)
        .joinToString(" ")
        .reversed()

    return if (negative) "-₡$grouped" else "₡$grouped"
}
