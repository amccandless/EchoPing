package com.booknerd.echopin.mapper

import com.booknerd.echopin.data.entities.ReminderEntity
import com.booknerd.echopin.domain.model.Reminder

object ReminderMapper {

    //Entity to Domain
    fun ReminderEntity.toDomain(): Reminder {
        return Reminder(
            reminderId = uid,
            reminderName = title,
            locationLatitude = locationLatitude,
            locationLongitude = locationLongitude,
            locationRadius = locationRadius,
            notificationText = notificationText,
            isActive = isActive // Using the isActive from the entity
        )
    }

    //Domain to Entity
    fun Reminder.toEntity(): ReminderEntity {
        return ReminderEntity(
            uid = reminderId,
            title = reminderName,
            notificationText = notificationText,
            locationLatitude = locationLatitude,
            locationLongitude = locationLongitude,
            locationRadius = locationRadius,
            isActive = isActive // Persisting the isActive state
        )
    }
}