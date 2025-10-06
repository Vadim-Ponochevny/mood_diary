package com.example.mood_diary.data.repository

import com.example.mood_diary.data.database.Dao
import com.example.mood_diary.data.model.Entry
import com.example.mood_diary.domain.EntryRepository

class EntryRepositoryImpl (
    private val dao: Dao
) : EntryRepository {

    override suspend fun upsertEntryDatabase(entry: Entry) {
        return dao.upsertEntryDatabase(entry)
    }

    override suspend fun deleteEntryDatabase(entry: Entry) {
        return dao.deleteEntryDatabase(entry)
    }

    override fun getAllEntries() {
        return dao.getAllEntries()
    }
}