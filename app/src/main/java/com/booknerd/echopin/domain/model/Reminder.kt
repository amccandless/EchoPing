package com.booknerd.echopin.domain.model

import java.util.UUID

class Reminder(
    val reminderId: String = UUID.randomUUID().toString(),
    val reminderName: String,
    val notificationText: String,

    val locationLatitude: Double,
    val locationLongitude: Double,
    val locationRadius: Float = 150f, //Meters

    val triggerType: TriggerType = TriggerType.ENTER,
    val isActive: Boolean = true,
)