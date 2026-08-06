package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.domain.model.AdminIncidentFilter
import ucenfotec.ac.cr.flydevs.domain.model.AdminIncidentItem
import ucenfotec.ac.cr.flydevs.domain.model.IncidentPriority
import ucenfotec.ac.cr.flydevs.domain.model.IncidentStatus
import ucenfotec.ac.cr.flydevs.domain.model.UserRole
import ucenfotec.ac.cr.flydevs.getEpochMillis
import ucenfotec.ac.cr.flydevs.presentation.AdminIncident.AdminIncidentsViewModel
import ucenfotec.ac.cr.flydevs.presentation.components.BottomNav
import ucenfotec.ac.cr.flydevs.presentation.components.FlyNavDestination
import ucenfotec.ac.cr.flydevs.presentation.components.TopBar
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentGold
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentMint
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentRed
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentViolet
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentVioletLight
import ucenfotec.ac.cr.flydevs.presentation.theme.BgCard
import ucenfotec.ac.cr.flydevs.presentation.theme.BgDark
import ucenfotec.ac.cr.flydevs.presentation.theme.BgDarkest
import ucenfotec.ac.cr.flydevs.presentation.theme.BgSurface
import ucenfotec.ac.cr.flydevs.presentation.theme.TextMuted
import ucenfotec.ac.cr.flydevs.presentation.theme.TextPrimary
import ucenfotec.ac.cr.flydevs.presentation.theme.TextSecondary

@Composable
fun AdminIncidentsScreen(
    userRole: UserRole,
    onBack: () -> Unit,
    onIncidentClick: (String) -> Unit,
    onNavSelect: (FlyNavDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminIncidentsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDarkest)
            .statusBarsPadding()
    ) {
        TopBar(
            title = "Incidencias de cartas",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                horizontal = 20.dp,
                vertical = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                IncidentSummary(
                    totalCount = state.totalCount,
                    openCount = state.openCount,
                    inReviewCount = state.inReviewCount
                )
            }

            item {
                IncidentSearchField(
                    value = state.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    onClear = {
                        viewModel.onSearchQueryChange("")
                    }
                )
            }

            item {
                IncidentFilters(
                    selectedFilter = state.selectedFilter,
                    onFilterSelected =
                        viewModel::onFilterSelected
                )
            }

            when {
                state.isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = AccentViolet
                            )
                        }
                    }
                }

                state.errorMessage != null -> {
                    item {
                        IncidentError(
                            message = state.errorMessage.orEmpty(),
                            onRetry = viewModel::retry
                        )
                    }
                }

                state.visibleIncidents.isEmpty() -> {
                    item {
                        EmptyIncidentsMessage(
                            hasSearch =
                                state.searchQuery.isNotBlank(),

                            selectedFilter =
                                state.selectedFilter
                        )
                    }
                }

                else -> {
                    items(
                        items = state.visibleIncidents,
                        key = { item ->
                            item.incident.id
                        }
                    ) { item ->
                        AdminIncidentCard(
                            item = item,
                            onClick = {
                                onIncidentClick(
                                    item.incident.id
                                )
                            }
                        )
                    }

                    if (state.hasMoreIncidents) {
                        item {
                            Button(
                                onClick =
                                    viewModel::loadMoreIncidents,

                                modifier =
                                    Modifier.fillMaxWidth(),

                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = BgSurface,
                                        contentColor = TextPrimary
                                    ),

                                shape =
                                    RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Ver más",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(
                    modifier = Modifier.height(8.dp)
                )
            }
        }

        BottomNav(
            userRole = userRole,
            currentDestination =
                FlyNavDestination.AdminIncidents,
            onDestinationSelected = onNavSelect
        )
    }
}

@Composable
private fun IncidentSummary(
    totalCount: Int,
    openCount: Int,
    inReviewCount: Int
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "RESUMEN",
            color = AccentGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {
            IncidentSummaryCard(
                value = totalCount,
                label = "Total",
                valueColor = TextPrimary,
                modifier = Modifier.weight(1f)
            )

            IncidentSummaryCard(
                value = openCount,
                label = "Abiertas",
                valueColor = AccentGold,
                modifier = Modifier.weight(1f)
            )

            IncidentSummaryCard(
                value = inReviewCount,
                label = "Investigación",
                valueColor = AccentVioletLight,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun IncidentSummaryCard(
    value: Int,
    label: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(BgCard)
            .padding(
                horizontal = 8.dp,
                vertical = 14.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            color = valueColor,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = label,
            color = TextSecondary,
            fontSize = 10.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun IncidentSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = {
            Text(
                text = "Buscar pedido, carta o usuario",
                color = TextMuted,
                fontSize = 13.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextSecondary
            )
        },
        trailingIcon = {
            if (value.isNotBlank()) {
                IconButton(
                    onClick = onClear
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription =
                            "Limpiar búsqueda",
                        tint = TextSecondary
                    )
                }
            }
        },
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentViolet,
            unfocusedBorderColor = BgSurface,
            focusedContainerColor = BgCard,
            unfocusedContainerColor = BgCard,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = AccentViolet
        )
    )
}

@Composable
private fun IncidentFilters(
    selectedFilter: AdminIncidentFilter,
    onFilterSelected: (AdminIncidentFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(
                rememberScrollState()
            ),
        horizontalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {
        AdminIncidentFilter.entries.forEach { filter ->
            IncidentFilterButton(
                label = filter.label,
                selected =
                    selectedFilter == filter,
                onClick = {
                    onFilterSelected(filter)
                }
            )
        }
    }
}

@Composable
private fun IncidentFilterButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(
                if (selected) {
                    AccentViolet
                } else {
                    BgCard
                }
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = 15.dp,
                vertical = 9.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) {
                TextPrimary
            } else {
                TextSecondary
            },
            fontSize = 12.sp,
            fontWeight = if (selected) {
                FontWeight.Bold
            } else {
                FontWeight.Medium
            }
        )
    }
}

