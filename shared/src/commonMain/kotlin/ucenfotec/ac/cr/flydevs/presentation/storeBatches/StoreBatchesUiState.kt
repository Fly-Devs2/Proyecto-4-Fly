package ucenfotec.ac.cr.flydevs.presentation.storeBatches

import ucenfotec.ac.cr.flydevs.domain.model.DeliveryBatch

data class StoreBatchesUiState(
    val isLoading: Boolean = true,
    val batches: List<DeliveryBatch> = emptyList(),
    val storeId: String = "",
    val storeName: String = "",
    val errorMessage: String? = null
) {
    val totalBatches: Int get() = batches.size
    val totalCards: Int get() = batches.sumOf { it.orderIds.size }
    val totalDestinations: Int get() = batches.map { it.destinationStoreId }.distinct().size
    
    val qrPayload: String get() = "flydevs://store-qr?sid=$storeId"
    
    val qrImageUrl: String get() = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=$qrPayload"
}
