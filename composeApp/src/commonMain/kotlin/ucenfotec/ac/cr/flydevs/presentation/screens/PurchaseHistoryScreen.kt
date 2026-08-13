package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.domain.model.Order
import ucenfotec.ac.cr.flydevs.domain.model.OrderStatus
import ucenfotec.ac.cr.flydevs.domain.model.UserRole
import ucenfotec.ac.cr.flydevs.presentation.components.BottomNav
import ucenfotec.ac.cr.flydevs.presentation.components.FlyNavDestination
import ucenfotec.ac.cr.flydevs.presentation.components.TopBar
import ucenfotec.ac.cr.flydevs.presentation.purchaseHistory.PurchaseHistoryFilter
import ucenfotec.ac.cr.flydevs.presentation.purchaseHistory.PurchaseSortOption
import ucenfotec.ac.cr.flydevs.presentation.purchaseHistory.PurchaseHistoryViewModel
import ucenfotec.ac.cr.flydevs.presentation.components.OrderItem
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentGold
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentMint
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentRed
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentViolet
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentVioletLight
import ucenfotec.ac.cr.flydevs.presentation.theme.BgCard
import ucenfotec.ac.cr.flydevs.presentation.theme.BgDarkest
import ucenfotec.ac.cr.flydevs.presentation.theme.BgSurface
import ucenfotec.ac.cr.flydevs.presentation.theme.TextMuted
import ucenfotec.ac.cr.flydevs.presentation.theme.TextPrimary
import ucenfotec.ac.cr.flydevs.presentation.theme.TextSecondary
import ucenfotec.ac.cr.flydevs.presentation.theme.getOrderStatusAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PurchaseHistoryScreen(
    userRole: UserRole,
    modifier: Modifier = Modifier,
    viewModel: PurchaseHistoryViewModel = koinViewModel(),
    onBack: () -> Unit = {},
    onOrderClick: (String) -> Unit = {},
    onNavSelect: (FlyNavDestination) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showSortMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDarkest)
            .statusBarsPadding(),
    ) {
        TopBar(title = "Historial de compras", onBack = onBack)

        // ── Contador ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "MIS COMPRAS · ${state.orders.size} pedidos",
                color = TextSecondary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Solo tuyas",
                color = AccentVioletLight,
                style = MaterialTheme.typography.labelSmall,
            )
        }

        // ── Filtros ───────────────────────────────────────────────────────────
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(PurchaseHistoryFilter.entries) { filter ->
                HistoryFilterChip(
                    label = filter.label,
                    selected = state.activeFilter == filter,
                    onClick = { viewModel.setFilter(filter) },
                )
            }
        }

        // ── Ordenar ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Ordenar",
                color = TextSecondary,
                style = MaterialTheme.typography.labelSmall,
            )
            PurchaseSortOption.entries.forEach { sort ->
                SortChipHistory(
                    label = sort.label,
                    selected = state.sortOption == sort,
                    onClick = { viewModel.setSortOption(sort) },
                )
            }
        }

        // ── Contenido ─────────────────────────────────────────────────────────
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentViolet)
                }
            }

            state.errorMessage != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            state.errorMessage ?: "",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(AccentViolet)
                                .clickable { viewModel.loadOrders() }
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                        ) {
                            Text(
                                "Reintentar",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            state.filteredOrders.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp),
                        )
                        Text(
                            "No tenés compras registradas",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.filteredOrders, key = { it.id }) { order ->
                        PurchaseOrderItem(
                            order = order,
                            onClick = { onOrderClick(order.id) },
                        )
                    }
                }
            }
        }

        BottomNav(
            userRole = userRole,
            currentDestination = FlyNavDestination.Orders,
            onDestinationSelected = onNavSelect,
        )
    }
}

@Composable
private fun HistoryFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        color = if (selected) TextPrimary else TextSecondary,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) AccentViolet else BgCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun SortChipHistory(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) BgSurface else BgCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            label,
            color = if (selected) TextPrimary else TextSecondary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
        if (selected) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = AccentViolet,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun PurchaseOrderItem(order: Order, onClick: () -> Unit) {
    val firstCard = order.cards.firstOrNull()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BgCard)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {

        // ── Imagen de la primera carta ─────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BgSurface),
                contentAlignment = Alignment.Center,
            ) {
                if (!firstCard?.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = firstCard!!.imageUrl,
                        contentDescription = firstCard.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Text("🃏", fontSize = 20.sp)
                }
            }
            Column {
                Text(
                    "#${order.id.take(6).uppercase()}",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
                if (firstCard != null) {
                    Text(
                        firstCard.name,
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            StatusBadgeOrder(order.status)
        }

        // ── Sobre ─────────────────────────────────────────────────────────
        if (order.sobreId.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("✉", fontSize = 12.sp)
                Text(
                    "Sobre ${order.sobreId} · ${order.cards.size} carta${if (order.cards.size != 1) "s" else ""}",
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }

        HorizontalDivider(color = BgSurface)

        // ── Vendedor / Comprador ───────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            LabelValue("Vendedor", order.sellerName.ifBlank { order.sellerId.take(8) })
            LabelValue("Comprador", order.buyerName.ifBlank { order.buyerId.take(8) })
        }

        // ── Fechas ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            LabelValue("Creado", formatDate(order.createdAt))
            LabelValue("Modificado", formatDate(order.modifiedAt))
        }

        // ── SINPE ─────────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "SINPE pagado",
                color = TextSecondary,
                style = MaterialTheme.typography.labelSmall,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                if (order.sinpePaid) "Sí ✓" else "No",
                color = if (order.sinpePaid) AccentMint else AccentRed,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun LabelValue(label: String, value: String) {
    Column {
        Text(label, color = TextMuted, style = MaterialTheme.typography.labelSmall)
        Text(
            value,
            color = TextPrimary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun StatusBadgeOrder(status: OrderStatus) {
    val accent = getOrderStatusAccent(status)
    val bg = accent.copy(alpha = 0.2f)
    Text(
        text = status.label.uppercase(),
        color = accent,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

private fun formatDate(timestamp: Long): String {
    if (timestamp == 0L) return "—"
    return try {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("es"))
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "—"
    }
}
