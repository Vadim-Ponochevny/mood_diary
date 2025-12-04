package com.example.mood_diary.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mood_diary.domain.model.DomainEntry
import com.example.mood_diary.domain.model.Mood
import com.example.mood_diary.domain.repository.EntryRepository
import com.example.mood_diary.ui.base.IntentAware
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import javax.inject.Inject

@HiltViewModel
class MoodStatsViewModel @Inject constructor(
    private val entryRepository: EntryRepository
) : ViewModel(), IntentAware<MoodStatsViewModel.MoodStatsIntents> {

    private companion object {
        private const val STATS_RANGE_DAYS = 7
        private const val DAYS_OFFSET_FROM_TODAY = STATS_RANGE_DAYS - 1
        private const val CHART_START_INDEX = 0
        private const val CHART_END_INDEX = DAYS_OFFSET_FROM_TODAY
        private const val DEFAULT_MOOD_VALUE_FLOAT = 0f
    }

    sealed class MoodStatsIntents {
        data object LoadStatistics: MoodStatsIntents()
    }

    private val _viewState = MutableStateFlow(MoodStatsState())
    val viewState = _viewState.asStateFlow()

    override fun onIntent(intent: MoodStatsIntents) {
        when (intent) {
            is MoodStatsIntents.LoadStatistics -> loadStatistics()
        }
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            _viewState.update {
                it.copy(
                    isLoading = true
                )
            }

            entryRepository.getAllEntries().collect { entriesFromFlow ->

                val chartData = processEntriesForChart(entriesFromFlow)
                val emotionStats = processEntriesForEmotionStats(entriesFromFlow)

                _viewState.update {
                    it.copy(
                        isLoading = false,
                        entries = entriesFromFlow,
                        chartData = chartData,
                        emotionStats = emotionStats,
                        error = if (entriesFromFlow.isEmpty()) "Записей для статистики пока нет" else null
                    )
                }
            }
        }
    }

    private fun processEntriesForChart(entries: List<DomainEntry>): List<BarEntry> {

        val today = LocalDate.now()
        val startOfChartRange = today.minusDays(DAYS_OFFSET_FROM_TODAY.toLong())

        val recentEntries = entries.filter { entry ->
            val entryDate = LocalDate.parse(entry.date)

            !entryDate.isBefore(startOfChartRange)
        }

        val groupedByDate = recentEntries.groupBy { entry ->
            LocalDate.parse(entry.date)
        }

        val dailyAverageMood = groupedByDate.mapValues { (_, entriesForDay) ->
            entriesForDay
                .map { it.mood.value.toDouble() }
                .average()
        }

        return (CHART_START_INDEX..CHART_END_INDEX).map { dayIndex ->

            val chartDate = startOfChartRange.plusDays(dayIndex.toLong())

            val averageMoodValue = dailyAverageMood[chartDate]?.toFloat() ?: DEFAULT_MOOD_VALUE_FLOAT

            BarEntry(dayIndex.toFloat(), averageMoodValue)
        }
    }

    private fun processEntriesForEmotionStats(entries: List<DomainEntry>): Map<Mood, Int> {
        return entries
            .groupBy { it.mood }
            .mapValues { (_, moodEntries) -> moodEntries.size }
    }
}
