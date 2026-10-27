package com.booknerd.echoping.data.geocoding

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

data class GeoResult(val label: String, val latitude: Double, val longitude: Double)

@Singleton
class GeocodingService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun search(query: String): List<GeoResult> = withContext(Dispatchers.IO) {
        if (query.isBlank() || !Geocoder.isPresent()) return@withContext emptyList()
        val geocoder = Geocoder(context, Locale.getDefault())

        try {
            if (Build.VERSION.SDK_INT >= 33) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocationName(query, 5, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            cont.resume(addresses.map { it.toResult() })
                        }
                        override fun onError(errorMessage: String?) {
                            cont.resume(emptyList())
                        }
                    })
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(query, 5)?.map { it.toResult() } ?: emptyList()
            }
        } catch (e: IOException) {
            emptyList()
        }
    }

    private fun Address.toResult() = GeoResult(
        label = getAddressLine(0) ?: "$latitude, $longitude",
        latitude = latitude,
        longitude = longitude
    )
}