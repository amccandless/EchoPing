package com.booknerd.EchoPin

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders")
    fun getAllReminders(): List<Reminder>

    @Query("SELECT * FROM reminders WHERE uid IN (:ids)")
    fun loadAllByIds(ids: IntArray): List<Reminder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg reminders: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)

    @Update
    suspend fun updateReminders(vararg reminders: Reminder)
}