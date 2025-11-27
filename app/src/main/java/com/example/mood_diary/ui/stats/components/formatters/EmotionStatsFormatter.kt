package com.example.mood_diary.ui.stats.components.formatters

import android.content.Context
import com.example.mood_diary.data.model.Mood

class EmotionStatsFormatter(private val context: Context) {
    fun format(stats: Map<Mood, Int>): String {
        if (stats.isEmpty()) return "Нет данных за период"

        val sb = StringBuilder("Количество за неделю:\n")
        stats.entries
            .sortedByDescending { it.value }
            .forEach { (mood, count) ->
                sb.append(formatMoodEntry(mood, count))
            }
        return sb.toString()
    }

    private fun formatMoodEntry(mood: Mood, count: Int): String {
        val moodName = context.getString(mood.labelRes)
        return "$moodName: $count \n"
    }
}