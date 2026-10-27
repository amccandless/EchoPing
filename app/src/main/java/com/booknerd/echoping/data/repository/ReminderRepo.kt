package com.booknerd.echoping.data.repository

import androidx.room.util.copy
import com.booknerd.echoping.data.dao.ReminderDao
import com.booknerd.echoping.data.entities.ReminderEntity
import com.booknerd.echoping.domain.model.Reminder
import com.booknerd.echoping.domain.model.RepeatType
import com.booknerd.echoping.domain.model.shouldNotify
import com.booknerd.echoping.geofence.GeofenceManager
import com.booknerd.echoping.mapper.ReminderMapper.toDomain
import com.booknerd.echoping.mapper.ReminderMapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
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

    // Turning a reminder back on or editing it starts its schedule fresh, so an
    // "only once" reminder can fire again.
    suspend fun setActive(reminder: Reminder, active: Boolean) {
        val updated = reminder.copy(isActive = active, lastTriggeredAt = null)
        reminderDao.updateReminders(updated.toEntity())
        if (active) geofenceManager.register(updated) else geofenceManager.remove(updated.reminderId)
    }

    suspend fun update(reminder: Reminder) {
        val updated = reminder.copy(lastTriggeredAt = null)
        reminderDao.updateReminders(updated.toEntity())
        if (updated.isActive) geofenceManager.register(updated)
        else geofenceManager.remove(updated.reminderId)
    }

    suspend fun getById(id: String): Reminder? = reminderDao.getById(id)?.toDomain()

    /**
     * Called when a geofence fires. Returns the reminder if its schedule allows a notification
     * right now (recording the trigger), or null if it should stay quiet.
     */
    suspend fun handleTrigger(id: String, nowMillis: Long): Reminder? {
        val reminder = getById(id) ?: return null
        if (!reminder.isActive) return null

        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.ofInstant(Instant.ofEpochMilli(nowMillis), zone)
        val last = reminder.lastTriggeredAt?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), zone) }
        if (!reminder.schedule.shouldNotify(last, now)) return null

        val once = reminder.schedule.repeat == RepeatType.ONCE
        val updated = reminder.copy(lastTriggeredAt = nowMillis, isActive = reminder.isActive && !once)
        reminderDao.updateReminders(updated.toEntity())
        if (once) geofenceManager.remove(reminder.reminderId)
        return updated
    }

    suspend fun reregisterAll() {
        reminderDao.getActive().forEach { geofenceManager.register(it.toDomain()) }
    }
}
