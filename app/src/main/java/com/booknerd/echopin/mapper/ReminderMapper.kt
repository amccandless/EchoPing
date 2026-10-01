package com.booknerd.echopin.mapper

import com.booknerd.echopin.data.entities.ReminderEntity
import com.booknerd.echopin.domain.model.Reminder

object ReminderMapper {

    //Entity to Domain
    fun ReminderEntity.toDomain(): Reminder {
        return Reminder(
            reminderId = id,
            reminderName = reminderName,
            notificationText = notificationText,
            locationLatitude = locationLatitude,
            locationLongitude = locationLongitude,
            locationRadius = locationRadius,
            triggerType = triggerType,
            isActive = isActive
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
            locationRadius = locationRadius,
            triggerType = triggerType,
            isActive = isActive
        )
    }
}