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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ucenfotec.ac.cr.flydevs.presentation.components.*
import ucenfotec.ac.cr.flydevs.presentation.deliverStore.DeliverStoreViewModel
import ucenfotec.ac.cr.flydevs.presentation.theme.*

@Composable
fun DeliverStoreScreen(
    orderId: String,
    onBack: () -> Unit = {},
    onSuccess: () -> Unit = {},
    viewModel: DeliverStoreViewModel = koinViewModel(parameters = { parametersOf(orderId) })
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
            TopBar(title = "Entregar en tienda", onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                
                Surface(color = BgCard, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Sobre #FA-1042 · 2 cartas", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text("CardKing CR → Tienda Escazú", color = TextSecondary, fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(28.dp))

                Text("FOTO DE LA CARTA + SOBRE · MÍN. 1", color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Black)
                
                Spacer(Modifier.height(12.dp))

                PhotoUploadZone(
                    onClick = { showCamera = true },
                    title = if (state.selectedImage != null) "Foto capturada" else "Añadir foto",
                    subtitle = "La información del sobre debe quedar visible",
                    accentColor = if (state.selectedImage != null) AccentMint else AccentViolet
                )

                Spacer(Modifier.height(24.dp))

                FormField("NOTA PARA LA TIENDA (OPCIONAL)") {
                    TextField(
                        value = state.note,
                        placeholder = "Ej. Entregado en recepción, recibido por bodega...",
                        onValueChange = viewModel::onNoteChange,
                        minHeight = 80,
                        singleLine = false
                    )
                }

                Spacer(Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = true, onCheckedChange = {}, colors = CheckboxDefaults.colors(checkedColor = AccentMint))
                    Text("La información del sobre queda visible en la foto", color = TextSecondary, fontSize = 12.sp)
                }

                Spacer(Modifier.height(32.dp))

                if (state.isLoading) {
                    CircularProgressIndicator(color = AccentViolet, modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    PrimaryButton(
                        text = "Confirmar entrega",
                        onClick = viewModel::confirmDelivery,
                        enabled = state.selectedImage != null
                    )
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
