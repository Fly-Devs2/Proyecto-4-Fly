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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.domain.model.CardStatus
import ucenfotec.ac.cr.flydevs.domain.model.GameCard
import ucenfotec.ac.cr.flydevs.presentation.components.BottomNav
import ucenfotec.ac.cr.flydevs.presentation.components.FlyNavDestination
import ucenfotec.ac.cr.flydevs.presentation.components.TopBar
import ucenfotec.ac.cr.flydevs.presentation.myCollection.CollectionFilter
import ucenfotec.ac.cr.flydevs.presentation.myCollection.MyCollectionViewModel
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentGold
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentMint
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentRed
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentViolet
import ucenfotec.ac.cr.flydevs.presentation.theme.BgCard
import ucenfotec.ac.cr.flydevs.presentation.theme.BgDarkest
import ucenfotec.ac.cr.flydevs.presentation.theme.BgSurface
import ucenfotec.ac.cr.flydevs.presentation.theme.TextMuted
import ucenfotec.ac.cr.flydevs.presentation.theme.TextPrimary
import ucenfotec.ac.cr.flydevs.presentation.theme.TextSecondary

@Composable
fun MyCollectionScreen(
    modifier: Modifier = Modifier,
    viewModel: MyCollectionViewModel = koinViewModel(),
    onBack: () -> Unit = {},
    onCardClick: (String) -> Unit = {},
    onNavSelect: (FlyNavDestination) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.pendingDeleteCardId != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            containerColor = BgCard,
            title = { Text("Eliminar carta", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que querés eliminar esta carta? Esta acción no se puede deshacer.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete() }) {
                    Text("Eliminar", color = AccentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
        )
    }

    state.deleteError?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            containerColor = BgCard,
            title = { Text("No se puede eliminar", color = AccentRed, fontWeight = FontWeight.Bold) },
            text = { Text(error, color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text("Entendido", color = AccentViolet)
                }
            },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDarkest)
            .statusBarsPadding(),
    ) {
        TopBar(title = "Mi colección", onBack = onBack)

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

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatChip("${state.totalActivas}", "Activas", TextPrimary, Modifier.weight(1f))
            StatChip("${state.totalVendidas}", "Vendidas", TextPrimary, Modifier.weight(1f))
            StatChip("${state.totalVistas}", "Vistas", AccentGold, Modifier.weight(1f))
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(CollectionFilter.entries) { filter ->
                CollectionFilterChip(
                    label = filter.label,
                    selected = state.activeFilter == filter,
                    onClick = { viewModel.setFilter(filter) },
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(state.filteredCards, key = { it.id }) { card ->
                CollectionCardItem(
                    card = card,
                    onClick = { onCardClick(card.id) },
                    onDelete = { viewModel.requestDelete(card.id) },
                )
            }
        }

        BottomNav(currentDestination = FlyNavDestination.Explore, onDestinationSelected = onNavSelect)
    }
}

@Composable
private fun StatChip(value: String, label: String, valueColor: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(BgCard).padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, color = valueColor, style = MaterialTheme.typography.titleMedium)
        Text(label, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun CollectionFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) AccentViolet else BgCard
    val textColor = if (selected) TextPrimary else TextSecondary
    Text(
        text = label,
        color = textColor,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun CollectionCardItem(card: GameCard, onClick: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BgCard)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)).background(BgSurface),
            contentAlignment = Alignment.Center,
        ) {
            if (card.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = card.imageUrl,
                    contentDescription = card.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                )
            } else {
                Text("🃏", style = MaterialTheme.typography.titleLarge)
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(card.name, color = TextPrimary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                when (card.status) {
                    CardStatus.AVAILABLE -> StatusBadge("ACTIVA", AccentMint)
                    CardStatus.SOLD      -> StatusBadge("VENDIDA", BgSurface)
                    CardStatus.RESERVED  -> StatusBadge("PAUSADA", BgSurface)
                }
            }
            Text(
                "${card.expansion ?: ""} · Cond. ${card.condition.label}",
                color = TextSecondary,
                style = MaterialTheme.typography.labelSmall,
            )
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("₡${card.price}", color = AccentViolet, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                if (card.status != CardStatus.SOLD) {
                    Text(
                        "🗑",
                        modifier = Modifier.clickable(onClick = onDelete),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(text: String, color: Color) {
    Text(
        text = text,
        color = if (color == AccentMint) BgDarkest else TextSecondary,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(color).padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

private val CollectionFilter.label: String get() = when (this) {
    CollectionFilter.TODAS    -> "Todas"
    CollectionFilter.ACTIVAS  -> "Activas"
    CollectionFilter.VENDIDAS -> "Vendidas"
    CollectionFilter.PAUSADAS -> "Pausadas"
}
