package com.booknerd.echopin.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val uid: UUID,

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
    val isActive: Boolean,
)
