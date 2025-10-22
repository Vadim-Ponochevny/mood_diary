package com.example.mood_diary.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mood_diary.data.model.Entry
import com.example.mood_diary.data.model.Mood
import com.example.mood_diary.domain.EntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

@HiltViewModel
class EntriesViewModel @Inject constructor(
    private val repository: EntryRepository
) : ViewModel(), IntentAware<EntriesViewModel.ViewState.Intents> {

    init {
        viewModelScope.launch {
            insertTestEntriesOnce()
        }
    }

    private suspend fun insertTestEntriesOnce() {
        val current = repository.getAllEntries().firstOrNull()
        if (current.isNullOrEmpty()) {
            val test1 = Entry(
                    id = 0,
                    des = "Тестовая запись 1",
                    teg = "тег1",
                    mood = Mood.HAPPY,
                    date = LocalDate.now(),
                    time = LocalTime.now()
                )
            val test2 = Entry(
                id = 0,
                des = "Тестовая запись 2",
                teg = "тег2",
                mood = Mood.NEUTRAL,
                date = LocalDate.now(),
                time = LocalTime.now()
            )
            repository.upsertEntryDatabase(test1)
            repository.upsertEntryDatabase(test2)
        }
    }

    data class ViewState(
        val isLoading: Boolean = false,
        val isError: Boolean = false,
        val entries: List<Entry> = emptyList(),
//        val filteredEntries: List<Entry> = emptyList(),
        val searchQuery: String = "",
        val moods: List<Mood> = Mood.entries,
        val filterType: MoodFilterType = MoodFilterType.ALL,
    ) {

        enum class MoodFilterType {
            ALL, HAPPY, SAD, ANGRY, NEUTRAL
        }

        sealed class Intents {
            data object LoadEntries : Intents()
            data class SearchEntries(val query: String) : Intents()
            data class OnMoodClicked(val mood: Mood) : Intents()
            data class FilterByMood(val filterType: MoodFilterType) : Intents()
            data class DeleteEntry(val entry: Entry) : Intents()
            data object ClearFilters : Intents()
        }

        sealed class StateTriggers {
            data object LoadEntries : StateTriggers()
            data class SearchChanged(val query: String) : StateTriggers()
            data class FilterChanged(val filterType: MoodFilterType) : StateTriggers()
            data class DeleteEntry(val entry: Entry) : StateTriggers()
            data object ClearFilters : StateTriggers()
        }
    }

    private var currentState = ViewState(isLoading = true, isError = false)
    private val refreshListener = MutableSharedFlow<ViewState.StateTriggers>()

    val entriesState: Flow<ViewState> = flow {
        emit(updateCurrentEntries())
        refreshListener.collect { trigger ->
            when (trigger) {
                ViewState.StateTriggers.LoadEntries -> {
                    emit(currentState.copy(isLoading = true))
                    emit(updateCurrentEntries())
                }
                is ViewState.StateTriggers.SearchChanged -> {
                    val newState = currentState.copy(searchQuery = trigger.query)
                    emit(applyFiltersAndSearch(newState))
                }
                is ViewState.StateTriggers.FilterChanged -> {
                    val newState = currentState.copy(filterType = trigger.filterType)
                    emit(applyFiltersAndSearch(newState))
                }
                is ViewState.StateTriggers.DeleteEntry -> {
                    deleteEntry(trigger.entry)
                    emit(updateCurrentEntries())
                }
                ViewState.StateTriggers.ClearFilters -> {
                    val clearedState = currentState.copy(
                        filterType = ViewState.MoodFilterType.ALL
                    )
                    emit(applyFiltersAndSearch(clearedState))
                }
            }
        }
    }
        .distinctUntilChanged()
        .onEach { currentState = it }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            currentState
        )

//    private suspend fun loadInitialEntries(): ViewState {
//        return try {
//            val entries = repository.getAllEntries().first()
//            ViewState(
//                isLoading = false,
//                isError = false,
//                entries = entries,
//                filteredEntries = entries
//            )
//        } catch (e: Exception) {
//            ViewState(isLoading = false, isError = true)
//        }
//    }

    private suspend fun updateCurrentEntries(): ViewState {
        return try {
            val entries = repository.getAllEntries().first()
            Log.d("entriesState", "$entries")
            currentState.copy(
                isLoading = false,
                isError = false,
                entries = entries
            ).let { applyFiltersAndSearch(it) }
        } catch (e: Exception) {
            currentState.copy(isLoading = false, isError = true)
        }
    }

    private suspend fun deleteEntry(entry: Entry) {
        try {
            repository.deleteEntryDatabase(entry)
        } catch (e: Exception) {
        }
    }

    private fun applyFiltersAndSearch(state: ViewState): ViewState {
        var result = state.entries

        if (state.searchQuery.isNotBlank()) {
            result = result.filter { entry ->
                entry.des.contains(state.searchQuery, ignoreCase = true) ||
                        entry.teg.contains(state.searchQuery, ignoreCase = true)
            }
        }

        if (state.filterType != ViewState.MoodFilterType.ALL) {
            val targetMood = when (state.filterType) {
                ViewState.MoodFilterType.HAPPY -> Mood.HAPPY
                ViewState.MoodFilterType.SAD -> Mood.SAD
                ViewState.MoodFilterType.ANGRY -> Mood.ANGRY
                ViewState.MoodFilterType.NEUTRAL -> Mood.NEUTRAL
                ViewState.MoodFilterType.ALL -> null
            }
            result = result.filter { it.mood == targetMood }
        }

        result = result.sortedByDescending { entry ->
            entry.date.atTime(entry.time)
        }

        return state.copy(
            entries = result,
            isLoading = false,
            isError = false
        )
    }

    override fun onIntent(intent: ViewState.Intents) {
        when (intent) {
            ViewState.Intents.LoadEntries -> {
                viewModelScope.launch {
                    refreshListener.emit(ViewState.StateTriggers.LoadEntries)
                }
            }

            is ViewState.Intents.SearchEntries -> {
                viewModelScope.launch {
                    refreshListener.emit(ViewState.StateTriggers.SearchChanged(intent.query))
                }
            }

            is ViewState.Intents.OnMoodClicked -> handleMoodClicked(intent.mood)

            is ViewState.Intents.FilterByMood -> {
                viewModelScope.launch {
                    refreshListener.emit(ViewState.StateTriggers.FilterChanged(intent.filterType))
                }
            }

            is ViewState.Intents.DeleteEntry -> {
                viewModelScope.launch {
                    refreshListener.emit(ViewState.StateTriggers.DeleteEntry(intent.entry))
                }
            }

            ViewState.Intents.ClearFilters -> {
                viewModelScope.launch {
                    refreshListener.emit(ViewState.StateTriggers.ClearFilters)
                }
            }

        }
    }

    private fun handleMoodClicked(mood: Mood) {
        val filterType = when (mood) {
            Mood.HAPPY -> ViewState.MoodFilterType.HAPPY
            Mood.SAD -> ViewState.MoodFilterType.SAD
            Mood.ANGRY -> ViewState.MoodFilterType.ANGRY
            Mood.NEUTRAL -> ViewState.MoodFilterType.NEUTRAL
        }

        onIntent(ViewState.Intents.FilterByMood(filterType))
    }
}