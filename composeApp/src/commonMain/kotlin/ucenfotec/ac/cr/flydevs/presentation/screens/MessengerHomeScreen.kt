package ucenfotec.ac.cr.flydevs.presentation.messenger

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.Surface
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.domain.model.BatchStatus
import ucenfotec.ac.cr.flydevs.domain.model.DeliveryBatch
import ucenfotec.ac.cr.flydevs.domain.model.UserRole
import ucenfotec.ac.cr.flydevs.presentation.components.BottomNav
import ucenfotec.ac.cr.flydevs.presentation.components.FlyNavDestination

private val MessengerBackground = Color(0xFF120C28)
private val MessengerSurface = Color(0xFF211747)
private val MessengerSurfaceLight = Color(0xFF2A2058)
private val MessengerPurple = Color(0xFF7C54FF)
private val MessengerPurpleLight = Color(0xFF9A7AFF)
private val MessengerYellow = Color(0xFFFFC72C)
private val MessengerTextPrimary = Color.White
private val MessengerTextSecondary = Color(0xFFA8A2CC)
private val MessengerTextMuted = Color(0xFF6B6690)

@Composable
fun MessengerHomeRoute(
    userRole: UserRole,
    viewModel: MessengerHomeViewModel = koinViewModel(),
    onScanQr: () -> Unit,
    onTakePickupPhoto: (batchId: String) -> Unit,
    onTakeDeliveryPhoto: (batchId: String) -> Unit,
    onOpenBatch: (batchId: String) -> Unit = {},
    onNavSelect: (FlyNavDestination) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val message = uiState.errorMessage
        ?: uiState.successMessage

    LaunchedEffect(uiState.courierId) {
        if (uiState.courierId == "UPClfzmesdb9zJsu0uqZIVHavbg2") {
            ucenfotec.ac.cr.flydevs.data.debug.BatchTestDataSeeder.seed(
                courierId = uiState.courierId,
                courierName = uiState.courierName.ifBlank { "Mensajero Demo" }
            )
            println("DEBUG_SEEDER: Batches seeded for courier ${uiState.courierId}")
        }
    }

    LaunchedEffect(message) {
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)

            viewModel.onEvent(
                MessengerHomeEvent.ClearMessage
            )
        }
    }

    MessengerHomeScreen(
        userRole = userRole,
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onScanQr = onScanQr,
        onTakePickupPhoto = onTakePickupPhoto,
        onTakeDeliveryPhoto = onTakeDeliveryPhoto,
        onOpenBatch = onOpenBatch,
        onNavSelect = onNavSelect
    )
}

@Composable
fun MessengerHomeScreen(
    userRole: UserRole,
    uiState: MessengerHomeUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (MessengerHomeEvent) -> Unit,
    onScanQr: () -> Unit,
    onTakePickupPhoto: (batchId: String) -> Unit,
    onTakeDeliveryPhoto: (batchId: String) -> Unit,
    onOpenBatch: (batchId: String) -> Unit,
    onNavSelect: (FlyNavDestination) -> Unit = {}
) {
    Scaffold(
        containerColor = MessengerBackground,

        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        },

        bottomBar = {
            BottomNav(
                userRole = userRole,
                currentDestination = FlyNavDestination.Deliveries,
                onDestinationSelected = onNavSelect
            )
        }
    ) { innerPadding ->

        if (
            uiState.isLoading &&
            uiState.activeBatch == null &&
            uiState.upcomingBatches.isEmpty()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MessengerPurple
                )
            }

            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 20.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                MessengerHeader(
                    courierName = uiState.courierName,
                    courierRating = uiState.courierRating
                )
            }

            item {
                MessengerStatistics(
                    todayEarnings = uiState.todayEarnings,
                    completedDeliveries = uiState.completedDeliveriesToday,
                    assignedDeliveries = uiState.assignedDeliveriesToday
                )
            }

            /*
            item {
                val activeBatch = uiState.activeBatch

                if (activeBatch != null) {
                    ActiveBatchSection(
                        batch = activeBatch,
                        isUpdating = uiState.isUpdatingBatch ||
                                uiState.isUploadingEvidence,
                        onTakePickupPhoto = {
                            onTakePickupPhoto(activeBatch.id)
                        },
                        onStartRoute = {
                            onEvent(
                                MessengerHomeEvent.StartRoute(
                                    batchId = activeBatch.id
                                )
                            )
                        },
                        onTakeDeliveryPhoto = {
                            onTakeDeliveryPhoto(activeBatch.id)
                        },
                        onOpenBatch = onOpenBatch
                    )
                } else {
                    NoActiveBatchCard()
                }
            }
            */

            item {
                ScanQrButton(
                    isLoading = uiState.isAcceptingBatch,
                    onClick = onScanQr
                )
            }

            item {
                UpcomingAssignmentsSection(
                    batches = uiState.upcomingBatches,
                    onOpenBatch = onOpenBatch
                )
            }
        }
    }
}

