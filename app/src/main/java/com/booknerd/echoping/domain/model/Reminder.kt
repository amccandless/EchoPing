package com.booknerd.echoping.domain.model

import java.util.UUID

data class Reminder(
    val reminderId: String = UUID.randomUUID().toString(),
    val reminderName: String,
    val notificationText: String,

    val address: String = "",
    val locationLatitude: Double,
    val locationLongitude: Double,
    val locationRadius: Float = 150f, //Meters

    val triggerType: TriggerType = TriggerType.ENTER,
    val isActive: Boolean = true,

    val schedule: Schedule = Schedule(),
    val lastTriggeredAt: Long? = null, // epoch millis of the last notification
)