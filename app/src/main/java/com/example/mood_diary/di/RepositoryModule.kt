package com.example.mood_diary.di

import com.example.mood_diary.data.database.Dao
import com.example.mood_diary.data.database.EntryDatabase
import com.example.mood_diary.data.repository.EntryRepositoryImpl
import com.example.mood_diary.domain.EntryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideNoteRepository(entryDatabase: EntryDatabase): EntryRepository {
        return EntryRepositoryImpl(dao = entryDatabase.dao)
    }
}