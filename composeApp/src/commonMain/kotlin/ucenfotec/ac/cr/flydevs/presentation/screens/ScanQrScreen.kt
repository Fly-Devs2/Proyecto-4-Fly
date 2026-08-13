package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.presentation.components.QrScannerCamera
import ucenfotec.ac.cr.flydevs.presentation.batch.ScanQrViewModel

private val BgDark = Color(0xFF1A1535)
private val MessengerPurple = Color(0xFF7C54FF)

@Composable
fun ScanQrRoute(
    viewModel: ScanQrViewModel = koinViewModel(),
    onSuccess: (batchId: String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.successBatchId) {
        val batchId = uiState.successBatchId
        if (batchId != null) {
            if (batchId.startsWith("STORE_SUCCESS:")) {
                viewModel.clearSuccess()
                onBack()
            } else {
                onSuccess(batchId)
                viewModel.clearSuccess()
            }
        }
    }

    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = {
                viewModel.clearError()
                onBack()
            },
            title = { Text("Error") },
            text = { Text(uiState.error!!) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearError()
                    onBack()
                }) {
                    Text("Aceptar")
                }
            },
            containerColor = BgDark,
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MessengerPurple)
        }
    }

    QrScannerCamera(
        onScan = viewModel::onQrScanned,
        onClose = onBack
    )
}
