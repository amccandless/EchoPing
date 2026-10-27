package com.booknerd.echoping.mapper

import com.booknerd.echoping.data.entities.ReminderEntity
import com.booknerd.echoping.domain.model.Reminder

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
            address = address,
            locationRadius = locationRadius,
            triggerType = triggerType,
            isActive = isActive,
        )
    }
}