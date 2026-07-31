package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.domain.model.UserRole
import ucenfotec.ac.cr.flydevs.presentation.components.BottomNav
import ucenfotec.ac.cr.flydevs.presentation.components.FlyNavDestination
import ucenfotec.ac.cr.flydevs.presentation.components.TopBar
import ucenfotec.ac.cr.flydevs.presentation.storeHome.StoreHomeViewModel
import ucenfotec.ac.cr.flydevs.presentation.theme.*

/** Listado completo de las órdenes que esperan retiro en la tienda. */
@Composable
fun StorePickupsScreen(
    userRole: UserRole,
    onBack: () -> Unit = {},
    onPickupClick: (String) -> Unit = {},
    onScanPickup: () -> Unit = {},
    onNavSelect: (FlyNavDestination) -> Unit = {},
    viewModel: StoreHomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                onDestinationSelected = onNavSelect,
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AccentMint,
                )

                uiState.errorMessage != null -> Text(
                    text = uiState.errorMessage!!,
                    color = AccentRed,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    textAlign = TextAlign.Center,
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Text(
                            text = "${uiState.pendingPickupsCount} pendientes · " +
                                "${uiState.overduePickupsCount} por vencer",
                            color = TextSecondary,
                            fontSize = 14.sp,
                        )
                    }

                    item {
                        Button(
                            onClick = onScanPickup,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentMint),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = BgDarkest,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Validar retiro (escanear QR)",
                                color = BgDarkest,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    if (uiState.pendingPickups.isEmpty()) {
                        item {
                            Text(
                                text = "No hay cartas esperando retiro en tu tienda.",
                                color = TextMuted,
                                fontSize = 14.sp,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                textAlign = TextAlign.Center,
                            )
                        }
                    } else {
                        items(uiState.pendingPickups) { pickup ->
                            Surface(color = BgCard, shape = RoundedCornerShape(14.dp)) {
                                StorePickupRow(
                                    pickup = pickup,
                                    onClick = { onPickupClick(pickup.orderId) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