@Composable
private fun AdminIncidentCard(
    item: AdminIncidentItem,
    onClick: () -> Unit
) {
    val incident = item.incident

    val orderDisplay =
        incident.orderCode
            .trim()
            .ifBlank {
                incident.orderId
                    .takeLast(8)
                    .uppercase()
            }

    val buyerDisplay =
        item.buyerName
            .trim()
            .ifBlank {
                incident.reporterName
                    .ifBlank {
                        "Comprador no identificado"
                    }
            }

    val sellerDisplay =
        item.sellerName
            .trim()
            .ifBlank {
                incident.counterpartName
                    .ifBlank {
                        "Vendedor no identificado"
                    }
            }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.displayIncidentId,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Pedido $orderDisplay",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement =
                    Arrangement.spacedBy(5.dp)
            ) {
                IncidentStatusBadge(
                    status = item.effectiveStatus
                )

                if (
                    item.effectivePriority ==
                    IncidentPriority.HIGH ||
                    item.effectivePriority ==
                    IncidentPriority.URGENT
                ) {
                    IncidentPriorityBadge(
                        priority =
                            item.effectivePriority
                    )
                }
            }
        }

        Column(
            verticalArrangement =
                Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = incident.type.label,
                color = AccentGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = item.primaryCardName,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(
            verticalArrangement =
                Arrangement.spacedBy(5.dp)
        ) {
            IncidentPartyRow(
                label = "Comprador",
                name = buyerDisplay
            )

            IncidentPartyRow(
                label = "Vendedor",
                name = sellerDisplay
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatIncidentAge(
                    incident.createdAt
                ),
                color = TextMuted,
                fontSize = 11.sp,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "Ver detalle",
                color = AccentVioletLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Icon(
                imageVector =
                    Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AccentVioletLight,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun IncidentPartyRow(
    label: String,
    name: String
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$label:",
            color = TextMuted,
            fontSize = 12.sp
        )

        Text(
            text = " $name",
            color = TextSecondary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun IncidentStatusBadge(
    status: IncidentStatus
) {
    val label =
        when (status) {
            IncidentStatus.OPEN ->
                "ABIERTA"

            IncidentStatus.IN_REVIEW ->
                "EN INVESTIGACIÓN"

            IncidentStatus.URGENT ->
                "ABIERTA"

            IncidentStatus.RESOLVED ->
                "RESUELTA"
        }

    val color =
        when (status) {
            IncidentStatus.OPEN ->
                AccentGold

            IncidentStatus.IN_REVIEW ->
                AccentVioletLight

            IncidentStatus.URGENT ->
                AccentRed

            IncidentStatus.RESOLVED ->
                AccentMint
        }

    IncidentBadge(
        text = label,
        color = color
    )
}

@Composable
private fun IncidentPriorityBadge(
    priority: IncidentPriority
) {
    val color =
        when (priority) {
            IncidentPriority.LOW ->
                TextMuted

            IncidentPriority.MEDIUM ->
                AccentGold

            IncidentPriority.HIGH ->
                AccentRed

            IncidentPriority.URGENT ->
                AccentRed
        }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(
                color.copy(alpha = 0.16f)
            )
            .padding(
                horizontal = 8.dp,
                vertical = 5.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector =
                Icons.Default.WarningAmber,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(13.dp)
        )

        Text(
            text = priority.label.uppercase(),
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun IncidentBadge(
    text: String,
    color: Color
) {
    Text(
        text = text,
        color = color,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(
                color.copy(alpha = 0.16f)
            )
            .padding(
                horizontal = 8.dp,
                vertical = 5.dp
            )
    )
}

@Composable
private fun EmptyIncidentsMessage(
    hasSearch: Boolean,
    selectedFilter: AdminIncidentFilter
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when {
                hasSearch ->
                    "No se encontraron incidencias para esta búsqueda."

                selectedFilter != AdminIncidentFilter.ALL ->
                    "No hay incidencias con este estado."

                else ->
                    "Todavía no existen incidencias."
            },
            color = TextSecondary,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun IncidentError(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = message,
            color = AccentRed,
            fontSize = 13.sp
        )

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentViolet
            )
        ) {
            Text(
                text = "Reintentar",
                color = TextPrimary
            )
        }
    }
}

private fun formatIncidentAge(
    createdAt: Long
): String {
    if (createdAt <= 0L) {
        return "Fecha no disponible"
    }

    val elapsedMillis =
        (getEpochMillis() - createdAt)
            .coerceAtLeast(0L)

    val minutes =
        elapsedMillis / 60_000L

    val hours =
        elapsedMillis / 3_600_000L

    val days =
        elapsedMillis / 86_400_000L

    return when {
        minutes < 1 ->
            "Ahora"

        minutes < 60 ->
            "Hace $minutes min"

        hours < 24 ->
            if (hours == 1L) {
                "Hace 1 hora"
            } else {
                "Hace $hours horas"
            }

        days == 1L ->
            "Hace 1 día"

        else ->
            "Hace $days días"
    }
}