package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

@Composable
actual fun QrScannerCamera(
    onScan: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    
    val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .enableAutoZoom()
        .build()
        
    val scanner = GmsBarcodeScanning.getClient(context, options)

    LaunchedEffect(Unit) {
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                val rawValue = barcode.rawValue
                if (rawValue != null) {
                    onScan(rawValue)
                } else {
                    onClose()
                }
            }
            .addOnFailureListener {
                onClose()
            }
            .addOnCanceledListener {
                onClose()
            }
    }
}
