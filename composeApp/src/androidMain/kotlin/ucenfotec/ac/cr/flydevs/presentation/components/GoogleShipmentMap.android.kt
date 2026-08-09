package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
actual fun GoogleShipmentMap(
    latitude: Double,
    longitude: Double,
    markerTitle: String,
    modifier: Modifier
) {
    val currentPosition =
        LatLng(
            latitude,
            longitude
        )

    val markerState =
        rememberUpdatedMarkerState(
            position = currentPosition
        )

    val cameraPositionState =
        rememberCameraPositionState {
            position =
                CameraPosition.fromLatLngZoom(
                    currentPosition,
                    DEFAULT_ZOOM
                )
        }

    var mapLoaded by remember {
        mutableStateOf(false)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        onMapLoaded = {
            mapLoaded = true
        }
    ) {
        Marker(
            state = markerState,
            title = markerTitle,
            snippet = "Posición actual del envío"
        )
    }

    LaunchedEffect(
        latitude,
        longitude,
        mapLoaded
    ) {
        if (!mapLoaded) {
            return@LaunchedEffect
        }

        cameraPositionState.animate(
            update =
                CameraUpdateFactory.newLatLng(
                    currentPosition
                ),
            durationMs = CAMERA_ANIMATION_DURATION
        )
    }
}

private const val DEFAULT_ZOOM = 15f
private const val CAMERA_ANIMATION_DURATION = 900
