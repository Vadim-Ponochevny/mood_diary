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

object MoodChartConfigurator {

    private const val ANIMATION_DURATION_MILLIS = 800
    private const val BAR_COLOR_HEX = "#F5B7B1"
    private const val LABEL_TEXT_SIZE_SP = 12f
    private const val TEXT_COLOR_HEX = "#333333"
    private const val EXTRA_OFFSET_DP = 16f

    private const val MIN_Y_AXIS = 0.5f
    private const val MAX_Y_AXIS = 4.5f
    private const val GRANULARITY = 1f
    private const val LABEL_COUNT_Y = 4
    private const val LABEL_COUNT_X = 7
    private const val VALUE_GRANULARITY_X = 1f

    private fun createMoodLabels (context: Context) = mapOf(
        1f to context.getString(R.string.mood_angry),
        2f to context.getString(R.string.mood_sad),
        3f to context.getString(R.string.mood_neutral),
        4f to context.getString(R.string.mood_happy)
    )

    fun configureChart(
        chart: BarChart,
        data: List<BarEntry>,
        context: Context) {
        val barDataSet = createBarDataSet(data)
        val barData = BarData(barDataSet)
        val moodLabels = createMoodLabels(context)

        chart.apply {
            setupBasicConfiguration(barData)
            setupXAxis(context)
            setupYAxis(moodLabels)
            setupAppearance()
            invalidate()
        }
    }

    private fun createBarDataSet(data: List<BarEntry>): BarDataSet {
        return BarDataSet(data, "").apply {
            color = Color.parseColor(BAR_COLOR_HEX)
            valueTextColor = Color.BLACK
            valueTextSize = LABEL_TEXT_SIZE_SP
            valueFormatter = ZeroValueFormatter()
            setDrawValues(false)
        }
    }

    private fun BarChart.setupBasicConfiguration(barData: BarData) {
        this.data = barData
        description.isEnabled = false
        animateY(ANIMATION_DURATION_MILLIS)
        setFitBars(true)
        setScaleEnabled(false)

        setExtraOffsets(EXTRA_OFFSET_DP, EXTRA_OFFSET_DP, EXTRA_OFFSET_DP, EXTRA_OFFSET_DP)
    }

    private fun BarChart.setupXAxis(context: Context) {
        xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(generateWeekDayLabels(context))
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            setDrawAxisLine(false)
            textSize = LABEL_TEXT_SIZE_SP
            labelCount = LABEL_COUNT_X
            granularity = VALUE_GRANULARITY_X
        }
    }

    private fun BarChart.setupYAxis(moodLabels: Map<Float, String>) {
        axisLeft.apply {
            valueFormatter = createMoodValueFormatter(moodLabels)
            axisMinimum = MIN_Y_AXIS
            axisMaximum = MAX_Y_AXIS
            granularity = GRANULARITY
            labelCount = LABEL_COUNT_Y

            textSize = 14f
            textColor = Color.parseColor(TEXT_COLOR_HEX)
        }

        axisRight.isEnabled = false
    }

    private fun BarChart.setupAppearance() {
        legend.isEnabled = false
    }

    private fun generateWeekDayLabels(context: Context): List<String> {
        val ruLocale = Locale.forLanguageTag("ru")
        val formatter = DateTimeFormatter.ofPattern("EEE", ruLocale)
        return (0..6).map {
            LocalDate.now().minusDays(6 - it.toLong()).format(formatter)
        }.toList()
    }

    private fun createMoodValueFormatter(moodLabels: Map<Float, String>): ValueFormatter {
        return object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return moodLabels[value.toInt().toFloat()] ?: ""
            }
        }
    }
}