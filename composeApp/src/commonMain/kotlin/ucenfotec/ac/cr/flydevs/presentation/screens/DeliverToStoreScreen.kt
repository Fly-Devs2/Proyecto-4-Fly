package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.domain.model.Order
import ucenfotec.ac.cr.flydevs.presentation.components.CameraCaptureScreen
import ucenfotec.ac.cr.flydevs.presentation.components.PhotoUploadZone
import ucenfotec.ac.cr.flydevs.presentation.components.PrimaryButton
import ucenfotec.ac.cr.flydevs.presentation.components.TopBar
import ucenfotec.ac.cr.flydevs.presentation.exchange.ExchangeSellerViewModel
import ucenfotec.ac.cr.flydevs.presentation.exchange.SellerEvidenceFeedback
import ucenfotec.ac.cr.flydevs.presentation.publishGameCard.ImageError
import ucenfotec.ac.cr.flydevs.presentation.theme.*

@Composable
fun DeliverToStoreScreen(
    exchangeId: String,
    modifier: Modifier = Modifier,
    viewModel: ExchangeSellerViewModel = koinViewModel(),
    onBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCamera by remember { mutableStateOf(false) }

    LaunchedEffect(exchangeId) { viewModel.load(exchangeId) }

    Box(modifier = modifier.fillMaxSize().background(BgDarkest)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            TopBar(title = "Entregar en tienda", onBack = onBack)

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentViolet)
                }
                return@Column
            }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {

                ExchangeStatusChip("Esperando entrega", AccentGold)

                state.order?.let { OrderEnvelopeCard(it) }

                ExchangeSectionTitle("FOTO DE LA CARTA + SOBRE · MÍN. 1")

                PhotoUploadZone(
                    onClick = { showCamera = true },
                    title = if (state.evidenceUrl != null) "Foto lista" else "Subir foto de la carta + sobre",
                    subtitle = "JPG o PNG · máx. 5 MB",
                    accentColor = if (state.evidenceUrl != null) AccentMint else AccentViolet,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp),
                )
                state.imageError?.let { ExchangeStatusText(exchangeImageErrorText(it), AccentRed) }

                ExchangeStatusText("La información del sobre debe quedar visible en la foto.", TextMuted)

                ExchangeCheckRow(
                    checked = state.infoVisibleChecked,
                    text = "La información del sobre queda visible en la foto",
                    onCheckedChange = viewModel::onInfoVisibleChange,
                )

                state.feedback?.let { feedback ->
                    val color = if (feedback == SellerEvidenceFeedback.SUCCESS) AccentMint else AccentRed
                    ExchangeStatusText(sellerFeedbackText(feedback), color)
                }

                PrimaryButton(
                    text = if (state.isSubmitting) "Enviando…" else "Confirmar entrega",
                    onClick = viewModel::confirmDelivery,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
                )
            }
        }

        if (showCamera) {
            CameraCaptureScreen(
                onImageCaptured = { image ->
                    showCamera = false
                    viewModel.onImagePicked(image)
                },
                onCancel = { showCamera = false },
            )
        }
    }
}

private fun sellerFeedbackText(feedback: SellerEvidenceFeedback): String = when (feedback) {
    SellerEvidenceFeedback.SUCCESS -> "Evidencia enviada. Ahora se le pedirá el comprobante SINPE al comprador."
    SellerEvidenceFeedback.MISSING_EVIDENCE -> "Subí la foto y confirmá que la info del sobre queda visible."
    SellerEvidenceFeedback.SUBMIT_FAILED -> "No se pudo enviar la evidencia. Intentá de nuevo."
}

internal fun exchangeImageErrorText(error: ImageError): String = when (error) {
    ImageError.UPLOAD_FAILED -> "No se pudo subir la foto. Intentá de nuevo."
    ImageError.REQUIRED -> "La foto es obligatoria."
}

// ── Piezas de UI compartidas del flujo de intercambio ──

@Composable
internal fun ExchangeStatusChip(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(start = 20.dp, top = 12.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Composable
internal fun OrderEnvelopeCard(order: Order) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(BgCard)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            "Pedido #${envelopeCode(order.id)}",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
        )
        Text("Vendedor: ${order.sellerName}", color = TextSecondary, fontSize = 13.sp)
    }
}

@Composable
internal fun ExchangeSectionTitle(text: String) {
    Text(
        text,
        color = AccentGold,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
    )
}

@Composable
internal fun ExchangeStatusText(message: String, color: Color) {
    Text(
        message,
        color = color,
        fontSize = 12.sp,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp),
    )
}

@Composable
internal fun ExchangeCheckRow(checked: Boolean, text: String, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 20.dp, top = 8.dp)
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = AccentMint, uncheckedColor = TextMuted),
        )
        Text(text, color = TextSecondary, fontSize = 13.sp)
    }
}

internal fun envelopeCode(id: String): String =
    if (id.isBlank()) "—" else id.take(6).uppercase()
