package com.booknerd.echoping.geofence

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.booknerd.echoping.domain.model.Reminder
import com.booknerd.echoping.domain.model.TriggerType
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class GeofenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client = LocationServices.getGeofencingClient(context)

    private val pendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    @SuppressLint("MissingPermission")
    fun register(reminder: Reminder) {
        val transitions = when (reminder.triggerType) {
            TriggerType.ENTER -> Geofence.GEOFENCE_TRANSITION_ENTER
            TriggerType.EXIT -> Geofence.GEOFENCE_TRANSITION_EXIT
            TriggerType.BOTH -> Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
        }

        val geofence = Geofence.Builder()
            .setRequestId(reminder.reminderId.toString())
            .setCircularRegion(
                reminder.locationLatitude,
                reminder.locationLongitude,
                reminder.locationRadius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(transitions)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(0) // don't fire if already inside when registered
            .addGeofence(geofence)
            .build()

        client.addGeofences(request, pendingIntent)
    }

    fun remove(reminderId: String) {
        client.removeGeofences(listOf(reminderId))
    }
}