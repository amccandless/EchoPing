package com.booknerd.echopin.data.repository

import com.booknerd.echopin.data.dao.ReminderDao
import com.booknerd.echopin.data.entities.ReminderEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ReminderRepo @Inject constructor(
        private val reminderDao: ReminderDao )

    {
        fun getAllReminders(): Flow<List<ReminderEntity>> {

            return reminderDao.getAllReminders()
        }

        suspend fun insertReminder(reminder: ReminderEntity){
            reminderDao.insertAll(reminder)
        }

        suspend fun deleteReminder(reminder: ReminderEntity) {
            reminderDao.delete(reminder)
        }

    }

