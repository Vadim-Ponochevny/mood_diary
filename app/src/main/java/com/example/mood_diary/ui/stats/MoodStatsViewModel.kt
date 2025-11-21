package com.example.mood_diary.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mood_diary.data.model.Entry
import com.example.mood_diary.data.model.Mood
import com.example.mood_diary.domain.EntryRepository
import com.example.mood_diary.ui.addEdit.AddEditEntryViewModel
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
) : ViewModel(), IntentAware<MoodStatsViewModel.Intents> {

    data class ViewState(
        val isLoading: Boolean = true,
        val entries: List<Entry> = emptyList(),
        val chartData: List<BarEntry> = emptyList(),
        val emotionStats: Map<Mood, Int> = emptyMap(),
        val error: String? = null
    )

    sealed class Intents {
        data object LoadStatistics: Intents()
    }

    private val _viewState = MutableStateFlow(ViewState())
    val viewState = _viewState.asStateFlow()


    override fun onIntent(intent: Intents) {
        when (intent) {
            is Intents.LoadStatistics -> loadStatistics()
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

    private fun processEntriesForChart(entries: List<Entry>): List<BarEntry> {
        val today = LocalDate.now()

        val dailyGroupedEntries = entries
            .filter { entry ->
                !entry.date.isBefore(today.minusDays(6))
            }
            .groupBy { it.date }

        val dailyAverageMood = dailyGroupedEntries.mapValues { (_, dailyEntries) ->
            dailyEntries.map { it.mood.value.toDouble() }.average()
        }

        return (0..6).map { i ->
            val date = today.minusDays(6 - i.toLong())

            val moodValue = dailyAverageMood[date]?.toFloat() ?: 0f

            BarEntry(i.toFloat(), moodValue)
        }
    }

    private fun processEntriesForEmotionStats(entries: List<Entry>): Map<Mood, Int> {
        return entries
            .groupBy { it.mood }
            .mapValues { (_, moodEntries) -> moodEntries.size }
    }
}
