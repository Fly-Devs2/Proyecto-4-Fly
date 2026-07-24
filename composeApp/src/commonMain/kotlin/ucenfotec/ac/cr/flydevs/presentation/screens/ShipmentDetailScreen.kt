package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ucenfotec.ac.cr.flydevs.domain.model.Batch
import ucenfotec.ac.cr.flydevs.domain.model.BatchEvidence
import ucenfotec.ac.cr.flydevs.domain.model.Order
import ucenfotec.ac.cr.flydevs.domain.model.UserRole
import ucenfotec.ac.cr.flydevs.presentation.components.BottomNav
import ucenfotec.ac.cr.flydevs.presentation.components.FlyNavDestination
import ucenfotec.ac.cr.flydevs.presentation.components.FullScreenImageViewer
import ucenfotec.ac.cr.flydevs.presentation.components.PaginationBar
import ucenfotec.ac.cr.flydevs.presentation.components.TopBar
import ucenfotec.ac.cr.flydevs.presentation.shipmentDetail.ShipmentDetailViewModel
import ucenfotec.ac.cr.flydevs.presentation.theme.*
import ucenfotec.ac.cr.flydevs.presentation.util.formatDateTime

 @Composable
fun ShipmentDetailScreen(
    userRole: UserRole,
    batchId: String,
    onBack: () -> Unit = {},
    onNavSelect: (FlyNavDestination) -> Unit = {},
    viewModel: ShipmentDetailViewModel = koinViewModel(parameters = { parametersOf(batchId) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = BgDarkest,
            topBar = {
                Column {
                    Spacer(Modifier.statusBarsPadding())
                    TopBar(title = "Detalle del envío", onBack = onBack)
                }
            },
            bottomBar = {
                BottomNav(
                    userRole = userRole,
                    currentDestination = FlyNavDestination.Deliveries,
                    onDestinationSelected = onNavSelect,
                )
            },
        ) { padding ->
            val batch = uiState.batch

            when {
                uiState.isLoading -> Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = AccentViolet)
                }

                batch == null -> Box(
                    Modifier.fillMaxSize().padding(padding).padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        uiState.errorMessage ?: "Error desconocido",
                        color = AccentRed,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                    )
                }

                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                ) {
                    Spacer(Modifier.height(12.dp))

                    ReadOnlyNotice()

                    Spacer(Modifier.height(20.dp))

                    BatchIdentityHeader(batch)

                    Spacer(Modifier.height(20.dp))

                    ShipmentDataSection(batch)

                    Spacer(Modifier.height(24.dp))

                    ShipmentOrdersSection(
                        orders = uiState.visibleOrders,
                        totalOrders = uiState.totalOrders,
                        isLoadingOrders = uiState.isLoadingOrders,
                        page = uiState.ordersPage,
                        totalPages = uiState.totalOrderPages,
                        onPrevious = viewModel::previousOrdersPage,
                        onNext = viewModel::nextOrdersPage,
                    )

                    Spacer(Modifier.height(24.dp))

                    EvidenceSection(
                        title = "EVIDENCIA DE ENTREGA",
                        emptyMessage = "El mensajero no adjuntó foto de la entrega.",
                        evidence = batch.deliveryEvidence,
                        imageUrl = uiState.deliveryEvidenceUrl,
                        onViewImage = { fullScreenImageUrl = it },
                    )

                    Spacer(Modifier.height(16.dp))

                    EvidenceSection(
                        title = "EVIDENCIA DE RECOGIDA",
                        emptyMessage = "El mensajero no adjuntó foto de la recogida.",
                        evidence = batch.pickupEvidence,
                        imageUrl = uiState.pickupEvidenceUrl,
                        onViewImage = { fullScreenImageUrl = it },
                    )

                    Spacer(Modifier.height(20.dp))

                    PrivacyNotice()

                    Spacer(Modifier.height(40.dp))
                }
            }
        }

        FullScreenImageViewer(
            imageUrl = fullScreenImageUrl,
            onDismiss = { fullScreenImageUrl = null },
        )
    }
}

@Composable
private fun ReadOnlyNotice() {
    Surface(color = BgCard, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                tint = AccentGold,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Consulta de auditoría · solo lectura. Esta vista no permite editar ni confirmar nada.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp,
            )
        }
    }
}

@Composable
private fun BatchIdentityHeader(batch: Batch) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "Lote #${batchCode(batch.displayId)}",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${batch.orderCount} sobres sellados",
                color = TextSecondary,
                fontSize = 13.sp,
            )
        }

        BatchStatusBadge(batch.status)
    }
}

