package com.booknerd.echoping.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val repo = EntryPointAccessors
            .fromApplication(context.applicationContext, GeofenceEntryPoint::class.java)
            .reminderRepo()

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repo.reregisterAll()
            } finally {
                pending.finish()
            }
        }
    }
}