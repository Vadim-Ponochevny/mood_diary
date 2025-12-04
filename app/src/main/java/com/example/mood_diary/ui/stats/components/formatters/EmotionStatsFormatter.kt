package com.example.mood_diary.ui.stats.components.formatters

import android.content.Context
import androidx.core.content.ContextCompat.getString
import com.example.mood_diary.R
import com.example.mood_diary.domain.model.Mood

object EmotionStatsFormatter {
    fun format(stats: Map<Mood, Int>, context: Context): String {
        if (stats.isEmpty())
            return R.string.fragment_mood_stats_emotionStatsTextView_nodata.toString()

        val text = getString(context, R.string.fragment_mood_stats_emotionStatsTextView)
        val sb = StringBuilder().apply {
            appendLine(text)
        }
        stats.entries
            .sortedByDescending { it.value }
            .forEach { (mood, count) ->
                sb.append(formatMoodEntry(mood, count, context))
            }
        return sb.toString()
    }

    private fun formatMoodEntry(mood: Mood, count: Int, context: Context): String {
        val moodName = context.getString(mood.labelRes)
        return "$moodName: $count \n"
    }
}