@Composable
private fun ShipmentDataSection(batch: Batch) {
    Surface(color = BgCard, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ShipmentSectionTitle("DATOS DEL ENVÍO")
            ShipmentDataItem("Origen", batch.sourceStoreName.ifBlank { "—" })
            if (batch.sourceStoreAddress.isNotBlank()) {
                ShipmentDataItem("Dirección origen", batch.sourceStoreAddress, TextSecondary)
            }
            ShipmentDataItem("Destino", batch.destinationStoreName.ifBlank { "—" })
            if (batch.destinationStoreAddress.isNotBlank()) {
                ShipmentDataItem("Dirección destino", batch.destinationStoreAddress, TextSecondary)
            }

            HorizontalDivider(color = BgSurface)

            ShipmentDataItem("Recogido", formatDateTime(batch.pickupAt))
            ShipmentDataItem(
                label = "Entregado",
                value = formatDateTime(batch.deliveredAt),
                valueColor = batchStatusColor(batch.status),
            )
        }
    }
}

@Composable
private fun ShipmentOrdersSection(
    orders: List<Order>,
    totalOrders: Int,
    isLoadingOrders: Boolean,
    page: Int,
    totalPages: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Column {
        ShipmentSectionTitle("SOBRES DEL LOTE · $totalOrders")

        Spacer(Modifier.height(8.dp))

        if (totalOrders == 0) {
            Text("Este lote no tiene sobres registrados.", color = TextMuted, fontSize = 13.sp)
            return@Column
        }

        if (isLoadingOrders) {
            Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentViolet, modifier = Modifier.size(28.dp))
            }
        } else if (orders.isEmpty()) {
            Text(
                "Los sobres de este lote ya no están disponibles.",
                color = TextMuted,
                fontSize = 13.sp,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                orders.forEach { order -> ShipmentOrderRow(order) }
            }
        }

        Spacer(Modifier.height(14.dp))

        PaginationBar(
            page = page,
            totalPages = totalPages,
            onPrevious = onPrevious,
            onNext = onNext,
        )
    }
}

// No es clicable ni muestra nombre/imagen de carta: privacidad exigida por el #38.
@Composable
private fun ShipmentOrderRow(order: Order) {
    Surface(color = BgCard, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Sobre #${batchCode(order.sobreId.ifBlank { order.id })}",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    "${order.cards.size} cartas selladas",
                    color = TextMuted,
                    fontSize = 12.sp,
                )
            }

            Text(
                order.status.label.uppercase(),
                color = AccentGold,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

@Composable
private fun EvidenceSection(
    title: String,
    emptyMessage: String,
    evidence: BatchEvidence?,
    imageUrl: String?,
    onViewImage: (String) -> Unit,
) {
    Surface(color = BgCard, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ShipmentSectionTitle(title)
                Text("Solo lectura", color = TextMuted, fontSize = 11.sp)
            }

            Spacer(Modifier.height(14.dp))

            if (evidence == null) {
                Text(emptyMessage, color = TextMuted, fontSize = 13.sp)
                return@Column
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                EvidenceThumbnail(
                    url = imageUrl,
                    onClick = { imageUrl?.let(onViewImage) },
                )

                Spacer(Modifier.width(14.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        formatDateTime(evidence.uploadedAt),
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    if (evidence.note.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(evidence.note, color = TextSecondary, fontSize = 12.sp)
                    }
                    if (imageUrl == null) {
                        Spacer(Modifier.height(4.dp))
                        Text("Foto no disponible.", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun EvidenceThumbnail(url: String?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(100.dp)
            .aspectRatio(1.2f)
            .clip(RoundedCornerShape(12.dp))
            .background(BgSurface)
            .clickable(enabled = url != null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = "Evidencia del mensajero",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(Icons.Default.Image, contentDescription = null, tint = TextMuted)
        }
    }
}

@Composable
private fun PrivacyNotice() {
    Text(
        "Por privacidad, esta consulta no incluye el nombre ni la imagen de las cartas, " +
            "ni el comprobante de SINPE Móvil.",
        color = TextMuted,
        fontSize = 11.sp,
        lineHeight = 16.sp,
    )
}

@Composable
private fun ShipmentSectionTitle(title: String) {
    Text(title, color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Black)
}

@Composable
private fun ShipmentDataItem(label: String, value: String, valueColor: Color = TextPrimary) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
