package com.example.mood_diary.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import com.example.mood_diary.R
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

@Parcelize
@Entity(tableName = "Entries")
data class Entry(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val mood: Mood,
    val teg: String,
    val des: String = "",
    val date: LocalDate,
    val time: LocalTime,
) : Parcelable

enum class Mood(
    val emoji: Int,
    val labelRes: Int,
    val value: Int
) {
    SAD(R.drawable._icon_happy_smile , R.string.mood_sad, 1),
    NEUTRAL(R.drawable._icon_happy_smile, R.string.mood_neutral, 2),
    HAPPY(R.drawable._icon_happy_smile, R.string.mood_happy, 3),
    ANGRY(R.drawable._icon_happy_smile, R.string.mood_angry, 4);

    companion object {
        fun fromValue(value: Int): Mood {
            return entries.find { it.value == value } ?: NEUTRAL
        }
    }
}