@Composable
private fun MessengerHeader(
    courierName: String,
    courierRating: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = MessengerYellow,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🚚",
                fontSize = 20.sp
            )
        }

        Spacer(
            modifier = Modifier.size(12.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Hola, ${
                    courierName.ifBlank { "Mensajero" }
                }",
                color = MessengerTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (courierRating > 0.0) {
                    "Mensajero verificado ★ $courierRating"
                } else {
                    "Mensajero verificado"
                },
                color = MessengerTextSecondary,
                fontSize = 12.sp
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF3A2B35))
                .padding(
                    horizontal = 12.dp,
                    vertical = 6.dp
                )
        ) {
            Text(
                text = "MENSAJERO",
                color = MessengerYellow,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MessengerStatistics(
    todayEarnings: Long,
    completedDeliveries: Int,
    assignedDeliveries: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MessengerStatCard(
            modifier = Modifier.weight(1f),
            value = todayEarnings.toColones(),
            description = "Ganancias hoy",
            highlightValue = true
        )

        MessengerStatCard(
            modifier = Modifier.weight(1f),
            value = "$completedDeliveries / $assignedDeliveries",
            description = "Entregas hoy",
            highlightValue = false
        )
    }
}

@Composable
private fun MessengerStatCard(
    modifier: Modifier,
    value: String,
    description: String,
    highlightValue: Boolean
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MessengerSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = value,
                color = if (highlightValue) {
                    MessengerYellow
                } else {
                    MessengerTextPrimary
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            Text(
                text = description,
                color = MessengerTextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ActiveBatchSection(
    batch: DeliveryBatch,
    isUpdating: Boolean,
    onTakePickupPhoto: () -> Unit,
    onStartRoute: () -> Unit,
    onTakeDeliveryPhoto: () -> Unit,
    onOpenBatch: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ENTREGA ACTIVA · LOTE #${batch.displayCode()}",
                color = MessengerYellow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = { onOpenBatch(batch.id) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "Ver detalle",
                    tint = MessengerTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

      //  BatchRouteGraphic()

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MessengerSurface
            )
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                StoreRouteRow(
                    indicatorColor = MessengerPurpleLight,
                    title = "Recoger en: ${
                        batch.sourceStoreName.ifBlank {
                            "Tienda de origen"
                        }
                    }",
                    subtitle = batch.sourceStoreAddress.ifBlank {
                        "${batch.orderIds.size} órdenes preparadas"
                    }
                )

                StoreRouteRow(
                    indicatorColor = MessengerYellow,
                    title = "Entregar en: ${
                        batch.destinationStoreName.ifBlank {
                            "Tienda de destino"
                        }
                    }",
                    subtitle = batch.destinationStoreAddress.ifBlank {
                        "Destino del lote"
                    },
                    reward = batch.courierReward
                )

                BatchProgressIndicator(
                    status = batch.status
                )

                ActiveBatchActionButton(
                    status = batch.status,
                    isLoading = isUpdating,
                    onTakePickupPhoto = onTakePickupPhoto,
                    onStartRoute = onStartRoute,
                    onTakeDeliveryPhoto = onTakeDeliveryPhoto
                )
            }
        }
    }
}

@Composable
private fun BatchRouteGraphic() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MessengerSurfaceLight
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 54.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(15.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = Color.White,
                        shape = CircleShape
                    )
                    .background(MessengerPurple)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(9) {
                    Box(
                        modifier = Modifier
                            .size(
                                width = 7.dp,
                                height = 3.dp
                            )
                            .clip(RoundedCornerShape(2.dp))
                            .background(MessengerYellow)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MessengerYellow)
            )
        }
    }
}

