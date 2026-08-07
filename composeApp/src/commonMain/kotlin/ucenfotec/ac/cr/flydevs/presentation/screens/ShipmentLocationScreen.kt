package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ucenfotec.ac.cr.flydevs.presentation.components.GoogleShipmentMap
import ucenfotec.ac.cr.flydevs.presentation.components.TopBar
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentGold
import ucenfotec.ac.cr.flydevs.presentation.theme.BgCard
import ucenfotec.ac.cr.flydevs.presentation.theme.BgDarkest
import ucenfotec.ac.cr.flydevs.presentation.theme.TextPrimary
import ucenfotec.ac.cr.flydevs.presentation.theme.TextSecondary

@Composable
fun ShipmentLocationScreen(
    batchId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BgDarkest,
        topBar = {
            Column {
                Spacer(
                    modifier = Modifier.statusBarsPadding()
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

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                color = BgCard,
                shape = RoundedCornerShape(18.dp)
            ) {
                /*
                 * Coordenada temporal para comprobar
                 * que la API key y Google Maps funcionan.
                 */
                GoogleShipmentMap(
                    latitude = 9.930050,
                    longitude = -84.239348,
                    markerTitle = "Envío FlyDevs",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = BgCard,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = "Última actualización",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Text(
                    text = "Ubicación de prueba",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
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
        batchId.takeLast(6).uppercase()
    }
}