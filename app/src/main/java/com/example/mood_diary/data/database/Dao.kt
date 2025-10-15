package com.example.mood_diary.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.mood_diary.data.model.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    @Upsert
    suspend fun upsertEntryDatabase (entry: Entry)

    @Delete
    suspend fun deleteEntryDatabase (entry: Entry)

    @Query("SELECT * FROM entries")
    fun getAllEntries () : Flow<List<Entry>>
}