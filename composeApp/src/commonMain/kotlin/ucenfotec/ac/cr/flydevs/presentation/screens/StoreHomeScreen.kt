package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.domain.model.BatchStatus
import ucenfotec.ac.cr.flydevs.domain.model.DeliveryBatch
import ucenfotec.ac.cr.flydevs.domain.model.UserRole
import ucenfotec.ac.cr.flydevs.presentation.components.BottomNav
import ucenfotec.ac.cr.flydevs.presentation.components.FlyNavDestination
import ucenfotec.ac.cr.flydevs.presentation.storeHome.StoreHomeViewModel
import ucenfotec.ac.cr.flydevs.presentation.storeHome.StorePickup
import ucenfotec.ac.cr.flydevs.presentation.theme.*
import ucenfotec.ac.cr.flydevs.presentation.util.cardsLabel

@Composable
fun StoreHomeScreen(
    userRole: UserRole,
    onBatchClick: (String) -> Unit = {},
    onPickupClick: (String) -> Unit = {},
    onScanPickup: () -> Unit = {},
    onSeeAllPickups: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavSelect: (FlyNavDestination) -> Unit = {},
    viewModel: StoreHomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BgDarkest,
        bottomBar = {
            BottomNav(
                userRole = userRole,
                currentDestination = FlyNavDestination.Home,
                onDestinationSelected = onNavSelect,
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentMint)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            StoreHeader(
                storeName = uiState.storeName,
                unreadNotifications = uiState.unreadNotifications,
                onNotificationsClick = onNavigateToNotifications,
            )

            uiState.errorMessage?.let { message ->
                Spacer(Modifier.height(16.dp))
                Text(message, color = AccentRed, fontSize = 13.sp)
            }

            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StoreKpiCard(
                    modifier = Modifier.weight(1f),
                    value = uiState.batchesReceivedToday.toString(),
                    label = "Lotes recibidos hoy",
                    valueColor = AccentMint,
                )
                StoreKpiCard(
                    modifier = Modifier.weight(1f),
                    value = uiState.pickupsToday.toString(),
                    label = "Retiros hoy",
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StoreKpiCard(
                    modifier = Modifier.weight(1f),
                    value = uiState.pendingPickupsCount.toString(),
                    label = "Pendientes de retiro",
                )
                StoreKpiCard(
                    modifier = Modifier.weight(1f),
                    value = uiState.overduePickupsCount.toString(),
                    label = "Por vencer (+3 días)",
                    valueColor = AccentRed,
                )
            }

            Spacer(Modifier.height(16.dp))

            IncomingBatchesSection(
                batches = uiState.incomingBatches,
                onBatchClick = onBatchClick,
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onScanPickup,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentMint),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = BgDarkest,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Validar retiro (escanear QR)",
                    color = BgDarkest,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(16.dp))

            PendingPickupsSection(
                pickups = uiState.recentPendingPickups,
                totalPickups = uiState.pendingPickupsCount,
                onPickupClick = onPickupClick,
                onSeeAll = onSeeAllPickups,
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StoreHeader(
    storeName: String,
    unreadNotifications: Int,
    onNotificationsClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .border(2.dp, AccentMint, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Storefront,
                contentDescription = null,
                tint = AccentMint,
                modifier = Modifier.size(18.dp),
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = storeName.ifBlank { "Mi tienda" },
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Tienda asociada",
                color = TextSecondary,
                fontSize = 12.sp,
            )
        }

        IconButton(onClick = onNotificationsClick) {
            BadgedBox(
                badge = {
                    if (unreadNotifications > 0) {
                        Badge(containerColor = AccentRed) {
                            Text(if (unreadNotifications > 9) "9+" else "$unreadNotifications")
                        }
                    }
                },
            ) {
                Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = TextPrimary)
            }
        }

        Surface(color = AccentMint.copy(alpha = 0.16f), shape = RoundedCornerShape(20.dp)) {
            Text(
                text = "TIENDA",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                color = AccentMint,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun StoreKpiCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = TextPrimary,
) {
    Surface(color = BgCard, shape = RoundedCornerShape(14.dp), modifier = modifier) {
        Column(Modifier.padding(horizontal = 13.dp, vertical = 12.dp)) {
            Text(value, color = valueColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(label, color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun IncomingBatchesSection(
    batches: List<DeliveryBatch>,
    onBatchClick: (String) -> Unit,
) {
    Surface(color = BgCard, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(vertical = 13.dp)) {
            Text(
                "LOTES ENTRANTES",
                style = Typography.titleSmall,
                color = AccentGold,
                modifier = Modifier.padding(horizontal = 13.dp),
            )

            if (batches.isEmpty()) {
                Text(
                    "No hay lotes en camino a tu tienda.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 16.dp),
                )
                return@Column
            }

            batches.forEachIndexed { index, batch ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 13.dp).width(100.dp),
                        color = TextMuted.copy(alpha = 0.25f),
                    )
                }
                IncomingBatchRow(batch = batch, onClick = { onBatchClick(batch.id) })
            }
        }
    }
}

@Composable
private fun IncomingBatchRow(
    batch: DeliveryBatch,
    onClick: () -> Unit,
) {
    val label = batch.batchId.ifBlank { batchCode(batch.id) }
    val subtitle = when (batch.status) {
        BatchStatus.DELIVERED -> "En tienda · Origen: ${batch.sourceStoreName.ifBlank { "—" }}"
        BatchStatus.IN_TRANSIT, BatchStatus.PICKED_UP ->
            "En camino · Mensajero: ${batch.courierName?.ifBlank { null } ?: "Sin asignar"}"
        else -> "Programado · Origen: ${batch.sourceStoreName.ifBlank { "—" }}"
    }

    StoreListRow(
        icon = Icons.Default.Inventory2,
        iconTint = AccentGold,
        title = "Lote #$label · ${batch.orderIds.size} cartas",
        subtitle = subtitle,
        badgeText = incomingBatchBadgeLabel(batch.status),
        badgeColor = batchStatusColor(batch.status),
        onClick = onClick,
    )
}

@Composable
private fun PendingPickupsSection(
    pickups: List<StorePickup>,
    totalPickups: Int,
    onPickupClick: (String) -> Unit,
    onSeeAll: () -> Unit,
) {
    Surface(color = BgCard, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(vertical = 13.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 13.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("PENDIENTES DE RETIRO", style = Typography.titleSmall, color = AccentGold)
                if (totalPickups > pickups.size) {
                    Text(
                        "Ver todos",
                        color = AccentViolet,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onSeeAll),
                    )
                }
            }

            if (pickups.isEmpty()) {
                Text(
                    "No hay cartas esperando retiro.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 16.dp),
                )
                return@Column
            }

            pickups.forEachIndexed { index, pickup ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 13.dp).width(100.dp),
                        color = TextMuted.copy(alpha = 0.25f),
                    )
                }
                StorePickupRow(pickup = pickup, onClick = { onPickupClick(pickup.orderId) })
            }
        }
    }
}

