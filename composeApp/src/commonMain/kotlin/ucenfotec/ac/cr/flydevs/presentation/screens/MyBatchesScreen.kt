package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.domain.model.Batch
import ucenfotec.ac.cr.flydevs.domain.model.BatchStatus
import ucenfotec.ac.cr.flydevs.domain.model.UserRole
import ucenfotec.ac.cr.flydevs.presentation.myBatches.BatchGroupItem
import ucenfotec.ac.cr.flydevs.presentation.components.BottomNav
import ucenfotec.ac.cr.flydevs.presentation.components.FlyIconType
import ucenfotec.ac.cr.flydevs.presentation.components.FlyNavDestination
import ucenfotec.ac.cr.flydevs.presentation.components.Icon
import ucenfotec.ac.cr.flydevs.presentation.components.PaginationBar
import ucenfotec.ac.cr.flydevs.presentation.components.TopBar
import ucenfotec.ac.cr.flydevs.presentation.myBatches.MyBatchesViewModel
import ucenfotec.ac.cr.flydevs.presentation.theme.*
import ucenfotec.ac.cr.flydevs.presentation.util.formatDateTime

@Composable
fun MyBatchesScreen(
    userRole: UserRole,
    onBack: () -> Unit = {},
    onBatchClick: (String) -> Unit = {},
    onNavSelect: (FlyNavDestination) -> Unit = {},
    viewModel: MyBatchesViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BgDarkest,
        topBar = {
            Column {
                Spacer(Modifier.statusBarsPadding())
                TopBar(title = "Mis lotes", onBack = onBack)
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
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentViolet)
                }

                uiState.errorMessage != null -> Box(
                    Modifier.fillMaxSize().padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        uiState.errorMessage!!,
                        color = AccentRed,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                    )
                }

                uiState.groups.isEmpty() -> Box(
                    Modifier.fillMaxSize().padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Todavía no tenés lotes asignados.",
                        color = TextMuted,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Text(
                            "${uiState.totalBatches} lotes en ${uiState.groups.size} tiendas destino",
                            color = TextSecondary,
                            fontSize = 12.sp,
                        )
                    }

                    items(uiState.visibleGroups, key = { it.key }) { group ->
                        BatchGroupCard(
                            group = group,
                            isExpanded = uiState.isExpanded(group.key),
                            onToggle = { viewModel.toggleGroup(group.key) },
                            onBatchClick = onBatchClick,
                        )
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        PaginationBar(
                            page = uiState.page,
                            totalPages = uiState.totalPages,
                            onPrevious = viewModel::previousPage,
                            onNext = viewModel::nextPage,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BatchGroupCard(
    group: BatchGroupItem,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onBatchClick: (String) -> Unit,
) {
    Surface(color = BgCard, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "TIENDA DESTINO",
                        color = AccentGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        group.storeDestinationName,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${group.batchCount} lotes · ${group.orderCount} sobres · ${group.deliveredCount} entregados",
                        color = TextMuted,
                        fontSize = 12.sp,
                    )
                }

                Icon(
                    FlyIconType.ChevronDown,
                    Modifier.size(22.dp).rotate(if (isExpanded) 180f else 0f),
                    TextSecondary,
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    group.batches.forEach { batch ->
                        BatchRow(batch = batch, onClick = { onBatchClick(batch.documentId) })
                    }
                }
            }
        }
    }
}

@Composable
private fun BatchRow(batch: Batch, onClick: () -> Unit) {
    Surface(
        color = BgSurface,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Lote #${batchCode(batch.displayId)}",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    "${batch.orderCount} sobres · ${formatDateTime(batch.deliveredAt)}",
                    color = TextMuted,
                    fontSize = 12.sp,
                )
            }

            BatchStatusBadge(batch.status)
        }
    }
}

@Composable
internal fun BatchStatusBadge(status: BatchStatus) {
    val color = batchStatusColor(status)
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(50)) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(color))
            Spacer(Modifier.size(6.dp))
            Text(
                status.label.uppercase(),
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

internal fun batchStatusColor(status: BatchStatus): Color = when (status) {
    BatchStatus.DELIVERED -> AccentMint
    BatchStatus.PICKED_UP -> AccentGold
    BatchStatus.CANCELLED -> AccentRed
    BatchStatus.READY_FOR_PICKUP, BatchStatus.ACCEPTED -> AccentVioletLight
}

internal fun batchCode(batchId: String): String =
    if (batchId.isBlank()) "—" else batchId.take(7).uppercase()
