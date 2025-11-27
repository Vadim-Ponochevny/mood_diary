package com.example.mood_diary.ui.stats.components.formatters

import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

class DateRangeFormatter(private val locale: Locale) {
    fun getDateRangeText(): String {
        val today = LocalDate.now()
        val startDate = today.minusDays(6)
        val formatter = DateTimeFormatter.ofPattern("d MMM", locale)

        return "${startDate.format(formatter)} – ${today.format(formatter)}"
    }
}