@Composable
internal fun StorePickupRow(
    pickup: StorePickup,
    onClick: () -> Unit,
) {
    StoreListRow(
        icon = if (pickup.isOverdue) Icons.Default.Schedule else Icons.Default.Style,
        iconTint = if (pickup.isOverdue) AccentRed else AccentVioletLight,
        title = "#${orderCode(pickup.orderId)} · ${pickup.buyerName}",
        subtitle = "${cardsLabel(pickup.cardCount)} · ${waitingLabel(pickup.waitingDays)}",
        badgeText = if (pickup.isOverdue) "AVISAR" else "RETIRO",
        badgeColor = if (pickup.isOverdue) AccentRed else AccentViolet,
        onClick = onClick,
    )
}

@Composable
private fun StoreListRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    badgeText: String,
    badgeColor: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(BgSurface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }

        Spacer(Modifier.width(10.dp))

        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                subtitle,
                color = TextMuted,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.width(8.dp))

        Surface(color = badgeColor.copy(alpha = 0.16f), shape = RoundedCornerShape(50)) {
            Text(
                badgeText,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                color = badgeColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

private fun incomingBatchBadgeLabel(status: BatchStatus): String = when (status) {
    BatchStatus.READY_FOR_PICKUP, BatchStatus.ACCEPTED -> "PROGRAMADO"
    BatchStatus.PICKED_UP, BatchStatus.IN_TRANSIT -> "EN RUTA"
    BatchStatus.DELIVERED -> "EN TIENDA"
    BatchStatus.CANCELLED -> "CANCELADO"
}

internal fun orderCode(orderId: String): String =
    if (orderId.length > 7) orderId.take(7).uppercase() else orderId.uppercase()

internal fun waitingLabel(days: Int): String = when {
    days <= 0 -> "Llegó hoy"
    days == 1 -> "Esperando 1 día"
    else -> "Esperando $days días"
}
