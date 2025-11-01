package com.example.mood_diary.di

import android.content.Context
import com.example.mood_diary.data.database.EntryDatabase
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Singleton
import androidx.room.Room


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNoteDatabase(
        @ApplicationContext context: Context
    ): EntryDatabase {
        return Room.databaseBuilder(
                    context,
                    EntryDatabase::class.java,
                    EntryDatabase.DATABASE_NAME
                ).fallbackToDestructiveMigration(false).build()
    }
}