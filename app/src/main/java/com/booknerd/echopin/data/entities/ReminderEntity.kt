package com.booknerd.echopin.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val uid: Int = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "notification_text")
    val notificationText: String,

    @ColumnInfo(name = "location_latitude")
    val locationLatitude: Double,

    @ColumnInfo(name = "location_longitude")
    val locationLongitude: Double,

    @ColumnInfo(name = "location_radius")
    val locationRadius: Double,
)
