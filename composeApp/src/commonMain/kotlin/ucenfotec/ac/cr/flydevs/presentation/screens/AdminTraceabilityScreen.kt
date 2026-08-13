package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ucenfotec.ac.cr.flydevs.domain.model.TraceStatusTone
import ucenfotec.ac.cr.flydevs.domain.model.TraceabilityRecord
import ucenfotec.ac.cr.flydevs.domain.model.TraceabilityTab
import ucenfotec.ac.cr.flydevs.domain.model.User
import ucenfotec.ac.cr.flydevs.domain.model.UserRole
import ucenfotec.ac.cr.flydevs.presentation.adminTraceability.AdminTraceabilityViewModel
import ucenfotec.ac.cr.flydevs.presentation.components.BottomNav
import ucenfotec.ac.cr.flydevs.presentation.components.FlyNavDestination
import ucenfotec.ac.cr.flydevs.presentation.components.TopBar
import ucenfotec.ac.cr.flydevs.presentation.theme.*
import ucenfotec.ac.cr.flydevs.presentation.util.formatColones
import ucenfotec.ac.cr.flydevs.presentation.util.formatDate

private const val PRIVACY_NOTE =
    "No se muestra información sensible."

@Composable
fun AdminTraceabilityScreen(
    userRole: UserRole,
    userId: String,
    onBack: () -> Unit,
    onNavSelect: (FlyNavDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminTraceabilityViewModel = koinViewModel(parameters = { parametersOf(userId) })
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDarkest)
            .statusBarsPadding()
    ) {
        TopBar(title = "Trazabilidad", onBack = onBack)

        if (state.isLoading) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentViolet)
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                UserSummaryCard(
                    user = state.user,
                    orderCount = state.summary.orderCount,
                    salesCount = state.summary.salesCount,
                    averageRating = state.summary.averageRating,
                    eventCount = state.summary.eventCount
                )

                CategoryTabs(
                    selected = state.selectedTab,
                    onSelect = viewModel::selectTab
                )

                ColumnHeader(leftLabel = state.selectedTab.columnHeader)

                RecordsTable(records = state.records)

                state.errorMessage?.let { message ->
                    Text(message, color = AccentRed, fontSize = 12.sp)
                }

                PrivacyNote()

                Spacer(Modifier.height(12.dp))
            }
        }

        BottomNav(
            userRole = userRole,
            currentDestination = FlyNavDestination.AdminUsers,
            onDestinationSelected = onNavSelect
        )
    }
}

@Composable
private fun UserSummaryCard(
    user: User?,
    orderCount: Int,
    salesCount: Int,
    averageRating: Double,
    eventCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BgCard)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BgSurface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initialsOf(user?.name),
                    color = AccentVioletLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = user?.name.orEmpty().ifBlank { "Usuario" },
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user?.email.orEmpty(),
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            RoleBadge(user?.userRole ?: UserRole.USER)
        }

        Row(Modifier.fillMaxWidth()) {
            SummaryStat("$orderCount", "Órdenes", TextPrimary, Modifier.weight(1f))
            SummaryStat("$salesCount", "Ventas", TextPrimary, Modifier.weight(1f))
            SummaryStat(formatRating(averageRating), "Rating", AccentGold, Modifier.weight(1f))
            SummaryStat("$eventCount", "Eventos", TextPrimary, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SummaryStat(value: String, label: String, valueColor: Color, modifier: Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = valueColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextSecondary, fontSize = 10.sp)
    }
}

@Composable
private fun RoleBadge(role: UserRole) {
    val label = when (role) {
        UserRole.DELIVERY -> "MENSAJERO"
        UserRole.STORE -> "TIENDA"
        UserRole.ADMIN -> "ADMIN"
        else -> "USUARIO"
    }
    val color = when (role) {
        UserRole.DELIVERY -> AccentGold
        UserRole.STORE -> AccentMint
        UserRole.ADMIN -> AccentRed
        else -> AccentVioletLight
    }

    Text(
        text = label,
        color = color,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 9.dp, vertical = 3.dp)
    )
}

@Composable
private fun CategoryTabs(
    selected: TraceabilityTab,
    onSelect: (TraceabilityTab) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TraceabilityTab.entries.forEach { tab ->
            val isSelected = tab == selected
            Text(
                text = tab.label,
                color = if (isSelected) Color.White else TextSecondary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (isSelected) AccentViolet else BgCard)
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 13.dp, vertical = 7.dp)
            )
        }
    }
}

@Composable
private fun ColumnHeader(leftLabel: String) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 2.dp)) {
        Text(leftLabel, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        Text("MONTO · ESTADO", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RecordsTable(records: List<TraceabilityRecord>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BgCard)
    ) {
        if (records.isEmpty()) {
            Text(
                text = "Sin registros en esta categoría.",
                color = TextMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(24.dp)
            )
            return@Column
        }

        records.forEachIndexed { index, record ->
            if (index > 0) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .height(1.dp)
                        .background(BgSurface)
                )
            }
            RecordRow(record)
        }
    }
}

@Composable
private fun RecordRow(record: TraceabilityRecord) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "#${record.code}",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${formatDate(record.timestamp)} · ${record.detail}",
                color = TextMuted,
                fontSize = 11.sp
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = record.amount?.let { formatColones(it) } ?: "—",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = record.statusLabel,
                color = record.statusTone.color(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PrivacyNote() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(13.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(PRIVACY_NOTE, color = TextMuted, fontSize = 11.sp)
    }
}

private fun TraceStatusTone.color(): Color = when (this) {
    TraceStatusTone.POSITIVE -> AccentMint
    TraceStatusTone.NEUTRAL -> AccentGold
    TraceStatusTone.NEGATIVE -> AccentRed
}

private fun initialsOf(name: String?): String {
    val parts = name.orEmpty().trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> "${parts[0].first()}${parts[1].first()}".uppercase()
    }
}

/** Una sola decimal, como en el wireframe (`4.8`). */
private fun formatRating(rating: Double): String {
    if (rating <= 0.0) return "—"
    val rounded = (rating * 10).toInt()
    return "${rounded / 10}.${rounded % 10}"
}
