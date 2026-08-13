package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.runtime.Composable

@Composable
expect fun QrScannerCamera(
    onScan: (String) -> Unit,
    onClose: () -> Unit
)
