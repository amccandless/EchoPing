package com.booknerd.echopin.domain.model

import java.util.UUID

class Reminder(
    val reminderId: UUID,               //Id for reminder
    val reminderName: String,          //Name of the reminder
    val locationLatitude: Double,              //Latitude for location
    val locationLongitude: Double,            //Longitude for location
    val locationRadius: Double,                 //Trigger radius in meters
    val isActive: Boolean = true,      //Whether the reminder is active or not
    val notificationText: String       //Notification message
)