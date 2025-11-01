package com.example.mood_diary.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mood_diary.data.model.Entry


@Database(
    entities = [Entry::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class EntryDatabase() : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "entries_table"
    }
    abstract val dao: Dao
}