package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.domain.model.CardEnvelope
import ucenfotec.ac.cr.flydevs.domain.model.GameCard
import ucenfotec.ac.cr.flydevs.domain.model.UserRole
import ucenfotec.ac.cr.flydevs.presentation.Envelopes.CardEnvelopesViewModel
import ucenfotec.ac.cr.flydevs.presentation.components.BottomNav
import ucenfotec.ac.cr.flydevs.presentation.components.FlyNavDestination
import ucenfotec.ac.cr.flydevs.presentation.components.FormField
import ucenfotec.ac.cr.flydevs.presentation.components.SearchableDropdown
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
fun MyEnvelopesScreen(
    userRole: UserRole,
    userId: String,
    onBack: () -> Unit = {},
    onEnvelopeClick: (String) -> Unit = {},
    onAddMoreCards: () -> Unit = {},
    onNavSelect: (FlyNavDestination) -> Unit = {},
    viewModel: CardEnvelopesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var envelopeToDelete by remember {
        mutableStateOf<CardEnvelope?>(null)
    }
    var showBulkWarning by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.loadPendingEnvelopes(userId)
    }
    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        val message = uiState.errorMessage ?: uiState.successMessage

        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        containerColor = BgDarkest,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            BottomNav(
                userRole = userRole,
                currentDestination = FlyNavDestination.Orders,
                onDestinationSelected = onNavSelect
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDarkest)
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            TopBar(
                title = "Mis sobres",
                onBack = onBack
            )

            when {
                uiState.isLoading -> {
                    LoadingContent()
                }

                uiState.errorMessage != null -> {
                    ErrorContent(
                        message = uiState.errorMessage ?: "Ocurrió un error."
                    )
                }

                uiState.isEmpty -> {
                    EmptyEnvelopesContent(
                        onAddMoreCards = onAddMoreCards
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            EnvelopesSummaryCard(
                                envelopeCount = uiState.pendingEnvelopeCount,
                                totalCards = uiState.totalCards,
                                totalAmount = uiState.totalAmount
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            FormField("Tienda de destino para todos los sobres") {
                                SearchableDropdown(
                                    selected = uiState.selectedGlobalStore,
                                    options = uiState.stores,
                                    label = { it.name },
                                    onSelect = { viewModel.onGlobalStoreChange(userId, it) },
                                    placeholder = "Seleccionar para todos"
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            
                            ReserveAllButton(
                                isLoading = uiState.isGeneratingOrders,
                                enabled = uiState.envelopes.isNotEmpty(),
                                onClick = {
                                    showBulkWarning = true
                                }
                            )
                        }

                        items(
                            items = uiState.envelopes,
                            key = { envelope -> envelope.id }
                        ) { envelope ->

                            val sellerName = uiState
                                .sellerNames[envelope.sellerId]
                                ?.takeIf { name -> name.isNotBlank() }
                                ?: "Vendedor desconocido"
                            
                            val sourceStoreName = uiState.sourceStoreNames[envelope.sourceStore] ?: "Tienda desconocida"

                            EnvelopeListItem(
                                envelope = envelope,
                                sellerName = sellerName,
                                sourceStoreName = sourceStoreName,
                                isDeleting = uiState.deletingEnvelopeId == envelope.id,
                                onClick = {
                                    onEnvelopeClick(envelope.id)
                                },
                                onDeleteClick = {
                                    envelopeToDelete = envelope
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }

    if (showBulkWarning) {
        AlertDialog(
            onDismissRequest = { showBulkWarning = false },
            title = {
                Text(
                    "Confirmar generación masiva",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Se generarán órdenes para todos los sobres pendientes.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (uiState.selectedGlobalStore != null) {
                        Text(
                            "IMPORTANTE: Todos los sobres serán entregados en la tienda: ${uiState.selectedGlobalStore?.name}",
                            color = AccentGold,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBulkWarning = false
                        viewModel.generateOrdersForAllEnvelopes(userId)
                    }
                ) {
                    Text("Confirmar", color = AccentViolet, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkWarning = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = BgCard
        )
    }

    envelopeToDelete?.let { envelope ->
        AlertDialog(
            onDismissRequest = {
                envelopeToDelete = null
            },
            title = {
                Text(
                    text = "Eliminar sobre",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = "¿Seguro que deseas eliminar este sobre? Las cartas volverán a estar disponibles.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEnvelope(
                            envelopeId = envelope.id,
                            userId = userId
                        )
                        envelopeToDelete = null
                    }
                ) {
                    Text(
                        text = "Eliminar",
                        color = AccentRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        envelopeToDelete = null
                    }
                ) {
                    Text(
                        text = "Cancelar",
                        color = TextSecondary
                    )
                }
            },
            containerColor = BgCard
        )
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = AccentViolet
        )
    }
}

@Composable
private fun ErrorContent(
    message: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun EmptyEnvelopesContent(
    onAddMoreCards: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No tienes sobres pendientes",
            color = TextPrimary,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Agrega cartas desde el catálogo para crear sobres separados por vendedor.",
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onAddMoreCards,
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentViolet,
                contentColor = TextPrimary
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Explorar cartas",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun EnvelopesSummaryCard(
    envelopeCount: Int,
    totalCards: Int,
    totalAmount: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = BgSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "RESUMEN",
                color = AccentGold,
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryValue(
                    label = "Sobres",
                    value = envelopeCount.toString()
                )

                SummaryValue(
                    label = "Cartas",
                    value = totalCards.toString()
                )

                SummaryValue(
                    label = "Total",
                    value = formatCRC(totalAmount),
                    valueColor = AccentGold
                )
            }
        }
    }
}

@Composable
private fun SummaryValue(
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    Column {
        Text(
            text = label,
            color = TextSecondary,
            style = MaterialTheme.typography.labelSmall
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            color = valueColor,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EnvelopeListItem(
    envelope: CardEnvelope,
    isDeleting: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    sellerName: String,
    sourceStoreName: String
) {
    val cardCount = if (envelope.cards.isNotEmpty()) {
        envelope.cards.size
    } else {
        envelope.cardIds.size
    }

    val shippingCost = getEnvelopeShippingCost(envelope)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = BgCard
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SellerAvatar(
                    sellerName = sellerName
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = "Sobre de vendedor",
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )

                    Text(
                        text = sellerName,
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Tienda de origen: $sourceStoreName",
                        color = AccentGold,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                StatusBadge(
                    text = "PENDIENTE"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            EnvelopeCardPreview(
                cards = envelope.cards,
                cardCount = cardCount
            )

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(
                color = BgSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            EnvelopeAmountRow(
                label = "Subtotal",
                value = formatCRC(envelope.subTotal)
            )

            Spacer(modifier = Modifier.height(6.dp))

            EnvelopeAmountRow(
                label = "Envío",
                value = if (shippingCost == 0L) "Gratis" else formatCRC(shippingCost),
                valueColor = if (shippingCost == 0L) AccentMint else TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            EnvelopeAmountRow(
                label = "Total",
                value = formatCRC(envelope.total),
                valueColor = AccentGold,
                isStrong = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentViolet,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Ver sobre",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onDeleteClick,
                    enabled = !isDeleting,
                    modifier = Modifier
                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentRed,
                        contentColor = TextPrimary,
                        disabledContainerColor = AccentRed.copy(alpha = 0.45f),
                        disabledContentColor = TextPrimary.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            color = TextPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "Eliminar sobre",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SellerAvatar(
    sellerName: String
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(BgSurface),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = sellerName.firstOrNull()?.uppercaseChar()?.toString() ?: "V",
            color = AccentVioletLight,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatusBadge(
    text: String
) {
    Text(
        text = text,
        color = AccentMint,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(BgSurface)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

@Composable
private fun EnvelopeCardPreview(
    cards: List<GameCard>,
    cardCount: Int
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            cards.take(3).forEach { card ->
                AsyncImage(
                    model = card.imageUrl,
                    contentDescription = card.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BgSurface)
                )
            }

            if (cardCount > 3) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BgSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+${cardCount - 3}",
                        color = TextPrimary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$cardCount ${if (cardCount == 1) "carta" else "cartas"} en este sobre",
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )

        if (cards.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = cards.take(2).joinToString(", ") { card -> card.name },
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun EnvelopeAmountRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary,
    isStrong: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextSecondary,
            style = if (isStrong) {
                MaterialTheme.typography.bodySmall
            } else {
                MaterialTheme.typography.labelSmall
            },
            fontWeight = if (isStrong) FontWeight.Bold else FontWeight.Normal
        )

        Text(
            text = value,
            color = valueColor,
            style = if (isStrong) {
                MaterialTheme.typography.bodyMedium
            } else {
                MaterialTheme.typography.labelSmall
            },
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ReserveAllButton(
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AccentViolet,
            disabledContainerColor = AccentViolet.copy(alpha = 0.5f),
            contentColor = TextPrimary,
            disabledContentColor = TextPrimary.copy(alpha = 0.5f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = TextPrimary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Text(
                text = "Generar orden para todos los sobres",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun getEnvelopeShippingCost(
    envelope: CardEnvelope
): Long {
    return (envelope.total - envelope.subTotal).coerceAtLeast(0L)
}

private fun formatCRC(
    amount: Long
): String {
    val number = amount
        .toString()
        .reversed()
        .chunked(3)
        .joinToString(" ")
        .reversed()

    return "₡$number"
}