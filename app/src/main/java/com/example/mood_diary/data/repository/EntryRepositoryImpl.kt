package com.example.mood_diary.data.repository

import com.example.mood_diary.data.database.Dao
import com.example.mood_diary.data.model.Entry
import com.example.mood_diary.domain.EntryRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EntryRepositoryImpl  @Inject constructor(
    private val dao: Dao
) : EntryRepository {

    override suspend fun upsertEntryDatabase(entry: Entry) {
        return dao.upsertEntryDatabase(entry)
    }

    override suspend fun deleteEntryDatabase(entry: Entry) {
        return dao.deleteEntryDatabase(entry)
    }

    override fun getAllEntries() : Flow<List<Entry>> {
        return dao.getAllEntries().map { list ->
            list.sortedWith(compareByDescending<Entry> { it.date }.thenByDescending { it.time })
        }
    }

    override suspend fun getEntryById(id: Int) : Entry? {
        return dao.getEntryById(id)
    }
}