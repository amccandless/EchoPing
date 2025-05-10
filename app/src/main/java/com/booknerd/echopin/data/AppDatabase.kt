package com.booknerd.echopin.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.booknerd.echopin.data.dao.ReminderDao
import com.booknerd.echopin.data.entities.Reminder
import kotlinx.coroutines.CoroutineScope

@Database(entities = [Reminder::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reminder_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}