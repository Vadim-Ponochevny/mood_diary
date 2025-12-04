package com.example.mood_diary.ui.common

import com.example.mood_diary.domain.model.Mood

data class MoodFilterItem(
    val mood: Mood,
    var isSelected: Boolean = false
)
