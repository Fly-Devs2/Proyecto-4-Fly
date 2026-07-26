package ucenfotec.ac.cr.flydevs.presentation.batch

data class ScanQrUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successBatchId: String? = null
)
