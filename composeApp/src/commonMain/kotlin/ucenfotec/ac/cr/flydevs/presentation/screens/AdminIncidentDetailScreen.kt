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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import org.koin.core.parameter.parametersOf
import ucenfotec.ac.cr.flydevs.domain.model.IncidentPriority
import ucenfotec.ac.cr.flydevs.domain.model.IncidentStatus
import ucenfotec.ac.cr.flydevs.domain.model.IncidentUpdate
import ucenfotec.ac.cr.flydevs.getEpochMillis
import ucenfotec.ac.cr.flydevs.presentation.AdminIncidentDetail.AdminIncidentDetailUiState
import ucenfotec.ac.cr.flydevs.presentation.AdminIncidentDetail.AdminIncidentDetailViewModel
import ucenfotec.ac.cr.flydevs.presentation.components.ImageCarousel
import ucenfotec.ac.cr.flydevs.presentation.components.TopBar
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

@Composable
fun AdminIncidentDetailScreen(
    incidentId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminIncidentDetailViewModel = koinViewModel(
        parameters = {
            parametersOf(incidentId)
        }
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .background(BgDarkest)
            .statusBarsPadding()
    ) {
        TopBar(
            title = "Detalle de incidencia",
            onBack = onBack
        )

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = AccentViolet
                    )
                }
            }

            state.incident == null -> {
                IncidentDetailError(
                    message = state.errorMessage
                        ?: "No se encontró la incidencia.",
                    onRetry = viewModel::retry
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        horizontal = 20.dp,
                        vertical = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        IncidentHeaderCard(state)
                    }

                    item {
                        StatusSection(
                            selectedStatus = state.effectiveStatus,
                            enabled = !state.isResolved &&
                                    !state.isUpdatingStatus,
                            onStatusSelected = viewModel::updateStatus
                        )
                    }

                    item {
                        PrioritySection(
                            selectedPriority = state.effectivePriority,
                            enabled = !state.isResolved &&
                                    !state.isUpdatingPriority,
                            onPrioritySelected = viewModel::updatePriority
                        )
                    }

                    item {
                        OrderInformationSection(state)
                    }

                    item {
                        PeopleSection(state)
                    }

                    item {
                        IncidentDescriptionSection(
                            type = state.incident?.type?.label.orEmpty(),
                            description = state.incident
                                ?.description
                                .orEmpty()
                        )
                    }

                    if (
                        state.incident
                            ?.evidenceUrls
                            .orEmpty()
                            .isNotEmpty()
                    ) {
                        item {
                            EvidenceSection(
                                imageUrls = state.incident
                                    ?.evidenceUrls
                                    .orEmpty()
                            )
                        }
                    }

                    item {
                        AddFollowUpSection(
                            noteText = state.noteText,
                            isInternal = state.isInternalNote,
                            isSaving = state.isAddingNote,
                            onNoteTextChange =
                                viewModel::onNoteTextChange,
                            onInternalChange =
                                viewModel::onInternalNoteChange,
                            onAddNote = viewModel::addNote
                        )
                    }

                    item {
                        Text(
                            text = "SEGUIMIENTO",
                            color = AccentGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    if (state.updates.isEmpty()) {
                        item {
                            EmptyUpdatesCard()
                        }
                    } else {
                        items(
                            items = state.updates,
                            key = { update ->
                                update.id
                            }
                        ) { update ->
                            IncidentUpdateCard(
                                update = update
                            )
                        }
                    }

                    state.successMessage?.let { message ->
                        item {
                            FeedbackCard(
                                message = message,
                                isError = false
                            )
                        }
                    }

                    state.errorMessage?.let { message ->
                        item {
                            FeedbackCard(
                                message = message,
                                isError = true
                            )
                        }
                    }

                    if (!state.isResolved) {
                        item {
                            ResolveIncidentButton(
                                isLoading = state.isResolving,
                                onClick =
                                    viewModel::resolveIncident
                            )
                        }
                    } else {
                        item {
                            ResolvedIncidentCard()
                        }
                    }

                    item {
                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IncidentHeaderCard(
    state: AdminIncidentDetailUiState
) {
    val incident = state.incident ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(BgCard)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = state.displayIncidentId,
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Pedido ${state.displayOrderCode}",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            IncidentDetailStatusBadge(
                status = state.effectiveStatus
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PriorityHigh,
                contentDescription = null,
                tint = priorityColor(
                    state.effectivePriority
                ),
                modifier = Modifier.size(18.dp)
            )

            Text(
                text = "Prioridad: ${state.effectivePriority.label}",
                color = priorityColor(
                    state.effectivePriority
                ),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = formatIncidentDate(
                incident.createdAt
            ),
            color = TextMuted,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun StatusSection(
    selectedStatus: IncidentStatus,
    enabled: Boolean,
    onStatusSelected: (IncidentStatus) -> Unit
) {
    IncidentDetailSection(
        title = "ESTADO DE LA INCIDENCIA"
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(
                    rememberScrollState()
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                IncidentStatus.OPEN,
                IncidentStatus.IN_REVIEW
            ).forEach { status ->
                SelectableIncidentChip(
                    text = status.label,
                    selected = selectedStatus == status,
                    enabled = enabled,
                    selectedColor = statusColor(status),
                    onClick = {
                        onStatusSelected(status)
                    }
                )
            }

            if (selectedStatus == IncidentStatus.RESOLVED) {
                SelectableIncidentChip(
                    text = IncidentStatus.RESOLVED.label,
                    selected = true,
                    enabled = false,
                    selectedColor = AccentMint,
                    onClick = {}
                )
            }
        }
    }
}

@Composable
private fun PrioritySection(
    selectedPriority: IncidentPriority,
    enabled: Boolean,
    onPrioritySelected: (IncidentPriority) -> Unit
) {
    IncidentDetailSection(
        title = "PRIORIDAD"
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(
                    rememberScrollState()
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IncidentPriority.entries.forEach { priority ->
                SelectableIncidentChip(
                    text = priority.label,
                    selected =
                        selectedPriority == priority,
                    enabled = enabled,
                    selectedColor =
                        priorityColor(priority),
                    onClick = {
                        onPrioritySelected(priority)
                    }
                )
            }
        }
    }
}

@Composable
private fun SelectableIncidentChip(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    val background =
        when {
            !enabled ->
                BgSurface.copy(alpha = 0.55f)

            selected ->
                selectedColor.copy(alpha = 0.22f)

            else ->
                BgSurface
        }

    val textColor =
        when {
            !enabled ->
                TextMuted

            selected ->
                selectedColor

            else ->
                TextSecondary
        }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(background)
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
            .padding(
                horizontal = 15.dp,
                vertical = 10.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
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
private fun OrderInformationSection(
    state: AdminIncidentDetailUiState
) {
    IncidentDetailSection(
        title = "PEDIDO Y CARTAS"
    ) {
        DetailInformationRow(
            icon = Icons.Default.ShoppingBag,
            label = "Pedido",
            value = state.displayOrderCode
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        if (state.cardNames.isEmpty()) {
            Text(
                text = "No se pudieron identificar las cartas.",
                color = TextMuted,
                fontSize = 13.sp
            )
        } else {
            state.cardNames.forEach { cardName ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(AccentViolet)
                    )

                    Text(
                        text = cardName,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun PeopleSection(
    state: AdminIncidentDetailUiState
) {
    IncidentDetailSection(
        title = "PARTES INVOLUCRADAS"
    ) {
        DetailInformationRow(
            icon = Icons.Default.Person,
            label = "Comprador",
            value = state.buyerName
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        DetailInformationRow(
            icon = Icons.Default.Person,
            label = "Vendedor",
            value = state.sellerName
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        DetailInformationRow(
            icon = Icons.Default.WarningAmber,
            label = "Reportado por",
            value = state.incident
                ?.reporterName
                .orEmpty()
                .ifBlank {
                    "Usuario no identificado"
                }
        )
    }
}

@Composable
private fun IncidentDescriptionSection(
    type: String,
    description: String
) {
    IncidentDetailSection(
        title = "DESCRIPCIÓN"
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = AccentGold,
                modifier = Modifier.size(19.dp)
            )

            Text(
                text = type.ifBlank {
                    "Otro"
                },
                color = AccentGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text = description.ifBlank {
                "No se agregó una descripción."
            },
            color = TextSecondary,
            fontSize = 14.sp,
            lineHeight = 21.sp
        )
    }
}

@Composable
private fun EvidenceSection(
    imageUrls: List<String>
) {
    IncidentDetailSection(
        title = "EVIDENCIAS"
    ) {
        ImageCarousel(
            imageUrls = imageUrls,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            onImageIndexChange = {}
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = if (imageUrls.size == 1) {
                "1 evidencia adjunta"
            } else {
                "${imageUrls.size} evidencias adjuntas"
            },
            color = TextMuted,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun AddFollowUpSection(
    noteText: String,
    isInternal: Boolean,
    isSaving: Boolean,
    onNoteTextChange: (String) -> Unit,
    onInternalChange: (Boolean) -> Unit,
    onAddNote: () -> Unit
) {
    IncidentDetailSection(
        title = "AGREGAR SEGUIMIENTO"
    ) {
        OutlinedTextField(
            value = noteText,
            onValueChange = onNoteTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            placeholder = {
                Text(
                    text = "Escribe una nota sobre el seguimiento...",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentViolet,
                unfocusedBorderColor = BgSurface,
                focusedContainerColor = BgSurface,
                unfocusedContainerColor = BgSurface,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = AccentViolet
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Nota interna",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "Solo será visible para administradores.",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }

            Switch(
                checked = isInternal,
                onCheckedChange = onInternalChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AccentViolet,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = BgSurface
                )
            )
        }

        Button(
            onClick = onAddNote,
            enabled = !isSaving &&
                    noteText.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentViolet
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    color = TextPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "Agregar nota",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun IncidentUpdateCard(
    update: IncidentUpdate
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(BgCard)
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = update.authorName.ifBlank {
                        "Administrador"
                    },
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = formatIncidentDate(
                        update.createdAt
                    ),
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }

            if (update.isInternal) {
                Text(
                    text = "INTERNA",
                    color = AccentVioletLight,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(
                            AccentViolet.copy(
                                alpha = 0.18f
                            )
                        )
                        .padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        )
                )
            }
        }

        Text(
            text = update.message,
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun EmptyUpdatesCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(BgCard)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Todavía no hay notas de seguimiento.",
            color = TextSecondary,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun ResolveIncidentButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AccentMint
        ),
        shape = RoundedCornerShape(13.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = BgDarkest,
                strokeWidth = 2.dp,
                modifier = Modifier.size(21.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = BgDarkest,
                modifier = Modifier.size(20.dp)
            )

            Spacer(
                modifier = Modifier.size(8.dp)
            )

            Text(
                text = "Resolver incidencia",
                color = BgDarkest,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ResolvedIncidentCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(
                AccentMint.copy(alpha = 0.16f)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = AccentMint
        )

        Text(
            text = "Esta incidencia está resuelta.",
            color = AccentMint,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FeedbackCard(
    message: String,
    isError: Boolean
) {
    val color =
        if (isError) AccentRed else AccentMint

    Text(
        text = message,
        color = color,
        fontSize = 13.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                color.copy(alpha = 0.13f)
            )
            .padding(14.dp)
    )
}

@Composable
private fun DetailInformationRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(BgSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentVioletLight,
                modifier = Modifier.size(19.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                color = TextMuted,
                fontSize = 10.sp
            )

            Text(
                text = value.ifBlank {
                    "No disponible"
                },
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun IncidentDetailSection(
    title: String,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(17.dp))
            .background(BgCard)
            .padding(17.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            color = AccentGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.9.sp
        )

        content()
    }
}

@Composable
private fun IncidentDetailStatusBadge(
    status: IncidentStatus
) {
    val text =
        when (status) {
            IncidentStatus.OPEN ->
                "ABIERTA"

            IncidentStatus.IN_REVIEW ->
                "EN INVESTIGACIÓN"

            IncidentStatus.RESOLVED ->
                "RESUELTA"

            IncidentStatus.URGENT ->
                "ABIERTA"
        }

    val color =
        statusColor(status)

    Text(
        text = text,
        color = color,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(
                color.copy(alpha = 0.15f)
            )
            .padding(
                horizontal = 9.dp,
                vertical = 6.dp
            )
    )
}

@Composable
private fun IncidentDetailError(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
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

private fun statusColor(
    status: IncidentStatus
): Color {
    return when (status) {
        IncidentStatus.OPEN ->
            AccentGold

        IncidentStatus.IN_REVIEW ->
            AccentVioletLight

        IncidentStatus.RESOLVED ->
            AccentMint

        IncidentStatus.URGENT ->
            AccentRed
    }
}

private fun priorityColor(
    priority: IncidentPriority
): Color {
    return when (priority) {
        IncidentPriority.LOW ->
            TextMuted

        IncidentPriority.MEDIUM ->
            AccentGold

        IncidentPriority.HIGH ->
            AccentRed

        IncidentPriority.URGENT ->
            AccentRed
    }
}

private fun formatIncidentDate(
    timestamp: Long
): String {
    if (timestamp <= 0L) {
        return "Fecha no disponible"
    }

    val elapsed =
        (getEpochMillis() - timestamp)
            .coerceAtLeast(0L)

    val minutes =
        elapsed / 60_000L

    val hours =
        elapsed / 3_600_000L

    val days =
        elapsed / 86_400_000L

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