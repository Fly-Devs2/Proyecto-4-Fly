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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.domain.model.DeliveryBatch
import ucenfotec.ac.cr.flydevs.domain.model.UserRole
import ucenfotec.ac.cr.flydevs.presentation.components.*
import ucenfotec.ac.cr.flydevs.presentation.storeBatches.StoreBatchesViewModel
import ucenfotec.ac.cr.flydevs.presentation.theme.*

@Composable
fun StoreBatchesScreen(
    userRole: UserRole,
    onBack: () -> Unit,
    onBatchClick: (String) -> Unit,
    onNavSelect: (FlyNavDestination) -> Unit,
    viewModel: StoreBatchesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BgDarkest,
        topBar = {
            Column {
                Spacer(Modifier.statusBarsPadding())
                TopBar(title = "Lotes", onBack = onBack)
            }
        },
        bottomBar = {
            BottomNav(
                userRole = userRole,
                currentDestination = FlyNavDestination.StoreBatchesScreen,
                onDestinationSelected = onNavSelect
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AccentViolet
                )
            } else if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = AccentRed,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "${uiState.totalBatches} lotes por despachar · ${uiState.totalCards} cartas",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }

                    item {
                        StoreQrCard(
                            storeName = uiState.storeName,
                            qrImageUrl = uiState.qrImageUrl,
                            batchCount = uiState.totalBatches,
                            destinationCount = uiState.totalDestinations
                        )
                    }

                    item {
                        Text(
                            text = "LOTES SALIENTES",
                            style = Typography.titleSmall,
                            color = AccentGold
                        )
                    }

                    if (uiState.batches.isEmpty()) {
                        item {
                            Text(
                                text = "No hay lotes listos para recogida.",
                                color = TextMuted,
                                fontSize = 14.sp,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        items(uiState.batches) { batch ->
                            StoreBatchRow(
                                batch = batch,
                                onClick = { onBatchClick(batch.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreQrCard(
    storeName: String,
    qrImageUrl: String,
    batchCount: Int,
    destinationCount: Int
) {
    Surface(
        color = BgCard,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MI QR DE TIENDA · $storeName".uppercase(),
                color = AccentGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(16.dp))
            
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(200.dp).padding(8.dp)
            ) {
                AsyncImage(
                    model = qrImageUrl,
                    contentDescription = "Store QR Code",
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = "El mensajero escanea este código para aceptar los\n$batchCount lotes salientes.",
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
            
            Spacer(Modifier.height(16.dp))
            
            Surface(
                color = AccentMint.copy(alpha = 0.16f),
                shape = RoundedCornerShape(50)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        FlyIconType.Home, // Placeholder for a check or similar if available
                        Modifier.size(14.dp),
                        AccentMint
                    )
                    Spacer(Modifier.width(6.dp))
                    val destinationText = if (destinationCount == 1) "1 tienda" else "$destinationCount tiendas"
                    Text(
                        text = "Destinos: $destinationText",
                        color = AccentMint,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StoreBatchRow(
    batch: DeliveryBatch,
    onClick: () -> Unit
) {
    Surface(
        color = BgCard,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BgSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    FlyIconType.Package,
                    Modifier.size(20.dp),
                    AccentGold
                )
            }
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Lote #${batchCodeInternal(batch.batchId.ifBlank { batch.id })}",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        FlyIconType.Home,
                        Modifier.size(12.dp),
                        AccentVioletLight
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Destino: ${batch.destinationStoreName}",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                BatchStatusBadge(batch.status)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${batch.orderIds.size} cartas",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun batchCodeInternal(id: String): String = if (id.length > 7) id.take(7).uppercase() else id.uppercase()
