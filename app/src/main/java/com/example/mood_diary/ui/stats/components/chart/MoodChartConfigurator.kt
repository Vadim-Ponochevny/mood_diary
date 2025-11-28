package com.example.mood_diary.ui.stats.components.chart

import android.content.Context
import android.graphics.Color
import com.example.mood_diary.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

class MoodChartConfigurator(private val context: Context) {
    private val moodLabels = mapOf(
        1f to context.getString(R.string.mood_angry),
        2f to context.getString(R.string.mood_sad),
        3f to context.getString(R.string.mood_neutral),
        4f to context.getString(R.string.mood_happy)
    )

    fun configureChart(chart: BarChart, data: List<BarEntry>) {
        val barDataSet = createBarDataSet(data)
        val barData = BarData(barDataSet)

        chart.apply {
            setupBasicConfiguration(barData)
            setupXAxis()
            setupYAxis()
            setupAppearance()
            invalidate()
        }
    }

    private fun createBarDataSet(data: List<BarEntry>): BarDataSet {
        return BarDataSet(data, "").apply {
            color = Color.parseColor("#F5B7B1")
            valueTextColor = Color.BLACK
            valueTextSize = 12f
            valueFormatter = ZeroValueFormatter()
            setDrawValues(false)
        }
    }

    private fun BarChart.setupBasicConfiguration(barData: BarData) {
        this.data = barData
        description.isEnabled = false
        animateY(800)
        setFitBars(true)
        setScaleEnabled(false)

        setExtraOffsets(16f, 16f, 16f, 16f)
    }

    private fun BarChart.setupXAxis() {
        xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(generateWeekDayLabels())
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            setDrawAxisLine(false)
            textSize = 12f
            labelCount = 7
            granularity = 1f
        }
    }

    private fun BarChart.setupYAxis() {
        axisLeft.apply {
            valueFormatter = createMoodValueFormatter()
            axisMinimum = 0.5f
            axisMaximum = 4.5f
            granularity = 1f
            labelCount = 4

            textSize = 14f
            textColor = Color.parseColor("#333333")

        }

        axisRight.isEnabled = false
    }

    private fun BarChart.setupAppearance() {
        legend.isEnabled = false
    }

    private fun generateWeekDayLabels(): List<String> {
        val formatter = DateTimeFormatter.ofPattern("EEE", Locale("ru"))
        return (0..6).map {
            LocalDate.now().minusDays(6 - it.toLong()).format(formatter)
        }
    }

    private fun createMoodValueFormatter(): ValueFormatter {
        return object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return moodLabels[value.toInt().toFloat()] ?: ""
            }
        }
    }
}