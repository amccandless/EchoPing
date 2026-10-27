package com.booknerd.echoping

import com.booknerd.echoping.data.entities.ReminderEntity
import com.booknerd.echoping.domain.model.Reminder
import com.booknerd.echoping.domain.model.RepeatType
import com.booknerd.echoping.domain.model.RepeatUnit
import com.booknerd.echoping.domain.model.Schedule
import com.booknerd.echoping.domain.model.TriggerType
import org.junit.Assert.assertEquals
import org.junit.Test

import com.booknerd.echoping.mapper.ReminderMapper.toDomain
import com.booknerd.echoping.mapper.ReminderMapper.toEntity
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.random.Random

class ReminderMapperTest {

    @Test
    fun activeReminderEntitytoDomainTest() {
        // Given
        val reminderEntity = ReminderEntity(
            id = "abc-123",
            reminderName = "Test Reminder",
            notificationText = "Don't forget!",
            locationLatitude = Random.nextDouble(-90.0000,90.0000,),
            locationLongitude = Random.nextDouble(-180.0000,180.0000),
            address = "1 Main St",
            locationRadius = Random.nextDouble(100.00,500.00).toFloat(),
            triggerType = TriggerType.EXIT,
            isActive = true
        )

        // When
        val reminderDomain = reminderEntity.toDomain()

        // Then
        assertEquals(reminderEntity.id, reminderDomain.reminderId)
        assertEquals(reminderEntity.reminderName, reminderDomain.reminderName)
        assertEquals(reminderEntity.notificationText, reminderDomain.notificationText)
        assertEquals(reminderEntity.locationLatitude, reminderDomain.locationLatitude, 0.0001)
        assertEquals(reminderEntity.locationLongitude, reminderDomain.locationLongitude, 0.0001)
        assertEquals(reminderEntity.address, reminderDomain.address)
        assertEquals(reminderEntity.locationRadius, reminderDomain.locationRadius, 0.0001f)
        assertEquals(reminderEntity.triggerType, reminderDomain.triggerType)
        assertEquals(reminderEntity.isActive, reminderDomain.isActive)
        assertEquals(RepeatType.ALWAYS, reminderDomain.schedule.repeat)
    }

    @Test
    fun inactiveReminderEntitytoDomainTest() {
        // Given
        val reminderEntity = ReminderEntity(
            id = "def-456",
            reminderName = "Inactive Reminder",
            notificationText = "This was snoozed.",
            locationLatitude = Random.nextDouble(-90.0000,90.0000,),
            locationLongitude = Random.nextDouble(-180.0000,180.0000),
            locationRadius = 150f,
            triggerType = TriggerType.ENTER,
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
            reminderName = "Domain Reminder",
            notificationText = "Time to go!",
            locationLatitude = Random.nextDouble(-90.0000,90.0000,),
            locationLongitude = Random.nextDouble(-180.0000,180.0000),
            locationRadius = 250f,
            isActive = true
        )

        // When
        val reminderEntity = reminderDomain.toEntity()

        // Then
        assertEquals(reminderDomain.reminderId, reminderEntity.id)
        assertEquals(reminderDomain.reminderName, reminderEntity.reminderName)
        assertEquals(reminderDomain.notificationText, reminderEntity.notificationText)
        assertEquals(reminderDomain.locationLatitude, reminderEntity.locationLatitude, 0.0001)
        assertEquals(reminderDomain.locationLongitude, reminderEntity.locationLongitude, 0.0001)
        assertEquals(reminderDomain.locationRadius, reminderEntity.locationRadius, 0.0001f)
    }

    @Test
    fun scheduleRoundTripsThroughEntity() {
        // Given
        val reminderDomain = Reminder(
            reminderName = "Trash night",
            notificationText = "Take out the bins",
            locationLatitude = 30.0,
            locationLongitude = -97.0,
            schedule = Schedule(
                repeat = RepeatType.CUSTOM,
                interval = 2,
                unit = RepeatUnit.WEEKS,
                daysOfWeek = setOf(DayOfWeek.TUESDAY, DayOfWeek.SUNDAY),
                dayOfMonth = 15,
                startDate = LocalDate.of(2026, 10, 27),
                windowStartMinute = 17 * 60,
                windowEndMinute = 0
            ),
            lastTriggeredAt = 1_700_000_000_000
        )

        // When
        val roundTripped = reminderDomain.toEntity().toDomain()

        // Then
        assertEquals(reminderDomain, roundTripped)
    }
}
