package com.booknerd.echopin.domain.model

class Reminder(
    val reminderId: Int,               //Id for reminder
    val reminderName: String,          //Name of the reminder
    val locationLatitude: Double,              //Latitude for location
    val locationLongitude: Double,            //Longitude for location
    val locationRadius: Double,                 //Trigger radius in meters
    val isActive: Boolean = true,      //Whether the reminder is active or not
    val notificationText: String       //Notification message
)