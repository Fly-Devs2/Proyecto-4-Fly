package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ucenfotec.ac.cr.flydevs.domain.model.GameCard
import ucenfotec.ac.cr.flydevs.domain.model.ShippingMethod
import ucenfotec.ac.cr.flydevs.presentation.components.BottomNav
import ucenfotec.ac.cr.flydevs.presentation.components.FlyNavDestination
import ucenfotec.ac.cr.flydevs.presentation.envelope.CardEnvelopeViewModel
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentVioletLight
import ucenfotec.ac.cr.flydevs.presentation.theme.BgCard
import androidx.compose.material3.HorizontalDivider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.domain.model.UserRole

private const val DELIVERY_SHIPPING_COST = 600L
@Composable
fun MyEnvelopeScreen(
    userRole: UserRole,
    userId: String,
    envelopeId: String,
    viewModel: CardEnvelopeViewModel = koinViewModel(),
    onBack: () -> Unit,
    onAddMoreCards: () -> Unit,
    onOrderGenerated: () -> Unit = {},
    onNavSelect: (FlyNavDestination) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }


    var selectedShippingMethod by remember {
        mutableStateOf(ShippingMethod.DELIVERY)
    }

    LaunchedEffect(envelopeId) {
        viewModel.loadEnvelopeById(envelopeId)
    }
    LaunchedEffect(uiState.envelope?.shippingMethod) {
        uiState.envelope?.shippingMethod?.let { method ->
            selectedShippingMethod = method
        }
    }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        val message = uiState.errorMessage ?: uiState.successMessage

        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()

            if (message == "Orden generada correctamente.") {
                onOrderGenerated()
            }
        }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            MyEnvelopeTopBar(
                cardCount = uiState.cardCount,
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                MyEnvelopeInfoBanner()

                Spacer(modifier = Modifier.height(12.dp))

                if (uiState.isLoading && uiState.cards.isEmpty()) {
                    MyEnvelopeLoadingContent()
                } else {
                    MyEnvelopeCardsContainer(
                        cards = uiState.cards,
                        onRemoveCard = { cardId ->
                            viewModel.removeCardFromEnvelope(
                                envelopeId = envelopeId,
                                cardId = cardId
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    MyEnvelopeAddMoreCardsButton(
                        onClick = onAddMoreCards
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    MyEnvelopeDeliveryMethodSection(
                        selectedShippingMethod = selectedShippingMethod,
                        onSelect = { method ->
                            selectedShippingMethod = method
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val shippingCost = getShippingCost(selectedShippingMethod)
                    val total = uiState.subTotal + shippingCost

                    MyEnvelopeSummarySection(
                        cardCount = uiState.cardCount,
                        subTotal = uiState.subTotal,
                        shippingCost = shippingCost,
                        total = total
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    MyEnvelopeReserveButton(
                        enabled = uiState.canGenerateOrder,
                        isLoading = uiState.isGeneratingOrder,
                        onClick = {
                            viewModel.generateOrderFromEnvelope(envelopeId)
                        }
                    )
                }
            }
        }
    }
}





@Composable
private fun MyEnvelopeTopBar(
    cardCount: Int,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Text(
            text = "Mi sobre",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f)
        )

        Card(
            shape = RoundedCornerShape(50),
            colors = CardDefaults.cardColors(
                containerColor = BgCard
            )
        ) {
            Text(
                text = "$cardCount CARTAS",
                color = AccentVioletLight,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(
                    horizontal = 14.dp,
                    vertical = 6.dp
                )
            )
        }
    }
}

@Composable
private fun MyEnvelopeInfoBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = BgCard
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MailOutline,
                contentDescription = null,
                tint = AccentVioletLight,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Tus cartas se agrupan en un sobre con código QR único para el intercambio.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun MyEnvelopeLoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun MyEnvelopeCardsContainer(
    cards: List<GameCard>,
    onRemoveCard: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = BgCard
        )
    ) {
        if (cards.isEmpty()) {
            MyEnvelopeEmptyMessage()
        } else {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                cards.forEachIndexed { index, card ->
                    MyEnvelopeCardRow(
                        card = card,
                        onRemoveCard = onRemoveCard
                    )

                    if (index < cards.lastIndex) {
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }
    }
}
@Composable
private fun MyEnvelopeEmptyMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tu sobre está vacío",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Agrega cartas para poder generar una orden.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun MyEnvelopeCardRow(
    card: GameCard,
    onRemoveCard: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = card.imageUrl,
            contentDescription = card.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = card.name,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${getSellerText(card)} · ${formatCondition(card.condition.name)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Text(
            text = formatCRC(card.price),
            color = AccentVioletLight,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(10.dp))

        Surface(
            modifier = Modifier
                .size(28.dp)
                .clickable {
                    onRemoveCard(card.id)
                },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Eliminar carta",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun MyEnvelopeAddMoreCardsButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Agregar más cartas",
            style = MaterialTheme.typography.labelLarge
        )
    }

}

@Composable
private fun MyEnvelopeDeliveryMethodSection(
    selectedShippingMethod: ShippingMethod,
    onSelect: (ShippingMethod) -> Unit
) {
    Column {
        Text(
            text = "MÉTODO DE ENTREGA",
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MyEnvelopeDeliveryOptionCard(
                modifier = Modifier.weight(1f),
                title = "Retiro en tienda",
                subtitle = "Gratis",
                iconContent = {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = AccentVioletLight,
                        modifier = Modifier.size(22.dp)
                    )
                },
                selected = selectedShippingMethod == ShippingMethod.PICKUP,
                subtitleColor = MaterialTheme.colorScheme.tertiary,
                onClick = {
                    onSelect(ShippingMethod.PICKUP)
                }
            )

            MyEnvelopeDeliveryOptionCard(
                modifier = Modifier.weight(1f),
                title = "Mensajero",
                subtitle = formatCRC(DELIVERY_SHIPPING_COST),
                iconContent = {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = AccentVioletLight,
                        modifier = Modifier.size(22.dp)
                    )
                },
                selected = selectedShippingMethod == ShippingMethod.DELIVERY,
                subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = {
                    onSelect(ShippingMethod.DELIVERY)
                }
            )
        }
    }
}

