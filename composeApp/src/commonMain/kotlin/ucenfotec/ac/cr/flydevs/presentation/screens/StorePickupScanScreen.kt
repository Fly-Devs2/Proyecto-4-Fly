package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.presentation.components.QrScannerCamera
import ucenfotec.ac.cr.flydevs.presentation.storeHome.StorePickupScanViewModel
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentMint
import ucenfotec.ac.cr.flydevs.presentation.theme.BgDark
import ucenfotec.ac.cr.flydevs.presentation.util.cardsLabel

/**
 * Escaneo del QR de retiro en la tienda. Reutiliza la cámara del flujo de lotes
 * del mensajero; al validar, la orden pasa a "Carta recogida".
 */
@Composable
fun StorePickupScanRoute(
    onFinished: () -> Unit,
    onBack: () -> Unit,
    viewModel: StorePickupScanViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    QrScannerCamera(
        onScan = viewModel::onQrScanned,
        onClose = onBack,
    )

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = AccentMint)
        }
    }

    uiState.error?.let { message ->
        AlertDialog(
            onDismissRequest = {
                viewModel.clearError()
                onBack()
            },
            title = { Text("No se pudo validar el retiro") },
            text = { Text(message) },
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
            textContentColor = Color.White,
        )
    }

    if (uiState.pickedUpOrderId != null) {
        AlertDialog(
            onDismissRequest = onFinished,
            title = { Text("Retiro validado") },
            text = {
                Text(
                    "${cardsLabel(uiState.cardCount)} entregadas a ${uiState.buyerName}. " +
                        "Se notificó al cliente."
                )
            },
            confirmButton = {
                TextButton(onClick = onFinished) {
                    Text("Listo")
                }
            },
            containerColor = BgDark,
            titleContentColor = Color.White,
            textContentColor = Color.White,
        )
    }
}
