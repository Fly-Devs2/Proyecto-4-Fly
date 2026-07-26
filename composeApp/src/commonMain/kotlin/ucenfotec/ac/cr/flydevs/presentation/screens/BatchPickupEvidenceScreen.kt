package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.domain.model.BatchStatus
import ucenfotec.ac.cr.flydevs.domain.model.DeliveryBatch
import ucenfotec.ac.cr.flydevs.presentation.batch.BatchEvidenceFeedback
import ucenfotec.ac.cr.flydevs.presentation.batch.BatchEvidenceViewModel
import ucenfotec.ac.cr.flydevs.presentation.components.*
import ucenfotec.ac.cr.flydevs.presentation.theme.*

@Composable
fun BatchPickupEvidenceRoute(
    batchId: String,
    viewModel: BatchEvidenceViewModel = koinViewModel(),
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCamera by remember { mutableStateOf(false) }

    LaunchedEffect(batchId) {
        viewModel.loadBatch(batchId)
    }

    LaunchedEffect(uiState.feedback) {
        if (uiState.feedback == BatchEvidenceFeedback.SUCCESS) {
            onSuccess()
            viewModel.clearFeedback()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDarkest)) {
        BatchPickupEvidenceScreen(
            uiState = uiState,
            onNoteChange = viewModel::onNoteChange,
            onConfirmChange = viewModel::onConfirmChange,
            onPickImage = { showCamera = true },
            onSubmit = viewModel::submitEvidence,
            onBack = onBack,
            onClearFeedback = viewModel::clearFeedback
        )

        if (showCamera) {
            CameraCaptureScreen(
                onImageCaptured = { image ->
                    viewModel.onImagePicked(image)
                    showCamera = false
                },
                onCancel = { showCamera = false }
            )
        }
    }
}

@Composable
fun BatchPickupEvidenceScreen(
    uiState: ucenfotec.ac.cr.flydevs.presentation.batch.BatchEvidenceUiState,
    onNoteChange: (String) -> Unit,
    onConfirmChange: (Boolean) -> Unit,
    onPickImage: () -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    onClearFeedback: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.feedback) {
        uiState.feedback?.let {
            if (it != BatchEvidenceFeedback.SUCCESS) {
                snackbarHostState.showSnackbar(
                    when (it) {
                        BatchEvidenceFeedback.MISSING_PHOTO -> "Debes subir al menos una foto de la recogida."
                        BatchEvidenceFeedback.MISSING_CONFIRM -> "Debes confirmar la recogida del lote."
                        BatchEvidenceFeedback.SUBMIT_FAILED -> uiState.error ?: "Error al enviar la evidencia."
                    }
                )
                onClearFeedback()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        TopBar(title = "Evidencia de Recogida", onBack = onBack)

        if (uiState.isLoading || uiState.batch == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentViolet)
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // QR Validated Chip
                Surface(
                    color = AccentMint.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = AccentMint,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.6.dp))
                        Text(
                            text = "QR validado",
                            color = AccentMint,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Batch Summary Card
                uiState.batch?.let { batch ->
                    BatchSummaryCard(batch)
                }

                // Photo Section
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "FOTOS DE LA RECOGIDA · MÍN. 1",
                        color = AccentGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (uiState.proofUrl != null) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BgCard)
                            ) {
                                AsyncImage(
                                    model = uiState.proofUrl,
                                    contentDescription = "Evidencia",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        if (!uiState.isUploadingImage && uiState.proofUrl == null) {
                            PhotoUploadZone(
                                onClick = onPickImage,
                                title = "Añadir",
                                subtitle = "",
                                modifier = Modifier.size(100.dp),
                                accentColor = AccentViolet
                            )
                        } else if (uiState.isUploadingImage) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BgCard),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = AccentViolet)
                            }
                        }
                    }

                    Text(
                        text = "Toma una foto del lote siendo entregado por la tienda de origen.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                // Note Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "NOTA PARA LA TIENDA (OPCIONAL)",
                        color = AccentGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextField(
                        value = uiState.note,
                        placeholder = "Ej. Entregado en recepción, recibido por bodega...",
                        onValueChange = onNoteChange,
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        singleLine = false,
                        minHeight = 100
                    )
                }

                // Confirmation Checkbox
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onConfirmChange(!uiState.confirmChecked) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = uiState.confirmChecked,
                        onCheckedChange = onConfirmChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = AccentMint,
                            uncheckedColor = TextMuted
                        )
                    )
                    Text(
                        text = "Confirmo la recogida del lote en la tienda origen",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val buttonEnabled = !uiState.isSubmitting && !uiState.isLoading
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (buttonEnabled) AccentMint else AccentMint.copy(alpha = 0.5f))
                        .then(if (buttonEnabled) Modifier.clickable(onClick = onSubmit) else Modifier),
                    contentAlignment = Alignment.Center,
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(color = BgDarkest, modifier = Modifier.size(24.dp))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, null, tint = BgDarkest, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Confirmar Recogida",
                                color = BgDarkest,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
        
        SnackbarHost(hostState = snackbarHostState)
    }
}

@Composable
private fun BatchSummaryCard(batch: DeliveryBatch) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Lote #${localBatchCode(batch.batchId.ifBlank { batch.id })} · ${batch.orderIds.size} sobres sellados",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = batch.sourceStoreName,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp).padding(horizontal = 4.dp)
                )
                Text(
                    text = batch.destinationStoreName,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun localBatchCode(id: String): String =
    if (id.isBlank()) "—" else id.takeLast(6).uppercase()
