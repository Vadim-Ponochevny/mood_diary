package com.example.mood_diary.ui.stats.components.chart

import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter

class ZeroValueFormatter : ValueFormatter() {
    override fun getBarLabel(barEntry: BarEntry?): String {
        return if (barEntry?.y == 0f) "" else barEntry?.y?.toInt().toString()
    }

    // Также можно добавить для оси значений
    override fun getFormattedValue(value: Float): String {
        return if (value == 0f) "" else value.toInt().toString()
    }
}