package com.booknerd.echoping.mapper

import com.booknerd.echoping.data.entities.ReminderEntity
import com.booknerd.echoping.domain.model.Reminder
import com.booknerd.echoping.domain.model.Schedule
import java.time.DayOfWeek
import java.time.LocalDate

object ReminderMapper {

    //Entity to Domain
    fun ReminderEntity.toDomain(): Reminder {
        return Reminder(
            reminderId = id,
            reminderName = reminderName,
            notificationText = notificationText,
            locationLatitude = locationLatitude,
            locationLongitude = locationLongitude,
            address = address,
            locationRadius = locationRadius,
            triggerType = triggerType,
            isActive = isActive,
            schedule = Schedule(
                repeat = repeatType,
                interval = repeatInterval,
                unit = repeatUnit,
                daysOfWeek = daysFromMask(repeatDays),
                dayOfMonth = repeatDayOfMonth,
                // 0 is the migration default for reminders created before schedules existed.
                startDate = if (repeatStartEpochDay == 0L) LocalDate.now()
                else LocalDate.ofEpochDay(repeatStartEpochDay),
                windowStartMinute = windowStartMinute,
                windowEndMinute = windowEndMinute
            ),
            lastTriggeredAt = lastTriggeredAt
        )
    }

    //Domain to Entity
    fun Reminder.toEntity(): ReminderEntity {
        return ReminderEntity(
            id = reminderId,
            reminderName = reminderName,
            notificationText = notificationText,
            locationLatitude = locationLatitude,
            locationLongitude = locationLongitude,
            address = address,
            locationRadius = locationRadius,
            triggerType = triggerType,
            isActive = isActive,
            repeatType = schedule.repeat,
            repeatInterval = schedule.interval,
            repeatUnit = schedule.unit,
            repeatDays = daysToMask(schedule.daysOfWeek),
            repeatDayOfMonth = schedule.dayOfMonth,
            repeatStartEpochDay = schedule.startDate.toEpochDay(),
            windowStartMinute = schedule.windowStartMinute,
            windowEndMinute = schedule.windowEndMinute,
            lastTriggeredAt = lastTriggeredAt
        )
    }

    private fun daysToMask(days: Set<DayOfWeek>): Int =
        days.fold(0) { mask, day -> mask or (1 shl (day.value - 1)) }

    private fun daysFromMask(mask: Int): Set<DayOfWeek> =
        DayOfWeek.entries.filter { mask and (1 shl (it.value - 1)) != 0 }.toSet()
}
