package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.domain.model.GameCard
import ucenfotec.ac.cr.flydevs.domain.model.User
import ucenfotec.ac.cr.flydevs.domain.model.UserRole
import ucenfotec.ac.cr.flydevs.presentation.cardDetail.CardDetailViewModel
import ucenfotec.ac.cr.flydevs.presentation.components.BottomNav
import ucenfotec.ac.cr.flydevs.presentation.components.FlyNavDestination
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
fun CardDetailScreen(
    userRole: UserRole,
    userId: String,
    cardId: String,
    fromCollection: Boolean = false,
    modifier: Modifier = Modifier,
    viewModel: CardDetailViewModel = koinViewModel(),
    onBack: () -> Unit = {},
    onNavSelect: (FlyNavDestination) -> Unit = {},
    onGoToEnvelope: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(cardId) { viewModel.loadCard(cardId, userId, fromCollection) }

    LaunchedEffect(state.idCopied) {
        if (state.idCopied) {
            snackbarHostState.showSnackbar("ID copiado al portapapeles")
            viewModel.clearIdCopied()
        }
    }
    LaunchedEffect(state.actionErrorMessage) {
        state.actionErrorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearEnvelopeNavigation()
        }
    }
    LaunchedEffect(state.targetEnvelopeId) {
        state.targetEnvelopeId?.let { envelopeId ->
            viewModel.clearEnvelopeNavigation()
            onGoToEnvelope(envelopeId)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(BgDarkest)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {

            TopBar(
                title = "Detalle de carta",
                onBack = onBack,

            )

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentViolet)
                }
                return@Column
            }

            state.errorMessage?.let { msg ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(msg, color = AccentRed)
                }
                return@Column
            }

            val card = state.card ?: return@Column

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).navigationBarsPadding()) {

                Box(
                    modifier = Modifier.fillMaxWidth().height(240.dp).background(BgSurface),
                ) {
                    AsyncImage(
                        model = card.imageUrl,
                        contentDescription = card.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    card.rarity?.let { rarity ->
                        Text(
                            text = rarity.uppercase(),
                            color = Color.Black,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(AccentGold)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(card.name, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text("ID: ${card.id}", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                                Text(
                                    "⧉",
                                    color = TextMuted,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.clickable {
                                        clipboardManager.setText(AnnotatedString(card.id))
                                        viewModel.onIdCopied()
                                    }
                                )
                            }
                        }
                        Text(
                            "₡${card.price}",
                            color = AccentVioletLight,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    CardTagPills(card, state.sourceStoreName)

                    //MarketDataSection()

                    SellerSection(seller = state.seller)

                    Button(
                        onClick = {
                            viewModel.addToEnvelope(
                                userId = userId,
                                cardId = card.id
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentViolet
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !fromCollection && !state.addedToEnvelope && !state.isAddingToEnvelope,
                    ) {
                        if (state.isAddingToEnvelope) {
                            CircularProgressIndicator(
                                color = TextPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                if (state.addedToEnvelope) {
                                    "Agregado al sobre"
                                } else {
                                    "Agregar al sobre"
                                },
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }


                }
            }

            BottomNav(currentDestination = FlyNavDestination.Explore, onDestinationSelected = onNavSelect, userRole = userRole)
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CardTagPills(card: GameCard, sourceStoreName: String? = null) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TagPill(
            text = ("Tienda de origen: " + sourceStoreName) ?: "Desconocido",
            backgroundColor = AccentMint.copy(alpha = 0.2f),
            textColor = AccentMint
        )
        card.expansion?.let { TagPill(it) }
        card.rarity?.let { TagPill(it) }
        TagPill(card.condition.label)
        TagPill(card.language.label)
    }
}

@Composable
private fun TagPill(
    text: String,
    backgroundColor: Color = BgSurface,
    textColor: Color = TextSecondary
) {
    Text(
        text = text,
        color = textColor,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 5.dp),
    )
}

//@Composable
//private fun MarketDataSection() {
//    Column(
//        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(BgCard).padding(16.dp),
//        verticalArrangement = Arrangement.spacedBy(10.dp),
//    ) {
//        Text("DATOS DE MERCADO · MOXFIELD API", color = AccentGold, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
//        HorizontalDivider(color = BgSurface)
//        MarketRow("Precio promedio mercado", "₡298 000", TextPrimary)
//        MarketRow("Tendencia 30 días", "+4.2%", AccentMint)
//        MarketRow("Última venta registrada", "₡310 000", TextPrimary)
//    }
//}

@Composable
private fun MarketRow(label: String, value: String, valueColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
        Text(value, color = valueColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SellerSection(seller: User?) {
    val sellerId = seller?.name ?: "Vendedor desconocido"

    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(BgCard).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(BgSurface),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                sellerId.firstOrNull()?.uppercaseChar()?.toString() ?: "V",
                color = AccentVioletLight,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("Vendedor", color = AccentGold, style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(sellerId, color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
           //Text("⭐⭐⭐⭐☆  4.8 · 132 ventas", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
        }
//        Text(
//            "VERIFICADO",
//            color = AccentViolet,
//            style = MaterialTheme.typography.labelSmall,
//            fontWeight = FontWeight.Bold,
//            modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(BgSurface).padding(horizontal = 8.dp, vertical = 4.dp),
//        )
    }
}
