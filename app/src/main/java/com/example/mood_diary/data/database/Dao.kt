package com.example.mood_diary.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.mood_diary.data.model.DataEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    @Upsert
    suspend fun upsertEntryDatabase (dataEntry: DataEntry)

    @Delete
    suspend fun deleteEntryDatabase (dataEntry: DataEntry)

    @Query("SELECT * FROM entries")
    fun getAllEntries () : Flow<List<DataEntry>>

    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getEntryById (id: Int) : DataEntry?
}