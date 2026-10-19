package com.booknerd.echopin.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.booknerd.echopin.data.repository.ReminderRepo
import com.booknerd.echopin.notification.ReminderNotifier
import com.google.android.gms.location.GeofencingEvent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface GeofenceEntryPoint {
    fun reminderRepo(): ReminderRepo
}

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) return

        val repo = EntryPointAccessors
            .fromApplication(context.applicationContext, GeofenceEntryPoint::class.java)
            .reminderRepo()

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                event.triggeringGeofences?.forEach { geofence ->
                    repo.getById(geofence.requestId)?.let { reminder ->
                        ReminderNotifier.show(context, reminder)
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }
}