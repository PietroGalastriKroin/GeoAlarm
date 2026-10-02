package com.geoalarm.app.ui.alarmedit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon

/**
 * Offline-friendly coordinate picker: a pannable OSM map (tiles cache locally once fetched,
 * so a previously-visited area keeps working without a connection) with a fixed center pin
 * and a shaded circle showing the geofence radius, in real map scale — it grows/shrinks with
 * both the radius slider and the zoom level, since it's drawn from geographic coordinates
 * rather than a fixed pixel size.
 */
@Composable
fun LocationPickerMap(
    initialLatitude: Double,
    initialLongitude: Double,
    radiusMeters: Float,
    onCenterChanged: (Double, Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val startPoint = remember {
        if (initialLatitude == 0.0 && initialLongitude == 0.0) {
            GeoPoint(-23.5505, -46.6333) // São Paulo, just a sensible default center
        } else {
            GeoPoint(initialLatitude, initialLongitude)
        }
    }

    val primaryArgb = MaterialTheme.colorScheme.primary.toArgb()
    val fillArgb = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f).toArgb()

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setUseDataConnection(true)
            controller.setZoom(15.0)
            controller.setCenter(startPoint)
        }
    }

    val radiusOverlay = remember {
        Polygon().apply {
            fillColor = fillArgb
            strokeColor = primaryArgb
            strokeWidth = 4f
            points = Polygon.pointsAsCircle(startPoint, radiusMeters.toDouble())
        }
    }

    val latestRadius = rememberUpdatedState(radiusMeters)

    DisposableEffect(mapView) {
        mapView.overlays.add(radiusOverlay)
        val listener = object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                val center = mapView.mapCenter
                onCenterChanged(center.latitude, center.longitude)
                radiusOverlay.points = Polygon.pointsAsCircle(center, latestRadius.value.toDouble())
                mapView.invalidate()
                return true
            }

            override fun onZoom(event: ZoomEvent?): Boolean = true
        }
        mapView.addMapListener(listener)
        mapView.onResume()
        onDispose {
            mapView.overlays.remove(radiusOverlay)
            mapView.onPause()
            mapView.onDetach()
        }
    }

    // Redraw the circle whenever the radius slider changes, keeping the map's current center.
    LaunchedEffect(radiusMeters) {
        radiusOverlay.points = Polygon.pointsAsCircle(mapView.mapCenter, radiusMeters.toDouble())
        mapView.invalidate()
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
