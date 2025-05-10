package com.booknerd.EchoPin.model

class Reminder(
    val reminderId: Int,               //Id for reminder
    val reminderName: String,          //Name of the reminder
    val latitude: Double,              //Latitude for location
    val longtitude: Double,            //Longitude for location
    val radius: Float,                 //Trigger radius in meters
    val isActive: Boolean = true,      //Whether the reminder is active or not

)