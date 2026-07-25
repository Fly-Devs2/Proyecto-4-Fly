package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ucenfotec.ac.cr.flydevs.domain.model.Order
import ucenfotec.ac.cr.flydevs.domain.model.OrderCardSnapshot
import ucenfotec.ac.cr.flydevs.domain.model.OrderStatus
import ucenfotec.ac.cr.flydevs.presentation.components.*
import ucenfotec.ac.cr.flydevs.presentation.orderDetail.OrderDetailViewModel

import ucenfotec.ac.cr.flydevs.presentation.theme.*
import ucenfotec.ac.cr.flydevs.domain.model.UserRole

@Composable
fun OrderDetailScreen(
    userRole: UserRole,
    orderId: String,
    onBack: () -> Unit = {},
    onNavigateToPay: (String) -> Unit = {},
    onNavigateToDeliver: (String) -> Unit = {},
    onNavSelect: (FlyNavDestination) -> Unit = {},
    viewModel: OrderDetailViewModel = koinViewModel(parameters = { parametersOf(orderId) })
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }
    var showCamera by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = BgDarkest,
            topBar = {
                Column {
                    Spacer(Modifier.statusBarsPadding())
                    TopBar(title = "Detalle de compra", onBack = onBack)
                }
            },
            bottomBar = {
                BottomNav(
                    userRole = userRole,
                    currentDestination = FlyNavDestination.Orders,
                    onDestinationSelected = onNavSelect
                )
            }
        ) { padding ->
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentViolet)
                }
            } else if (uiState.order != null) {
                val order = uiState.order!!
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(Modifier.height(16.dp))
                    
                    Text(
                        text = if (uiState.userRole == UserRole.SELLER)
                            "Estás viendo la compra como vendedor. Ambas partes ven este detalle en tiempo real."
                        else 
                            "Estás viendo la compra como comprador. Ambas partes ven este detalle en tiempo real.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(24.dp))

                    // Card Info List
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        order.cards.forEach { cardSnapshot ->
                            OrderCardInfo(cardSnapshot, order.status)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Data Section
                    OrderDataSection(order, uiState.userRole, uiState.sourceStoreName, uiState.destinationStoreName)

                    Spacer(Modifier.height(28.dp))

                    // Tracking Stepper
                    TrackingStepper(order.status, uiState.destinationStoreName)

                    Spacer(Modifier.height(32.dp))

                    // Section: COMPROBANTE SINPE
                    ComprobanteSinpeSection(
                        order = order,
                        role = uiState.userRole,
                        onNavigateToPay = onNavigateToPay,
                        onViewImage = { url -> fullScreenImageUrl = url }
                    )

                    Spacer(Modifier.height(24.dp))
                    if (order.sinpePaid) {
                        OrderQrAccessSection(
                            order = order,
                            onShowQr = { showQrDialog = true }
                        )

                        Spacer(Modifier.height(24.dp))
                    }

                    // --- Workflow Action Buttons ---
                    
                    if (uiState.userRole == UserRole.SELLER && order.status == OrderStatus.WAITING_SELLER_DELIVERY) {
                        PrimaryButton(
                            text = "Entregar en tienda",
                            onClick = { onNavigateToDeliver(order.id) },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                        )
                    }

                    // Section: EVIDENCIA
                    EvidenciaSection(
                        order = order,
                        role = uiState.userRole,
                        onNavigateToDeliver = onNavigateToDeliver,
                        onAddEvidence = { showCamera = true },
                        onViewImage = { url -> fullScreenImageUrl = url }
                    )
                    
                    Spacer(Modifier.height(40.dp))
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.errorMessage ?: "Error desconocido", color = AccentRed)
                }
            }
        }

        FullScreenImageViewer(
            imageUrl = fullScreenImageUrl,
            onDismiss = { fullScreenImageUrl = null }
        )

        if (showCamera) {
            CameraCaptureScreen(
                onImageCaptured = { image ->
                    showCamera = false
                    viewModel.onImagePicked(image)
                },
                onCancel = { showCamera = false }
            )
        }
        if (showQrDialog && uiState.order != null) {
            OrderQrDialog(
                order = uiState.order!!,
                onDismiss = { showQrDialog = false }
            )
        }
    }
}

