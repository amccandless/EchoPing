package com.booknerd.echoping.ui.reminder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlin.math.cos
import kotlin.math.roundToInt

private const val MIN_RADIUS = 100f
private const val DEFAULT_MAX_RADIUS = 2000f
private const val METERS_PER_DEGREE_LAT = 111_320.0

@Composable
fun RadiusPicker(
    center: LatLng,
    radius: Float,
    onRadiusChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Keep older reminders with a larger radius reachable on the slider.
    val maxRadius = remember { maxOf(DEFAULT_MAX_RADIUS, radius) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, 15f)
    }
    var isMapLoaded by remember { mutableStateOf(false) }

    // Map gestures are off: the map sits inside a scrolling form, so the camera is code-driven.
    val uiSettings = remember {
        MapUiSettings(
            scrollGesturesEnabled = false,
            zoomGesturesEnabled = false,
            rotationGesturesEnabled = false,
            tiltGesturesEnabled = false,
            zoomControlsEnabled = false,
            mapToolbarEnabled = false
        )
    }

    LaunchedEffect(center, radius, isMapLoaded) {
        if (!isMapLoaded) return@LaunchedEffect
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngBounds(boundsFor(center, radius), 48),
            durationMs = 200
        )
    }

    val primary = MaterialTheme.colorScheme.primary
    val markerState = remember(center) { MarkerState(position = center) }

    Column(modifier) {
        Card(Modifier.fillMaxWidth()) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                cameraPositionState = cameraPositionState,
                uiSettings = uiSettings,
                onMapLoaded = { isMapLoaded = true }
            ) {
                Marker(state = markerState)
                Circle(
                    center = center,
                    radius = radius.toDouble(),
                    fillColor = primary.copy(alpha = 0.25f),
                    strokeColor = primary,
                    strokeWidth = 4f
                )
            }
        }

        Text(
            "Radius: ${radius.roundToInt()} m",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 8.dp)
        )
        Slider(
            value = radius,
            onValueChange = { onRadiusChange((it / 10f).roundToInt() * 10f) },
            valueRange = MIN_RADIUS..maxRadius
        )
    }
}

private fun boundsFor(center: LatLng, radiusMeters: Float): LatLngBounds {
    val dLat = radiusMeters / METERS_PER_DEGREE_LAT
    val dLng = radiusMeters / (METERS_PER_DEGREE_LAT * cos(Math.toRadians(center.latitude)))
    return LatLngBounds(
        LatLng(center.latitude - dLat, center.longitude - dLng),
        LatLng(center.latitude + dLat, center.longitude + dLng)
    )
}