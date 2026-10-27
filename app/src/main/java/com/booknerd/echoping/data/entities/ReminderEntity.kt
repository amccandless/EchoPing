package com.booknerd.echoping.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.booknerd.echoping.domain.model.RepeatType
import com.booknerd.echoping.domain.model.RepeatUnit
import com.booknerd.echoping.domain.model.TriggerType

@Entity(tableName = "reminders")
data class ReminderEntity(
   @PrimaryKey
   @ColumnInfo(name = "id") val id: String,

   @ColumnInfo(name = "reminderName") val reminderName: String,
   @ColumnInfo(name = "notification_text") val notificationText: String,

   @ColumnInfo(name = "location_latitude") val locationLatitude: Double,
   @ColumnInfo(name = "location_longitude") val locationLongitude: Double,
   @ColumnInfo(name = "address") val address: String = "",

   @ColumnInfo(name = "location_radius") val locationRadius: Float,

   @ColumnInfo(name = "trigger_type") val triggerType: TriggerType,
   @ColumnInfo(name = "is_active") val isActive: Boolean,

   // Schedule (see domain Schedule). Defaults must match MIGRATION_2_3.
   @ColumnInfo(name = "repeat_type", defaultValue = "ALWAYS") val repeatType: RepeatType = RepeatType.ALWAYS,
   @ColumnInfo(name = "repeat_interval", defaultValue = "1") val repeatInterval: Int = 1,
   @ColumnInfo(name = "repeat_unit", defaultValue = "WEEKS") val repeatUnit: RepeatUnit = RepeatUnit.WEEKS,
   @ColumnInfo(name = "repeat_days", defaultValue = "0") val repeatDays: Int = 0, // bit (dayOfWeek.value - 1)
   @ColumnInfo(name = "repeat_day_of_month", defaultValue = "1") val repeatDayOfMonth: Int = 1,
   @ColumnInfo(name = "repeat_start_epoch_day", defaultValue = "0") val repeatStartEpochDay: Long = 0,
   @ColumnInfo(name = "window_start_minute") val windowStartMinute: Int? = null,
   @ColumnInfo(name = "window_end_minute") val windowEndMinute: Int? = null,
   @ColumnInfo(name = "last_triggered_at") val lastTriggeredAt: Long? = null,
   )