@Composable
private fun StoreRouteRow(
    indicatorColor: Color,
    title: String,
    subtitle: String,
    reward: Long? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MessengerSurfaceLight),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(11.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = indicatorColor,
                        shape = CircleShape
                    )
            )
        }

        Spacer(
            modifier = Modifier.size(10.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = MessengerTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                color = MessengerTextSecondary,
                fontSize = 10.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (reward != null && reward > 0L) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF554134))
                    .padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    )
            ) {
                Text(
                    text = reward.toColones(),
                    color = MessengerYellow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BatchProgressIndicator(
    status: BatchStatus
) {
    val currentStep = when (status) {
        BatchStatus.READY_FOR_PICKUP -> 0
        BatchStatus.ACCEPTED -> 0
        BatchStatus.PICKED_UP -> 1
        BatchStatus.IN_TRANSIT -> 2
        BatchStatus.DELIVERED -> 3
        BatchStatus.CANCELLED -> 0
    }

    val labels = listOf(
        "Aceptado",
        "Recogido",
        "En ruta",
        "Entregado"
    )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            labels.forEachIndexed { index, _ ->
                val completed = index < currentStep
                val selected = index == currentStep

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                completed -> MessengerPurple
                                selected -> MessengerYellow
                                else -> MessengerSurfaceLight
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (completed) {
                            "✓"
                        } else {
                            "${index + 1}"
                        },
                        color = when {
                            selected -> MessengerBackground
                            completed -> Color.White
                            else -> MessengerTextSecondary
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (index < labels.lastIndex) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(
                                if (index < currentStep) {
                                    MessengerPurple
                                } else {
                                    MessengerTextMuted
                                }
                            )
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(7.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            labels.forEach { label ->
                Text(
                    modifier = Modifier.weight(1f),
                    text = label,
                    color = MessengerTextSecondary,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
private fun ActiveBatchActionButton(
    status: BatchStatus,
    isLoading: Boolean,
    onTakePickupPhoto: () -> Unit,
    onStartRoute: () -> Unit,
    onTakeDeliveryPhoto: () -> Unit
) {
    val buttonText = when (status) {
        BatchStatus.READY_FOR_PICKUP -> "Aceptar lote"
        BatchStatus.ACCEPTED -> "Tomar evidencia de recogida"
        BatchStatus.PICKED_UP -> "Iniciar ruta"
        BatchStatus.IN_TRANSIT -> "Tomar evidencia de entrega"
        BatchStatus.DELIVERED -> "Entrega completada"
        BatchStatus.CANCELLED -> "Lote cancelado"
    }

    val enabled = !isLoading &&
            status != BatchStatus.DELIVERED &&
            status != BatchStatus.CANCELLED

    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MessengerPurple,
            disabledContainerColor = MessengerSurfaceLight
        ),
        onClick = {
            when (status) {
                BatchStatus.ACCEPTED -> {
                    onTakePickupPhoto()
                }

                BatchStatus.PICKED_UP -> {
                    onStartRoute()
                }

                BatchStatus.IN_TRANSIT -> {
                    onTakeDeliveryPhoto()
                }

                else -> Unit
            }
        }
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = buttonText,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ScanQrButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = !isLoading,
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MessengerPurple
        ),
        onClick = onClick
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "Aceptar lote — escanear QR",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun NoActiveBatchCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MessengerSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No tienes una entrega activa",
                color = MessengerTextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "Escanea el código QR de un lote para aceptarlo.",
                color = MessengerTextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun UpcomingAssignmentsSection(
    batches: List<DeliveryBatch>,
    onOpenBatch: (batchId: String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MessengerSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = "PRÓXIMAS ASIGNACIONES",
                color = MessengerYellow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            if (batches.isEmpty()) {
                Text(
                    text = "No tienes más lotes asignados.",
                    color = MessengerTextSecondary,
                    fontSize = 12.sp
                )
            } else {
                batches.forEachIndexed { index, batch ->
                    UpcomingBatchItem(
                        batch = batch,
                        onClick = {
                            onOpenBatch(batch.id)
                        }
                    )

                    if (index < batches.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(
                                vertical = 10.dp
                            ),
                            color = MessengerSurfaceLight
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingBatchItem(
    batch: DeliveryBatch,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MessengerSurfaceLight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "♤",
                color = MessengerPurpleLight,
                fontSize = 18.sp
            )
        }

        Spacer(
            modifier = Modifier.size(10.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Lote #${batch.displayCode()} · " +
                        "${batch.orderIds.size} órdenes",
                color = MessengerTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "${
                    batch.sourceStoreName.ifBlank {
                        "Tienda origen"
                    }
                } → ${
                    batch.destinationStoreName.ifBlank {
                        "Tienda destino"
                    }
                }",
                color = MessengerTextSecondary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (batch.courierReward > 0L) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MessengerSurfaceLight)
                    .padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    )
            ) {
                Text(
                    text = batch.courierReward.toColones(),
                    color = MessengerPurpleLight,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun DeliveryBatch.displayCode(): String {
    return batchId.ifBlank {
        id.takeLast(6).uppercase()
    }
}

private fun Long.toColones(): String {
    val formattedValue = toString()
        .reversed()
        .chunked(3)
        .joinToString(" ")
        .reversed()

    return "₡$formattedValue"
}
