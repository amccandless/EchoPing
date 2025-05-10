package com.booknerd.echopin.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.booknerd.echopin.data.entities.ReminderEntity

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders")
    fun getAllReminders(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE uid IN (:ids)")
    fun loadAllByIds(ids: IntArray): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg reminders: ReminderEntity)

    @Delete
    suspend fun delete(reminder: ReminderEntity)

    @Update
    suspend fun updateReminders(vararg reminders: ReminderEntity)
}