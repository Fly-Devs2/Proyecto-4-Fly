package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ucenfotec.ac.cr.flydevs.presentation.components.GoogleShipmentMap
import ucenfotec.ac.cr.flydevs.presentation.components.TopBar
import ucenfotec.ac.cr.flydevs.presentation.shipmentLocation.ShipmentLocationUiState
import ucenfotec.ac.cr.flydevs.presentation.shipmentLocation.ShipmentLocationViewModel
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentGold
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentRed
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentViolet
import ucenfotec.ac.cr.flydevs.presentation.theme.BgCard
import ucenfotec.ac.cr.flydevs.presentation.theme.BgDarkest
import ucenfotec.ac.cr.flydevs.presentation.theme.TextPrimary
import ucenfotec.ac.cr.flydevs.presentation.theme.TextSecondary
import ucenfotec.ac.cr.flydevs.presentation.util.formatDateTime

@Composable
fun ShipmentLocationScreen(
    batchId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ShipmentLocationViewModel =
        koinViewModel(
            parameters = {
                parametersOf(batchId)
            }
        )
) {
    val state by
    viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BgDarkest,
        topBar = {
            Column {
                Spacer(
                    modifier =
                        Modifier.statusBarsPadding()
                )

                TopBar(
                    title = "Ubicación del envío",
                    onBack = onBack
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = "LOTE EN TRÁNSITO",
                color = AccentGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Lote #${shortBatchCode(batchId)}",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            when {

                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = AccentViolet
                        )
                    }
                }

                state.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment =
                            Alignment.CenterHorizontally,
                        verticalArrangement =
                            Arrangement.Center
                    ) {
                        Text(
                            text =
                                state.errorMessage.orEmpty(),
                            color = AccentRed,
                            fontSize = 14.sp
                        )

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )

                        Button(
                            onClick = viewModel::retry
                        ) {
                            Text("Reintentar")
                        }
                    }
                }

                !state.hasValidLocation -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Text(
                            text =
                                "Ubicación no disponible",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }

                else -> {

                    val location =
                        state.location!!

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        color = BgCard,
                        shape =
                            RoundedCornerShape(18.dp)
                    ) {

                        GoogleShipmentMap(
                            latitude =
                                location.latitude,
                            longitude =
                                location.longitude,
                            markerTitle =
                                "Envío FlyDevs",
                            modifier =
                                Modifier.fillMaxSize()
                        )
                    }
                }
            }

            LocationInformationCard(
                state = state
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }
    }
}

@Composable
private fun LocationInformationCard(
    state: ShipmentLocationUiState
) {
    val location = state.location

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = BgCard,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(16.dp),
        verticalArrangement =
            Arrangement.spacedBy(5.dp)
    ) {

        Text(
            text = "Última actualización",
            color = TextSecondary,
            fontSize = 11.sp
        )

        Text(
            text =
                if (
                    location != null &&
                    location.updatedAt > 0L
                ) {
                    formatDateTime(
                        location.updatedAt
                    )
                } else {
                    "Sin actualizaciones"
                },
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        if (
            location != null &&
            state.hasValidLocation
        ) {

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Text(
                text =
                    if (location.trackingActive) {
                        "Ubicación en tiempo real"
                    } else {
                        "Seguimiento finalizado"
                    },
                color =
                    if (location.trackingActive) {
                        AccentViolet
                    } else {
                        TextSecondary
                    },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun shortBatchCode(
    batchId: String
): String {
    return if (batchId.isBlank()) {
        "—"
    } else {
        batchId
            .takeLast(6)
            .uppercase()
    }
}