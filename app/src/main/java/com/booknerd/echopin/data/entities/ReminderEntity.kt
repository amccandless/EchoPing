package com.booknerd.echopin.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.booknerd.echopin.domain.model.TriggerType

@Entity(tableName = "reminders")
data class ReminderEntity(
   @PrimaryKey
   @ColumnInfo(name = "id") val id: String,

   @ColumnInfo(name = "reminderName") val reminderName: String,
   @ColumnInfo(name = "notification_text") val notificationText: String,

   @ColumnInfo(name = "location_latitude") val locationLatitude: Double,
   @ColumnInfo(name = "location_longitude") val locationLongitude: Double,

   @ColumnInfo(name = "location_radius") val locationRadius: Float,

   @ColumnInfo(name = "trigger_type") val triggerType: TriggerType,
   @ColumnInfo(name = "is_active") val isActive: Boolean,

   )
