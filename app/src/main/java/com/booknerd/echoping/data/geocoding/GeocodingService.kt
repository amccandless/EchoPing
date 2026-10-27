package com.booknerd.echoping.data.geocoding

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.math.cos

data class GeoResult(val label: String, val latitude: Double, val longitude: Double)

private const val MAX_RESULTS = 5
// Half-size of the "nearby" search box, roughly 55 km.
private const val NEARBY_DEGREES = 0.5

@Singleton
class GeocodingService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private data class Box(val south: Double, val west: Double, val north: Double, val east: Double)

    // Searches near the user first so partial input like "123 Main St" resolves locally,
    // then falls back to a worldwide search.
    suspend fun search(query: String): List<GeoResult> = withContext(Dispatchers.IO) {
        if (query.isBlank() || !Geocoder.isPresent()) return@withContext emptyList()
        val geocoder = Geocoder(context, Locale.getDefault())

        try {
            val nearby = lastLocation()?.let { geocode(geocoder, query, boxAround(it)) }.orEmpty()
            nearby.ifEmpty { geocode(geocoder, query, null) }
        } catch (e: IOException) {
            emptyList()
        } catch (e: IllegalArgumentException) {
            emptyList()
        }
    }

    private suspend fun geocode(geocoder: Geocoder, query: String, box: Box?): List<GeoResult> {
        if (Build.VERSION.SDK_INT >= 33) {
            return suspendCancellableCoroutine { cont ->
                val listener = object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        cont.resume(addresses.map { it.toResult() })
                    }
                    override fun onError(errorMessage: String?) {
                        cont.resume(emptyList())
                    }
                }
                if (box == null) {
                    geocoder.getFromLocationName(query, MAX_RESULTS, listener)
                } else {
                    geocoder.getFromLocationName(
                        query, MAX_RESULTS, box.south, box.west, box.north, box.east, listener
                    )
                }
            }
        }
        @Suppress("DEPRECATION")
        val addresses = if (box == null) {
            geocoder.getFromLocationName(query, MAX_RESULTS)
        } else {
            geocoder.getFromLocationName(query, MAX_RESULTS, box.south, box.west, box.north, box.east)
        }
        return addresses?.map { it.toResult() }.orEmpty()
    }

    @SuppressLint("MissingPermission")
    private suspend fun lastLocation(): Location? {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return null

        return suspendCancellableCoroutine { cont ->
            LocationServices.getFusedLocationProviderClient(context).lastLocation
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
        }
    }

    private fun boxAround(location: Location): Box {
        val dLng = NEARBY_DEGREES / cos(Math.toRadians(location.latitude)).coerceAtLeast(0.1)
        return Box(
            south = (location.latitude - NEARBY_DEGREES).coerceAtLeast(-90.0),
            west = (location.longitude - dLng).coerceAtLeast(-180.0),
            north = (location.latitude + NEARBY_DEGREES).coerceAtMost(90.0),
            east = (location.longitude + dLng).coerceAtMost(180.0)
        )
    }

    private fun Address.toResult() = GeoResult(
        label = getAddressLine(0) ?: "$latitude, $longitude",
        latitude = latitude,
        longitude = longitude
    )
}
