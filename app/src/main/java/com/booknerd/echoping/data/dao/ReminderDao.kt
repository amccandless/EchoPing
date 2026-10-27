package com.booknerd.echoping.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.booknerd.echoping.data.entities.ReminderEntity

import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: String): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE is_active = 1")
    suspend fun getActive(): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg reminders: ReminderEntity)

    @Update
    suspend fun updateReminders(vararg reminders: ReminderEntity)

    @Delete
    suspend fun delete(reminder: ReminderEntity)
}