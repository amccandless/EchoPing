package com.booknerd.echoping.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.booknerd.echoping.data.dao.ReminderDao
import com.booknerd.echoping.data.entities.ReminderEntity
import kotlinx.coroutines.CoroutineScope

@Database(entities = [ReminderEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun reminderDao(): ReminderDao

    companion object {
        // Adds repeat schedules. Column defaults must match ReminderEntity's @ColumnInfo values.
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                listOf(
                    "repeat_type TEXT NOT NULL DEFAULT 'ALWAYS'",
                    "repeat_interval INTEGER NOT NULL DEFAULT 1",
                    "repeat_unit TEXT NOT NULL DEFAULT 'WEEKS'",
                    "repeat_days INTEGER NOT NULL DEFAULT 0",
                    "repeat_day_of_month INTEGER NOT NULL DEFAULT 1",
                    "repeat_start_epoch_day INTEGER NOT NULL DEFAULT 0",
                    "window_start_minute INTEGER",
                    "window_end_minute INTEGER",
                    "last_triggered_at INTEGER",
                ).forEach { db.execSQL("ALTER TABLE reminders ADD COLUMN $it") }
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reminder_database"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}