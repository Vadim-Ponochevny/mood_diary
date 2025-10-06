package com.example.mood_diary.domain

import com.example.mood_diary.data.model.Entry

interface EntryRepository {

    suspend fun upsertEntryDatabase (entry: Entry)

    suspend fun deleteEntryDatabase (entry: Entry)

    fun getAllEntries ()
}