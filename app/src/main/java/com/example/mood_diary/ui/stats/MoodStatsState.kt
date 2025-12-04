package com.example.mood_diary.ui.stats

import com.example.mood_diary.domain.model.DomainEntry
import com.example.mood_diary.domain.model.Mood
import com.github.mikephil.charting.data.BarEntry

data class MoodStatsState(
    val isLoading: Boolean = true,
    val entries: List<DomainEntry> = emptyList(),
    val chartData: List<BarEntry> = emptyList(),
    val emotionStats: Map<Mood, Int> = emptyMap(),
    val error: String? = null
)
