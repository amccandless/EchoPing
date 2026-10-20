package com.booknerd.echopin.data.repository

import androidx.room.util.copy
import com.booknerd.echopin.data.dao.ReminderDao
import com.booknerd.echopin.data.entities.ReminderEntity
import com.booknerd.echopin.domain.model.Reminder
import com.booknerd.echopin.geofence.GeofenceManager
import com.booknerd.echopin.mapper.ReminderMapper.toDomain
import com.booknerd.echopin.mapper.ReminderMapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepo @Inject constructor(
    private val reminderDao: ReminderDao,
    private val geofenceManager: GeofenceManager
) {
    val reminders: Flow<List<Reminder>> =
        reminderDao.getAllReminders().map { list -> list.map { it.toDomain() } }

    suspend fun add(reminder: Reminder) {
        reminderDao.insertAll(reminder.toEntity())
        if (reminder.isActive) geofenceManager.register(reminder)
    }

    suspend fun delete(reminder: Reminder) {
        reminderDao.delete(reminder.toEntity())
        geofenceManager.remove(reminder.reminderId)
    }

    suspend fun setActive(reminder: Reminder, active: Boolean) {
        val updated = reminder.copy(isActive = active)
        reminderDao.updateReminders(updated.toEntity())
        if (active) geofenceManager.register(updated) else geofenceManager.remove(updated.reminderId)
    }

    suspend fun update(reminder: Reminder) {
        reminderDao.updateReminders(reminder.toEntity())
        if (reminder.isActive) geofenceManager.register(reminder)
        else geofenceManager.remove(reminder.reminderId)
    }

    suspend fun getById(id: String): Reminder? = reminderDao.getById(id)?.toDomain()

    suspend fun reregisterAll() {
        reminderDao.getActive().forEach { geofenceManager.register(it.toDomain()) }
    }
}
