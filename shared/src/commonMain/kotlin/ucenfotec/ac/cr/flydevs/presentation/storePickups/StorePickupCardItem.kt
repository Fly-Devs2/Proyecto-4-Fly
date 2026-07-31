package ucenfotec.ac.cr.flydevs.presentation.storePickups

data class StorePickupCardItem(
    val cardId: String,
    val name: String,
    val rarity: String,
    val cardStatus: String,
    val userRole: String,
    val buyerName: String,
    val sellerName: String,
    val arrivalAt: Long,
    val arrivalDateLabel: String,
    val batchId: String,
    val orderId: String,
    val imageUrl: String,
    val condition: String,
    val game: String,
    val destinationStoreName: String,
) {
    val displayRole: String
        get() = if (userRole.equals("SELLER", ignoreCase = true)) "Vendedor" else "Comprador"

    val participantLabel: String
        get() = when {
            buyerName.isNotBlank() && sellerName.isNotBlank() -> "Comprador: $buyerName · Vendedor: $sellerName"
            buyerName.isNotBlank() -> "Comprador: $buyerName"
            sellerName.isNotBlank() -> "Vendedor: $sellerName"
            else -> "Sin participantes"
        }
}
