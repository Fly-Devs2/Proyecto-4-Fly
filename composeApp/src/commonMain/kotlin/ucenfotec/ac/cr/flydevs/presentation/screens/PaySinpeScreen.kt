package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ucenfotec.ac.cr.flydevs.presentation.components.CameraCaptureScreen
import ucenfotec.ac.cr.flydevs.presentation.components.PhotoUploadZone
import ucenfotec.ac.cr.flydevs.presentation.components.PrimaryButton
import ucenfotec.ac.cr.flydevs.presentation.components.TopBar
import ucenfotec.ac.cr.flydevs.presentation.paySinpe.PaySinpeViewModel
import ucenfotec.ac.cr.flydevs.presentation.theme.*

@Composable
fun PaySinpeScreen(
    orderId: String,
    onBack: () -> Unit = {},
    onSuccess: () -> Unit = {},
    viewModel: PaySinpeViewModel = koinViewModel(parameters = { parametersOf(orderId) })
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCamera by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onSuccess()
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDarkest)
                .statusBarsPadding()
        ) {
            TopBar(title = "Pagar con SINPE", onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                
                Surface(color = BgCard, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Sobre #FA-1042", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text("SINPE Móvil a: 8888-8888 · CardKing CR", color = TextSecondary, fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Surface(color = BgSurface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("ℹ", color = AccentViolet, modifier = Modifier.padding(end = 12.dp))
                        Text("Recordá presentar el SINPE Móvil en la tienda destino al retirar.", color = TextPrimary, fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(28.dp))

                Text("COMPROBANTE SINPE · MÍN. 1", color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Black)
                
                Spacer(Modifier.height(12.dp))

                PhotoUploadZone(
                    onClick = { showCamera = true },
                    title = if (state.selectedImage != null) "Comprobante seleccionado" else "Añadir comprobante",
                    accentColor = if (state.selectedImage != null) AccentMint else AccentViolet
                )

                Spacer(Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = true, onCheckedChange = {}, colors = CheckboxDefaults.colors(checkedColor = AccentMint))
                    Text("Confirmo que el comprobante corresponde a este sobre", color = TextSecondary, fontSize = 12.sp)
                }

                Spacer(Modifier.height(32.dp))

                if (state.isLoading) {
                    CircularProgressIndicator(color = AccentViolet, modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    PrimaryButton(
                        text = "Enviar comprobante",
                        onClick = viewModel::uploadReceipt,
                        enabled = state.selectedImage != null
                    )
                }
                
                Spacer(Modifier.height(16.dp))

                Surface(color = Color(0xFF352424), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp)) {
                        Text("🕒", color = AccentGold, modifier = Modifier.padding(end = 12.dp))
                        Text("Plazo: jueves 12:00 md. Sin comprobante, el sobre vuelve a estar disponible.", color = AccentGold, fontSize = 12.sp)
                    }
                }
            }
        }

        if (showCamera) {
            CameraCaptureScreen(
                onImageCaptured = { image ->
                    showCamera = false
                    viewModel.onImagePicked(image)
                },
                onCancel = { showCamera = false }
            )
        }
    }
}
