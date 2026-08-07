package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun GoogleShipmentMap(
    latitude: Double,
    longitude: Double,
    markerTitle: String,
    modifier: Modifier = Modifier
)