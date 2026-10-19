package com.booknerd.echopin.di

import android.content.Context
import com.booknerd.echopin.data.AppDatabase
import com.booknerd.echopin.data.dao.ReminderDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getDatabase(context)

    @Provides
    fun provideReminderDao(database: AppDatabase): ReminderDao =
        database.reminderDao()
}