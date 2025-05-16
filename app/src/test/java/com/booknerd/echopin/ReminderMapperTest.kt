package com.booknerd.echopin

import com.booknerd.echopin.data.entities.ReminderEntity
import com.booknerd.echopin.domain.model.Reminder
import org.junit.Assert.assertEquals
import org.junit.Test

import com.booknerd.echopin.mapper.ReminderMapper.toDomain
import com.booknerd.echopin.mapper.ReminderMapper.toEntity
import kotlin.random.Random

class ReminderMapperTest {

    @Test
    fun activeReminderEntitytoDomainTest() {
        // Given
        val reminderEntity = ReminderEntity(
            uid = Random.nextInt(1,10),
            title = "Test Reminder",
            notificationText = "Don't forget!",
            locationLatitude = Random.nextDouble(-90.0000,90.0000,),
            locationLongitude = Random.nextDouble(-180.0000,180.0000),
            locationRadius = Random.nextDouble(5.00,100.00),
            isActive = true
        )

        // When
        val reminderDomain = reminderEntity.toDomain()

        // Then
        assertEquals(reminderEntity.uid, reminderDomain.reminderId)
        assertEquals(reminderEntity.title, reminderDomain.reminderName)
        assertEquals(reminderEntity.notificationText, reminderDomain.notificationText)
        assertEquals(reminderEntity.locationLatitude, reminderDomain.locationLatitude, 0.0001)
        assertEquals(reminderEntity.locationLongitude, reminderDomain.locationLongitude, 0.0001)
        assertEquals(reminderEntity.locationRadius, reminderDomain.locationRadius, 0.0001)
        assertEquals(reminderEntity.isActive, reminderDomain.isActive)
    }

    @Test
    fun inactiveReminderEntitytoDomainTest() {
        // Given
        val reminderEntity = ReminderEntity(
            uid = Random.nextInt(1,10),
            title = "Inactive Reminder",
            notificationText = "This was snoozed.",
            locationLatitude = Random.nextDouble(-90.0000,90.0000,),
            locationLongitude = Random.nextDouble(-180.0000,180.0000),
            locationRadius = Random.nextDouble(5.00,100.00),
            isActive = false
        )

        // When
        val reminderDomain = reminderEntity.toDomain()

        // Then
        assertEquals(reminderEntity.isActive, reminderDomain.isActive)
    }

    @Test
    fun `given a Reminder domain object, toEntity should map it correctly to a ReminderEntity`() {
        // Given
        val reminderDomain = Reminder(
            reminderId = Random.nextInt(1,10),
            reminderName = "Domain Reminder",
            notificationText = "Time to go!",
            locationLatitude = Random.nextDouble(-90.0000,90.0000,),
            locationLongitude = Random.nextDouble(-180.0000,180.0000),
            locationRadius = Random.nextDouble(5.00,100.00),
            isActive = true
        )

        // When
        val reminderEntity = reminderDomain.toEntity()

        // Then
        assertEquals(reminderDomain.reminderId, reminderEntity.uid)
        assertEquals(reminderDomain.reminderName, reminderEntity.title)
        assertEquals(reminderDomain.notificationText, reminderEntity.notificationText)
        assertEquals(reminderDomain.locationLatitude, reminderEntity.locationLatitude, 0.0001)
        assertEquals(reminderDomain.locationLongitude, reminderEntity.locationLongitude, 0.0001)
        assertEquals(reminderDomain.locationRadius, reminderEntity.locationRadius, 0.0001)
    }
}