package com.example.mood_diary.domain.repository

import com.example.mood_diary.domain.model.DomainEntry
import kotlinx.coroutines.flow.Flow

interface EntryRepository {

    suspend fun upsertEntryDatabase (domainEntry: DomainEntry)

    suspend fun deleteEntryDatabase (domainEntry: DomainEntry)

    suspend fun getEntryById (id: Int) : DomainEntry?

    fun getAllEntries () : Flow<List<DomainEntry>>
}