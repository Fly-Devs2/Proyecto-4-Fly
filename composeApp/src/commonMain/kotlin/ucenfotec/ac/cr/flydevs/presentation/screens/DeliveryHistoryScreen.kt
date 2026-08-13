package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.domain.model.BatchStatus
import ucenfotec.ac.cr.flydevs.domain.model.DeliveryBatch
import ucenfotec.ac.cr.flydevs.presentation.DeliveryHistoryFilter
import ucenfotec.ac.cr.flydevs.presentation.DeliveryHistoryViewModel
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentMint
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentRed
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentViolet
import ucenfotec.ac.cr.flydevs.presentation.theme.BgCard
import ucenfotec.ac.cr.flydevs.presentation.theme.BgDarkest
import ucenfotec.ac.cr.flydevs.presentation.theme.TextMuted
import ucenfotec.ac.cr.flydevs.presentation.theme.TextPrimary
import ucenfotec.ac.cr.flydevs.presentation.theme.TextSecondary
import androidx.compose.material3.Scaffold
import ucenfotec.ac.cr.flydevs.domain.model.UserRole
import ucenfotec.ac.cr.flydevs.presentation.components.BottomNav
import ucenfotec.ac.cr.flydevs.presentation.components.FlyNavDestination
import ucenfotec.ac.cr.flydevs.presentation.components.TopBar


@Composable
fun DeliveryHistoryScreen(
    userRole: UserRole,
    onBack: () -> Unit = {},
    onNavSelect: (FlyNavDestination) -> Unit = {},
    viewModel: DeliveryHistoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = BgDarkest,

        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgDarkest)
                    .statusBarsPadding()
            ) {
                TopBar(
                    title = "Historial",
                    onBack = onBack
                )
            }
        },

        bottomBar = {
            BottomNav(
                userRole = userRole,
                currentDestination = FlyNavDestination.DeliveryHistory,
                onDestinationSelected = onNavSelect
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDarkest)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Consulta tus entregas anteriores",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                HistoryFilterChip(
                    text = "Todas",
                    selected = uiState.selectedFilter ==
                            DeliveryHistoryFilter.ALL,
                    onClick = {
                        viewModel.selectFilter(
                            DeliveryHistoryFilter.ALL
                        )
                    }
                )

                HistoryFilterChip(
                    text = "Entregadas",
                    selected = uiState.selectedFilter ==
                            DeliveryHistoryFilter.DELIVERED,
                    onClick = {
                        viewModel.selectFilter(
                            DeliveryHistoryFilter.DELIVERED
                        )
                    }
                )

                HistoryFilterChip(
                    text = "Canceladas",
                    selected = uiState.selectedFilter ==
                            DeliveryHistoryFilter.CANCELLED,
                    onClick = {
                        viewModel.selectFilter(
                            DeliveryHistoryFilter.CANCELLED
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            when {

                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = AccentViolet
                        )
                    }
                }

                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.errorMessage
                                ?: "Error al cargar historial",
                            color = AccentRed
                        )
                    }
                }

                uiState.filteredBatches.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "No hay entregas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text = "No hay registros en esta categoría.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {

                        items(
                            items = uiState.filteredBatches,
                            key = { it.id }
                        ) { batch ->

                            DeliveryHistoryCard(
                                batch = batch
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                fontWeight = if (selected) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                }
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = BgCard,
            labelColor = TextSecondary,

            selectedContainerColor = AccentViolet,
            selectedLabelColor = Color.White
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = TextMuted.copy(alpha = 0.35f),
            selectedBorderColor = AccentViolet
        )
    )
}

@Composable
private fun DeliveryHistoryCard(
    batch: DeliveryBatch
) {

    val statusColor = when (batch.status) {
        BatchStatus.DELIVERED -> AccentMint
        BatchStatus.CANCELLED -> AccentRed
        else -> TextMuted
    }

    val statusText = when (batch.status) {
        BatchStatus.DELIVERED -> "Entregado"
        BatchStatus.CANCELLED -> "Cancelado"
        else -> batch.status.name
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = BgCard
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text = "Lote #${batch.batchId}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = statusColor
            )
        }
    }
}