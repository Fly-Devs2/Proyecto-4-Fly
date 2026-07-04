package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.FileUpload
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
import ucenfotec.ac.cr.flydevs.domain.model.OrderStatus
import ucenfotec.ac.cr.flydevs.presentation.components.BottomNav
import ucenfotec.ac.cr.flydevs.presentation.components.FlyNavDestination
import ucenfotec.ac.cr.flydevs.presentation.components.TopBar
import ucenfotec.ac.cr.flydevs.presentation.orderDetail.OrderDetailViewModel
import ucenfotec.ac.cr.flydevs.presentation.orderDetail.UserRole
import ucenfotec.ac.cr.flydevs.presentation.theme.*

@Composable
fun OrderDetailScreen(
    orderId: String,
    onBack: () -> Unit = {},
    onNavigateToPay: (String) -> Unit = {},
    onNavigateToDeliver: (String) -> Unit = {},
    onNavSelect: (FlyNavDestination) -> Unit = {},
    viewModel: OrderDetailViewModel = koinViewModel(parameters = { parametersOf(orderId) })
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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

                // Card Info Card
                OrderCardInfo(order)

                Spacer(Modifier.height(24.dp))

                // Data Section
                OrderDataSection(order, uiState.userRole)

                Spacer(Modifier.height(28.dp))

                // Tracking Stepper
                TrackingStepper(order.status)

                Spacer(Modifier.height(32.dp))

                // Section: COMPROBANTE SINPE
                ComprobanteSinpeSection(
                    order = order,
                    role = uiState.userRole,
                    onNavigateToPay = onNavigateToPay
                )

                Spacer(Modifier.height(24.dp))

                // Section: EVIDENCIA
                EvidenciaSection(
                    order = order,
                    onNavigateToDeliver = onNavigateToDeliver
                )
                
                Spacer(Modifier.height(40.dp))
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.errorMessage ?: "Error desconocido", color = AccentRed)
            }
        }
    }
}

@Composable
private fun OrderCardInfo(order: Order) {
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
                if (order.cardImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = order.cardImageUrl,
                        contentDescription = order.cardName,
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
                    Text("✦ " + order.status.label.uppercase(), color = AccentGold, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
                val cardDisplayName = if (order.cardName.length > 50) order.cardName.take(47) + "..." else order.cardName
                Text(cardDisplayName, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                Text("₡${order.cardPrice}", color = AccentGold, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun OrderDataSection(order: Order, role: UserRole) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("DATOS DE LA COMPRA")
        DataItem("Vendedor", order.sellerName)
        DataItem("Comprador", if (role == UserRole.BUYER) "Tú (@${order.buyerName})" else order.buyerName)
        DataItem("Sobre", "Sobre #${order.sobreId.take(7).uppercase()}")
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
private fun TrackingStepper(currentStatus: OrderStatus) {
    Column {
        SectionTitle("SEGUIMIENTO")
        val steps = listOf(
            OrderStatus.RESERVED,
            OrderStatus.WAITING_SELLER_DELIVERY,
            OrderStatus.WAITING_PAYMENT,
            OrderStatus.IN_TRANSIT,
            OrderStatus.PICKED_UP
        )
        
        steps.forEachIndexed { index, step ->
            TrackingStepItem(
                status = step,
                isCompleted = steps.indexOf(currentStatus) >= index,
                isCurrent = currentStatus == step,
                isLast = index == steps.size - 1
            )
        }
    }
}

@Composable
private fun TrackingStepItem(status: OrderStatus, isCompleted: Boolean, isCurrent: Boolean, isLast: Boolean) {
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
            Text(
                text = status.label,
                color = if (isCurrent) AccentGold else if (isCompleted) TextPrimary else TextMuted,
                fontSize = 14.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ComprobanteSinpeSection(
    order: Order,
    role: UserRole,
    onNavigateToPay: (String) -> Unit
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
                ComprobanteCardPolished(order)
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
                        "Visible para el comprador y el vendedor. Solo el comprador puede adjuntar o reemplazar el SINPE.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }

                if (role == UserRole.BUYER) {
                    Spacer(Modifier.height(20.dp))
                    DashedButton(
                        text = "Reemplazar comprobante",
                        icon = Icons.Default.FileUpload,
                        onClick = { onNavigateToPay(order.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ComprobanteCardPolished(order: Order) {
    Surface(
        color = BgSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BgDarkest),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = AccentMint)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Comprobante adjuntado", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Por el comprador · 13 jun 2026 · 09:00", color = TextMuted, fontSize = 12.sp)
                Text("₡${order.cardPrice}", color = AccentMint, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, tint = TextMuted, contentDescription = null)
        }
    }
}

@Composable
private fun EvidenciaSection(
    order: Order,
    onNavigateToDeliver: (String) -> Unit
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

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EvidencePreviewCard("COMPRADOR", order.buyerEvidenceUrl != null)
                EvidencePreviewCard("VENDEDOR", order.sellerEvidenceUrl != null)
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1.5f)
                ) {
                    DashedAddButton(onClick = { onNavigateToDeliver(order.id) })
                }
            }
        }
    }
}

@Composable
private fun EvidencePreviewCard(label: String, hasImage: Boolean) {
    Column(
        modifier = Modifier.width(90.dp),
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
            Icon(Icons.Default.Image, contentDescription = null, tint = if (hasImage) AccentMint else TextMuted)
        }
        Spacer(Modifier.height(8.dp))
        Surface(
            color = BgDarkest,
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                label,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                color = if (hasImage) AccentMint else TextSecondary,
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
