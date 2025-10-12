package com.example.mood_diary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mood_diary.data.model.Entry
import com.example.mood_diary.data.model.Mood
import com.example.mood_diary.domain.EntryRepository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EntriesViewModel(
    private val repository: EntryRepository
) : ViewModel(), IntentAware<EntriesViewModel.ViewState.Intents> {

    data class ViewState(
        val isLoading: Boolean = false,
        val isError: Boolean = false,
        val entries: List<Entry> = emptyList(),
        val filteredEntries: List<Entry> = emptyList(),
        val searchQuery: String = "",
        val filterType: MoodFilterType = MoodFilterType.ALL,
    ) {

        enum class MoodFilterType {
            ALL, HAPPY, SAD, ANGRY, NEUTRAL
        }

        sealed class Intents {
            data object LoadEntries : Intents()
            data class SearchEntries(val query: String) : Intents()
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
        emit(loadInitialEntries())
        refreshListener.collect { trigger ->
            when (trigger) {
                ViewState.StateTriggers.LoadEntries -> {
                    emit(currentState.copy(isLoading = true))
                    emit(updateEntries())
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
                    emit(updateEntries())
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

    private suspend fun loadInitialEntries(): ViewState {
        return try {
            val entries = repository.getAllEntries().first()
            ViewState(
                isLoading = false,
                isError = false,
                entries = entries,
                filteredEntries = entries
            )
        } catch (e: Exception) {
            ViewState(isLoading = false, isError = true)
        }
    }

    private suspend fun updateEntries(): ViewState {
        return try {
            val entries = repository.getAllEntries().first()
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
            filteredEntries = result,
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
}