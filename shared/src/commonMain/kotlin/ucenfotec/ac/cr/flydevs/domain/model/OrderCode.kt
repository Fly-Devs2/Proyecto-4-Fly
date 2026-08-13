package ucenfotec.ac.cr.flydevs.domain.model

/** Código corto y público de una orden (`FA-1042`): los primeros 7 caracteres del id. */
fun orderCodeOf(orderId: String): String =
    if (orderId.length > 7) orderId.take(7).uppercase() else orderId.uppercase()