@Composable
private fun MyEnvelopeDeliveryOptionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    iconContent: @Composable () -> Unit,
    selected: Boolean,
    subtitleColor: Color,
    onClick: () -> Unit
) {
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .height(82.dp)
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = borderColor
        ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            iconContent()

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                color = if (selected) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                color = subtitleColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MyEnvelopeSummarySection(
    cardCount: Int,
    subTotal: Long,
    shippingCost: Long,
    total: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = BgCard
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            SummaryRow(
                label = "Subtotal ($cardCount cartas)",
                value = formatCRC(subTotal),
                valueColor = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            SummaryRow(
                label = "Envío",
                value = if (shippingCost == 0L) "Gratis" else formatCRC(shippingCost),
                valueColor = if (shippingCost == 0L) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.onBackground
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = formatCRC(total),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            color = valueColor,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MyEnvelopeReserveButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Text(
                text = "Reservar sobre y continuar",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
private fun Modifier.dashedBorder(
    color: Color,
    radius: Dp
): Modifier {
    return this.drawBehind {
        val strokeWidth = 1.dp.toPx()
        val dashWidth = 10.dp.toPx()
        val dashGap = 6.dp.toPx()

        drawRoundRect(
            color = color,
            style = Stroke(
                width = strokeWidth,
                pathEffect = PathEffect.dashPathEffect(
                    intervals = floatArrayOf(dashWidth, dashGap),
                    phase = 0f
                )
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                radius.toPx(),
                radius.toPx()
            )
        )
    }
}

private fun getShippingCost(
    shippingMethod: ShippingMethod
): Long {
    return when (shippingMethod) {
        ShippingMethod.PICKUP -> 0L
        ShippingMethod.DELIVERY -> DELIVERY_SHIPPING_COST
    }
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

private fun formatCondition(
    condition: String
): String {
    return condition
        .replace("_", " ")
        .lowercase()
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { char ->
                char.uppercase()
            }
        }
}

private fun getSellerText(
    card: GameCard
): String {
    return card.sellerId.ifBlank {
        "Vendedor"
    }
}
