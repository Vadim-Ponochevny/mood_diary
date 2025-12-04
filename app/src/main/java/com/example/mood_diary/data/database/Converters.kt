package com.example.mood_diary.data.database

import androidx.room.TypeConverter
import com.example.mood_diary.data.model.Mood
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
class Converters {
    @TypeConverter
    fun fromMoodToValue (mood: Mood) : Int {
        return mood.value
    }

    @TypeConverter
    fun fromValueToMood (value: Int) : Mood {
        return Mood.fromValue(value)
    }

//    @TypeConverter
//    fun fromLocalDate(date: LocalDate): String {
//        return date.toString()
//    }
//
//    @TypeConverter
//    fun toLocalDate(value: String): LocalDate {
//        return LocalDate.parse(value)
//    }
//
//    @TypeConverter
//    fun fromLocalTime(dateTime: LocalTime): String {
//        return dateTime.toString()
//    }
//
//    @TypeConverter
//    fun toLocalTime(value: String): LocalTime {
//        return LocalTime.parse(value)
//    }
}