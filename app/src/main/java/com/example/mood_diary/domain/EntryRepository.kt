package com.example.mood_diary.domain

import com.example.mood_diary.data.model.Entry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface EntryRepository {

    suspend fun upsertEntryDatabase (entry: Entry)

    suspend fun deleteEntryDatabase (entry: Entry)

    suspend fun getEntryById (id: Int) : Entry?

    fun getAllEntries () : Flow<List<Entry>>
}