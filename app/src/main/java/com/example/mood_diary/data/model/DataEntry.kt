package com.example.mood_diary.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import com.example.mood_diary.R

@Parcelize
@Entity(tableName = "Entries")
data class DataEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val mood: Mood,
    val teg: String,
    val des: String = "",
    val date: String,
    val time: String,
) : Parcelable

enum class Mood(
    val emoji: Int,
    val labelRes: Int,
    val value: Int
) {
    HAPPY(R.drawable.ic_emoji_happy_smile, R.string.mood_happy, 1),
    NEUTRAL(R.drawable.ic_emoji_neutral, R.string.mood_neutral, 2),
    SAD(R.drawable.ic_emoji_sad, R.string.mood_sad, 3),
    ANGRY(R.drawable.ic_emoji_angry, R.string.mood_angry, 4);

    companion object {
        fun fromValue(value: Int): Mood {
            return entries.find { it.value == value } ?: NEUTRAL
        }
    }
}