@Composable
private fun OrderCardInfo(card: OrderCardSnapshot, status: OrderStatus) {
    Surface(
        color = BgCard,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(BgSurface),
                contentAlignment = Alignment.Center
            ) {
                if (card.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = card.imageUrl,
                        contentDescription = card.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Image, contentDescription = null, tint = TextMuted, modifier = Modifier.size(32.dp))
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("#C-2041", color = AccentViolet, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Text("✦ " + status.label.uppercase(), color = getOrderStatusAccent(status), fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
                val cardDisplayName = if (card.name.length > 50) card.name.take(47) + "..." else card.name
                Text(cardDisplayName, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                Text("₡${card.price}", color = AccentGold, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun OrderDataSection(order: Order, role: UserRole, sourceStore: String?, destinationStore: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("DATOS DE LA COMPRA")
        DataItem("Vendedor", order.sellerName)
        DataItem("Comprador", if (role == UserRole.BUYER) "Tú (@${order.buyerName})" else order.buyerName)
        DataItem("Tienda de origen", sourceStore ?: "Cargando...")
        DataItem("Tienda de destino", destinationStore ?: "Cargando...")
        DataItem("Sobre", "Sobre #${order.sobreId.take(7).uppercase()}")
        DataItem("Monto Total", "₡${order.montoTotal}", valueColor = AccentGold)
        DataItem("SINPE pagado", if (order.sinpePaid) "✓ SÍ" else "PENDIENTE", if (order.sinpePaid) AccentMint else AccentRed)
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

@Composable
private fun TrackingStepper(currentStatus: OrderStatus, destinationStoreName: String?) {
    Column {
        SectionTitle("SEGUIMIENTO")
        val steps = listOf(
            OrderStatus.WAITING_SELLER_DELIVERY,
            OrderStatus.WAITING_PAYMENT,
            OrderStatus.WAITING_STORE_SHIPMENT,
            OrderStatus.IN_TRANSIT,
            OrderStatus.DELIVERED_TO_STORE,
            OrderStatus.PICKED_UP
        )
        
        steps.forEachIndexed { index, step ->
            val dynamicMessage = if (step == OrderStatus.WAITING_STORE_SHIPMENT && currentStatus == step) {
                "Esperando envío a '${destinationStoreName ?: "..."}'"
            } else if (step == OrderStatus.IN_TRANSIT && currentStatus == step) {
                "En camino a '${destinationStoreName ?: "..."}'"
            } else if (step == OrderStatus.DELIVERED_TO_STORE && currentStatus == step) {
                "Listo para retirar en '${destinationStoreName ?: "..."}'"
            } else null

            TrackingStepItem(
                status = step,
                isCompleted = steps.indexOf(currentStatus) >= index,
                isCurrent = currentStatus == step,
                isLast = index == steps.size - 1,
                dynamicMessage = dynamicMessage
            )
        }
    }
}

@Composable
private fun TrackingStepItem(
    status: OrderStatus,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isLast: Boolean,
    dynamicMessage: String? = null
) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) AccentViolet else BgSurface),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) Text("✓", color = Color.White, fontSize = 10.sp, textAlign = TextAlign.Center)
            }
            if (!isLast) {
                Box(modifier = Modifier.width(2.dp).weight(1f).background(if (isCompleted) AccentViolet else BgSurface))
            }
        }
        
        Spacer(Modifier.width(16.dp))
        
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            val accent = getOrderStatusAccent(status)
                        Text(
                            text = status.label,
                            color = if (isCurrent) accent else if (isCompleted) TextPrimary else TextMuted,
                            fontSize = 14.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                        )
            if (dynamicMessage != null) {
                Text(
                    text = dynamicMessage,
                    color = AccentMint,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun ComprobanteSinpeSection(
    order: Order,
    role: UserRole,
    onNavigateToPay: (String) -> Unit,
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
                Text("COMPROBANTE SINPE", color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Black)
                if (order.sinpePaid) {
                    StatusBadge(text = "PAGADO", color = AccentMint)
                }
            }

            Spacer(Modifier.height(16.dp))

            if (order.sinpePaid) {
                ComprobanteCardPolished(order, onViewImage)
            } else if (role == UserRole.BUYER) {
                Button(
                    onClick = { onNavigateToPay(order.id) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentViolet),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Pagar con SINPE", style = Typography.labelLarge)
                }
            } else {
                Text("Esperando que el comprador adjunte el comprobante.", color = TextMuted, fontSize = 13.sp)
            }

            if (order.sinpePaid) {
                Spacer(Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Visible para el comprador y el vendedor. Solo el comprador puede adjuntar el SINPE.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ComprobanteCardPolished(order: Order, onViewImage: (String) -> Unit) {
    EvidenceCard(
        title = "Comprobante adjuntado",
        subtitle = "Por el comprador · 13 jun 2026 · 09:00",
        amount = "₡${order.montoTotal}",
        imageUrl = order.sinpeReceiptUrl,
        onClick = { order.sinpeReceiptUrl?.let { onViewImage(it) } }
    )
}

@Composable
private fun EvidenciaSection(
    order: Order,
    role: UserRole,
    onNavigateToDeliver: (String) -> Unit,
    onAddEvidence: () -> Unit,
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
                Text("EVIDENCIA", color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Black)
                Text("Ambos pueden adjuntar", color = TextMuted, fontSize = 11.sp)
            }

            Spacer(Modifier.height(16.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(order.buyerEvidenceUrls) { url ->
                    EvidencePreviewCard("COMPRADOR", url, onClick = { onViewImage(url) })
                }
                items(order.sellerEvidenceUrls) { url ->
                    EvidencePreviewCard("VENDEDOR", url, onClick = { onViewImage(url) })
                }
                
                item {
                    Box(
                        modifier = Modifier
                            .width(90.dp)
                            .aspectRatio(1.2f)
                    ) {
                        DashedAddButton(onClick = { 
                            if (role == UserRole.SELLER) {
                                onNavigateToDeliver(order.id)
                            } else {
                                onAddEvidence()
                            }
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun EvidencePreviewCard(label: String, url: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(90.dp).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.2f)
                .clip(RoundedCornerShape(12.dp))
                .background(BgSurface),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = url,
                contentDescription = label,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(Modifier.height(8.dp))
        Surface(
            color = BgDarkest,
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                label,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                color = AccentMint,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
            )
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
            Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun DashedButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .drawBehind {
                drawRoundRect(
                    color = AccentViolet.copy(alpha = 0.6f),
                    cornerRadius = CornerRadius(26.dp.toPx()),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                )
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = AccentVioletLight, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(text, color = AccentVioletLight, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DashedAddButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRoundRect(
                    color = AccentViolet.copy(alpha = 0.6f),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                )
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Add, contentDescription = null, tint = AccentVioletLight)
            Text("Añadir", color = AccentVioletLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun OrderQrAccessSection(
    order: Order,
    onShowQr: () -> Unit
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
                Column {
                    Text(
                        "QR DE RETIRO",
                        color = AccentGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        if (order.qrImageUrl.isNotBlank())
                            "Disponible para retirar en tienda."
                        else
                            "QR en preparación. Intenta de nuevo en unos segundos.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                if (order.qrImageUrl.isNotBlank()) {
                    StatusBadge(text = "LISTO", color = AccentMint)
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onShowQr,
                enabled = order.qrImageUrl.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentViolet,
                    disabledContainerColor = BgSurface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    text = if (order.qrImageUrl.isNotBlank())
                        "Ver QR de retiro"
                    else
                        "QR no disponible todavía",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun OrderQrDialog(
    order: Order,
    onDismiss: () -> Unit
) {
    if (!order.sinpePaid) return

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = BgCard,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "QR DE RETIRO",
                        color = AccentMint,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextPrimary
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (order.qrImageUrl.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .size(250.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = order.qrImageUrl,
                            contentDescription = "QR de retiro",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    Text(
                        "Mostrá este código en la tienda destino para retirar tu pedido.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(Modifier.height(18.dp))

                    Surface(
                        color = BgDarkest,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "Pedido #${order.id.take(7).uppercase()}",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(
                                "Vendedor: ${order.sellerName}",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    Text(
                        "El pago ya está completo, pero el QR todavía no está disponible.",
                        color = TextMuted,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
