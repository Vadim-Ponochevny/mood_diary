package com.example.mood_diary.data.repository

import com.example.mood_diary.data.database.Dao
import com.example.mood_diary.data.toDomain
import com.example.mood_diary.data.toData
import com.example.mood_diary.domain.model.DomainEntry
import com.example.mood_diary.domain.repository.EntryRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EntryRepositoryImpl @Inject constructor(
    private val dao: Dao
) : EntryRepository {

    override suspend fun upsertEntryDatabase(domainEntry: DomainEntry) {
        return dao.upsertEntryDatabase(domainEntry.toData())
    }

    override suspend fun deleteEntryDatabase(domainEntry: DomainEntry) {
        return dao.deleteEntryDatabase(domainEntry.toData())
    }

    override fun getAllEntries() : Flow<List<DomainEntry>> {
        return dao.getAllEntries()
            .map { dataList ->

                val domainList = dataList.map {
                    it.toDomain()
                }

                domainList
                    .sortedWith(compareByDescending<DomainEntry> { it.date }
                    .thenByDescending { it.time })
            }

    }

    override suspend fun getEntryById(id: Int) : DomainEntry? {
        val dataEntry = dao.getEntryById(id)

        return dataEntry?.toDomain()
    }
}