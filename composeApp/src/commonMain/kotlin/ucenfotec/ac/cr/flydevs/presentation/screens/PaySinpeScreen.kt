package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import ucenfotec.ac.cr.flydevs.domain.model.Order
import ucenfotec.ac.cr.flydevs.presentation.components.*
import ucenfotec.ac.cr.flydevs.presentation.exchange.ExchangeBuyerViewModel
import ucenfotec.ac.cr.flydevs.presentation.exchange.SinpeProofFeedback
import ucenfotec.ac.cr.flydevs.presentation.theme.*

@Composable
fun PaySinpeScreen(
    exchangeId: String,
    modifier: Modifier = Modifier,
    viewModel: ExchangeBuyerViewModel = koinViewModel(),
    onBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showPicker by remember { mutableStateOf(false) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(exchangeId) { viewModel.load(exchangeId) }

    LaunchedEffect(state.feedback) {
        if (state.feedback == SinpeProofFeedback.SUCCESS) {
            onBack()
        }
    }

    Box(modifier = modifier.fillMaxSize().background(BgDarkest)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            TopBar(title = "Pagar con SINPE", onBack = onBack)

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentViolet)
                }
                return@Column
            }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {

                ExchangeStatusChip("Esperando comprobante", AccentGold)

                state.order?.let { SinpeOrderCard(it) }

                InfoBanner(
                    "Recordá presentar el SINPE Móvil en la tienda destino al retirar.",
                    AccentViolet,
                )

                if (!state.isReadyForSinpe) {
                    ExchangeStatusText(
                        "Esperá a que el vendedor entregue el sobre en tienda antes de subir el comprobante.",
                        AccentGold,
                    )
                }

                ExchangeSectionTitle("COMPROBANTE SINPE · MÍN. 1")

                PhotoUploadZone(
                    onClick = { showPicker = true },
                    title = if (state.proofUrl != null) "Comprobante listo" else "Subir comprobante SINPE",
                    subtitle = "JPG o PNG · máx. 5 MB",
                    accentColor = if (state.proofUrl != null) AccentMint else AccentViolet,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp),
                )

                if (state.proofUrl != null) {
                    Box(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                        EvidenceCard(
                            title = "Vista previa del comprobante",
                            subtitle = "Total a pagar",
                            amount = "₡${state.order?.montoTotal ?: 0L}",
                            imageUrl = state.proofUrl,
                            onClick = { fullScreenImageUrl = state.proofUrl }
                        )
                    }
                }

                state.imageError?.let { ExchangeStatusText(exchangeImageErrorText(it), AccentRed) }

                ExchangeStatusText("Adjuntá la captura del comprobante de SINPE Móvil.", TextMuted)

                ExchangeCheckRow(
                    checked = state.confirmChecked,
                    text = "Confirmo que el comprobante corresponde a este sobre",
                    onCheckedChange = viewModel::onConfirmChange,
                )

                state.feedback?.let { feedback ->
                    val color = if (feedback == SinpeProofFeedback.SUCCESS) AccentMint else AccentRed
                    ExchangeStatusText(sinpeFeedbackText(feedback), color)
                }

                InfoBanner(
                    "Plazo: jueves 12:00 md. Sin comprobante, el sobre vuelve a estar disponible.",
                    AccentGold,
                )

                PrimaryButton(
                    text = if (state.isSubmitting) "Enviando…" else "Enviar comprobante",
                    onClick = viewModel::submitProof,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
                )
            }
        }

        if (showPicker) {
            GalleryPicker(
                onImagePicked = { image ->
                    showPicker = false
                    viewModel.onImagePicked(image)
                },
                onCancel = { showPicker = false },
            )
        }

        FullScreenImageViewer(
            imageUrl = fullScreenImageUrl,
            onDismiss = { fullScreenImageUrl = null }
        )
    }
}

@Composable
private fun SinpeOrderCard(order: Order) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(BgCard)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val cardsCount = if (order.cards.isNotEmpty()) order.cards.size else 1
        Text(
            "Pedido #${envelopeCode(order.id)} · $cardsCount cartas",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
        )
        Text(
            "Total ₡${order.montoTotal}",
            color = AccentGold,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp
        )
        Text("Vendedor: ${order.sellerName}", color = TextSecondary, fontSize = 13.sp)
    }
}

@Composable
private fun InfoBanner(text: String, color: Color) {
    Text(
        text,
        color = TextPrimary,
        fontSize = 13.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    )
}

private fun sinpeFeedbackText(feedback: SinpeProofFeedback): String = when (feedback) {
    SinpeProofFeedback.SUCCESS -> "Comprobante enviado. ¡Gracias! Podés retirar tu carta en la tienda destino."
    SinpeProofFeedback.NOT_READY -> "Todavía no podés subir el comprobante: falta la evidencia del vendedor."
    SinpeProofFeedback.MISSING_PROOF -> "Subí el comprobante y confirmá que corresponde a este sobre."
    SinpeProofFeedback.SUBMIT_FAILED -> "No se pudo enviar el comprobante. Intentá de nuevo."
}
