package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ucenfotec.ac.cr.flydevs.domain.model.BatchEvidence
import ucenfotec.ac.cr.flydevs.domain.model.BatchStatus
import ucenfotec.ac.cr.flydevs.domain.model.DeliveryBatch
import ucenfotec.ac.cr.flydevs.domain.model.Order
import ucenfotec.ac.cr.flydevs.domain.model.UserRole
import ucenfotec.ac.cr.flydevs.presentation.components.*
import ucenfotec.ac.cr.flydevs.presentation.shipmentDetail.ShipmentDetailViewModel
import ucenfotec.ac.cr.flydevs.presentation.theme.*
import ucenfotec.ac.cr.flydevs.presentation.util.formatDateTime

@Composable
fun ShipmentDetailScreen(
    userRole: UserRole,
    batchId: String,
    onBack: () -> Unit = {},
    onNavSelect: (FlyNavDestination) -> Unit = {},
    onTakePickupPhoto: (String) -> Unit = {},
    onTakeDeliveryPhoto: (String) -> Unit = {},
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
                    Spacer(Modifier.height(16.dp))

                    ReadOnlyNotice()

                    Spacer(Modifier.height(24.dp))

                    BatchIdentityHeader(batch)

                    Spacer(Modifier.height(24.dp))

                    ShipmentDataSection(
                        batch = batch,
                        userRole = userRole,
                        onTakePickupPhoto = onTakePickupPhoto,
                        onTakeDeliveryPhoto = onTakeDeliveryPhoto,
                        onStartRoute = viewModel::startRoute
                    )

                    Spacer(Modifier.height(28.dp))

                    // Orders list section
                    ShipmentOrdersSection(
                        orders = uiState.visibleOrders,
                        totalOrders = uiState.totalOrders,
                        isLoadingOrders = uiState.isLoadingOrders,
                        page = uiState.ordersPage,
                        totalPages = uiState.totalOrderPages,
                        onPrevious = viewModel::previousOrdersPage,
                        onNext = viewModel::nextOrdersPage,
                    )

                    Spacer(Modifier.height(32.dp))

                    // Evidence Sections matching OrderDetail style
                    BatchEvidencePolishedSection(
                        title = "EVIDENCIA DE RECOGIDA",
                        evidence = batch.pickupEvidence,
                        imageUrl = uiState.pickupEvidenceUrl,
                        emptyMessage = "El mensajero no adjuntó foto de la recogida.",
                        onViewImage = { fullScreenImageUrl = it }
                    )

                    Spacer(Modifier.height(24.dp))

                    BatchEvidencePolishedSection(
                        title = "EVIDENCIA DE ENTREGA",
                        evidence = batch.deliveryEvidence,
                        imageUrl = uiState.deliveryEvidenceUrl,
                        emptyMessage = "El mensajero no adjuntó foto de la entrega.",
                        onViewImage = { fullScreenImageUrl = it }
                    )

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
                "Consulta de auditoría · solo lectura. Los datos son gestionados por el sistema y el mensajero.",
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp,
            )
        }
    }
}

@Composable
private fun BatchIdentityHeader(batch: DeliveryBatch) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "Lote #${localBatchCode(batch.batchId.ifBlank { batch.id })}",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${batch.orderIds.size} sobres sellados",
                color = TextSecondary,
                fontSize = 13.sp,
            )
        }

        StatusBadge(text = batch.status.name.replace("_", " "), color = localBatchStatusColor(batch.status))
    }
}

