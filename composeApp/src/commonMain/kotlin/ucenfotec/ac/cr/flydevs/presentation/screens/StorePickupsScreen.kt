package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.domain.model.UserRole
import ucenfotec.ac.cr.flydevs.navigation.ShipmentDetail
import ucenfotec.ac.cr.flydevs.navigation.StorePickups
import ucenfotec.ac.cr.flydevs.presentation.components.*
import ucenfotec.ac.cr.flydevs.presentation.storeBatches.StoreBatchesViewModel
import ucenfotec.ac.cr.flydevs.presentation.storePickups.StorePickupCardItem
import ucenfotec.ac.cr.flydevs.presentation.theme.*

@Composable
fun StorePickupsScreen(
    userRole: UserRole,
    onBack: () -> Unit,
    onCardClick: (String) -> Unit,
    onNavSelect: (FlyNavDestination) -> Unit,
    viewModel: StoreBatchesViewModel = koinViewModel()
) {
    val state by viewModel.pickupsUiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BgDarkest,
        topBar = {
            Column {
                Spacer(Modifier.statusBarsPadding())
                TopBar(title = "Retiros", onBack = onBack)
            }
        },
        bottomBar = {
            BottomNav(
                userRole = userRole,
                currentDestination = FlyNavDestination.StorePickups,
                onDestinationSelected = onNavSelect
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AccentViolet
                )
                return@Box
            }

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage!!,
                    color = AccentRed,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    textAlign = TextAlign.Center
                )
                return@Box
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "${state.totalCards} cartas para retiro · ${state.availableRarities.size} rarezas",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }

                item {
                    // Filters
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Status dropdown
                        val statuses = remember(state) { state.allCards.map { it.cardStatus }.distinct().sorted() }
                        Dropdown(
                            selected = state.selectedCardStatus,
                            options = listOf("Todos") + statuses,
                            label = { it.toString() },
                            placeholder = "Estado",
                            onSelect = { value ->
                                viewModel.onPickupStatusFilterChange(if (value == "Todos") null else value)
                            }
                        )

                        // User role dropdown
                        val users = remember(state) { state.allCards.map { it.userRole }.distinct().sorted() }
                        Dropdown(
                            selected = state.selectedUserRole,
                            options = listOf("Todos") + users,
                            label = { it.toString() },
                            placeholder = "Usuario",
                            onSelect = { value ->
                                viewModel.onPickupUserFilterChange(if (value == "Todos") null else value)
                            }
                        )

                        // Rarity dropdown
                        Dropdown(
                            selected = state.selectedRarity,
                            options = listOf("Todos") + state.availableRarities,
                            label = { it.toString() },
                            placeholder = "Rareza",
                            onSelect = { value ->
                                viewModel.onPickupRarityFilterChange(if (value == "Todos") null else value)
                            }
                        )

                        // Date text field (simple exact-match filter)
                        OutlinedTextField(
                                                    value = state.arrivalDateFilter,
                                                    onValueChange = { viewModel.onPickupDateFilterChange(it) },
                                                    label = { Text("Fecha (YYYY-MM-DD)") },
                                                    modifier = Modifier.fillMaxWidth()
                                                )

                        // Clear filters
                        TextButton(onClick = { viewModel.clearPickupFilters() }) {
                            Text(text = "Limpiar filtros", color = AccentViolet)
                        }
                    }
                }

                // Cards list
                if (state.visibleCards.isEmpty()) {
                    item {
                        Text(
                            text = "No hay cartas disponibles para retiro.",
                            color = TextMuted,
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(state.visibleCards) { card ->
                        StorePickupCard(card = card, onClick = { onCardClick(card.batchId) })
                    }
                }

                // Pagination
                item {
                    PaginationBar(
                        page = state.page,
                        totalPages = state.totalPages,
                        onPrevious = { viewModel.onPickupPageChange(state.page - 1) },
                        onNext = { viewModel.onPickupPageChange(state.page + 1) },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StorePickupCard(card: StorePickupCardItem, onClick: () -> Unit) {
    Surface(
        color = BgCard,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = BgSurface,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(64.dp)
            ) {
                if (card.imageUrl.isNotBlank()) {
                    AsyncImage(model = card.imageUrl, contentDescription = card.name, modifier = Modifier.fillMaxSize())
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = card.name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(text = "Rarity: ${card.rarity} · Estado: ${card.cardStatus}", color = TextMuted, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Text(text = card.participantLabel, color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Text(text = "Llegada: ${card.arrivalDateLabel}", color = TextSecondary, fontSize = 12.sp)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = card.displayRole, color = AccentGold, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(text = card.destinationStoreName, color = TextMuted, fontSize = 11.sp)
            }
        }
    }
}