@Composable
private fun ShipmentDataSection(
    batch: DeliveryBatch,
    userRole: UserRole,
    onTakePickupPhoto: (String) -> Unit,
    onTakeDeliveryPhoto: (String) -> Unit,
    onStartRoute: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("DATOS DEL LOTE")
        DataItem("Mensajero", batch.courierName ?: "—")
        DataItem("Tienda de origen", batch.sourceStoreName)
        DataItem("Tienda de destino", batch.destinationStoreName)
        DataItem("Pago mensajero", "₡${batch.courierReward}", valueColor = AccentGold)

        if (userRole == UserRole.DELIVERY && batch.status == BatchStatus.ACCEPTED) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onTakePickupPhoto(batch.id) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C54FF)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Tomar evidencia de recogida", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        if (userRole == UserRole.DELIVERY && batch.status == BatchStatus.PICKED_UP) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onStartRoute,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C54FF)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Iniciar ruta", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        if (userRole == UserRole.DELIVERY && batch.status == BatchStatus.IN_TRANSIT) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onTakeDeliveryPhoto(batch.id) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C54FF)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Tomar evidencia de entrega", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        if (batch.pickupAt != null) {
            DataItem("Recogido el", formatDateTime(batch.pickupAt!!))
        }
        if (batch.deliveredAt != null) {
            DataItem("Entregado el", formatDateTime(batch.deliveredAt!!), valueColor = AccentMint)
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
        SectionTitle("SOBRES INCLUIDOS · $totalOrders")

        Spacer(Modifier.height(8.dp))

        if (totalOrders == 0) {
            Text("Este lote no tiene sobres registrados.", color = TextMuted, fontSize = 13.sp)
            return@Column
        }

        if (isLoadingOrders) {
            Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentViolet, modifier = Modifier.size(28.dp))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                orders.forEach { order -> ShipmentOrderPolishedRow(order) }
            }
        }

        Spacer(Modifier.height(16.dp))

        PaginationBar(
            page = page,
            totalPages = totalPages,
            onPrevious = onPrevious,
            onNext = onNext,
        )
    }
}

@Composable
private fun ShipmentOrderPolishedRow(order: Order) {
    Surface(color = BgCard, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(BgSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Visibility, contentDescription = null, tint = AccentVioletLight, modifier = Modifier.size(20.dp))
            }
            
            Spacer(Modifier.width(14.dp))
            
            Column(Modifier.weight(1f)) {
                Text(
                    "Sobre #${localBatchCode(order.sobreId.ifBlank { order.id })}",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "${order.cards.size} cartas selladas",
                    color = TextMuted,
                    fontSize = 12.sp,
                )
            }

            StatusBadge(text = order.status.label.uppercase(), color = AccentGold)
        }
    }
}

@Composable
private fun BatchEvidencePolishedSection(
    title: String,
    evidence: BatchEvidence?,
    imageUrl: String?,
    emptyMessage: String,
    onViewImage: (String) -> Unit
) {
    Surface(
        color = BgCard,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Black)
                if (evidence != null) {
                    StatusBadge(text = "ADJUNTADA", color = AccentMint)
                }
            }

            Spacer(Modifier.height(16.dp))

            if (evidence == null) {
                Text(emptyMessage, color = TextMuted, fontSize = 13.sp)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EvidenceThumbnail(
                        url = imageUrl,
                        onClick = { imageUrl?.let(onViewImage) },
                    )

                    Spacer(Modifier.width(16.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            "Adjuntada el",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            formatDateTime(evidence.uploadedAt),
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        if (evidence.note.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                evidence.note,
                                color = TextMuted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
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
            .size(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(BgSurface)
            .clickable(enabled = url != null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = "Evidencia",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(Icons.Default.Image, contentDescription = null, tint = TextMuted)
        }
    }
}

@Composable
private fun StatusBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(50),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(6.6.dp))
            Text(text, color = color, fontSize = 9.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun DataItem(label: String, value: String, valueColor: Color = TextPrimary) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

private fun localBatchCode(id: String): String =
    if (id.isBlank()) "—" else id.takeLast(6).uppercase()

private fun localBatchStatusColor(status: BatchStatus): Color = when (status) {
    BatchStatus.READY_FOR_PICKUP -> AccentViolet
    BatchStatus.ACCEPTED -> AccentGold
    BatchStatus.PICKED_UP -> AccentBlue
    BatchStatus.IN_TRANSIT -> AccentVioletLight
    BatchStatus.DELIVERED -> AccentMint
    BatchStatus.CANCELLED -> AccentRed
}
