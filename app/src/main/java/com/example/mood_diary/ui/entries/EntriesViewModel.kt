package com.example.mood_diary.ui.entries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mood_diary.data.model.Entry
import com.example.mood_diary.data.model.Mood
import com.example.mood_diary.domain.EntryRepository
import com.example.mood_diary.ui.base.IntentAware
import com.example.mood_diary.ui.common.MoodFilterItem
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class EntriesViewModel @Inject constructor(
    private val repository: EntryRepository
) : ViewModel(), IntentAware<EntriesViewModel.ViewState.Intents> {

    data class ViewState(
        val isLoading: Boolean = false,
        val isError: Boolean = false,
        val entries: List<Entry> = emptyList(),
        val filteredEntries: List<Entry> = emptyList(),
        val searchQuery: String = "",
        val isSearchExpanded: Boolean = false,
        val moods: List<MoodFilterItem> = Mood.entries.map {
            MoodFilterItem(mood = it, isSelected = false)
        },
        val filterMood: Mood? = null,
    ) {

        sealed class Intents {
            data object LoadEntries : Intents()
            data class SearchEntries(val query: String) : Intents()
            data class SetSearchExpanded(val open: Boolean) : Intents()
            data class OnMoodClicked(val mood: Mood) : Intents()
            data class FilterByMood(val filterMood: Mood) : Intents()
            data class DeleteEntry(val entry: Entry) : Intents()
            data object ClearFilters : Intents()
        }

        sealed class StateTriggers {
            data object LoadEntries : StateTriggers()
            data class SearchChanged(val query: String) : StateTriggers()
            data class SetSearchExpanded(val open: Boolean) : StateTriggers()
            data class FilterChanged(val filterMood: Mood) : StateTriggers()
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
                is ViewState.StateTriggers.SetSearchExpanded -> {
                    val newState = currentState.copy(isSearchExpanded = trigger.open)
                    emit(applyFiltersAndSearch(newState))
                }
                is ViewState.StateTriggers.FilterChanged -> {
                    val newState = currentState.copy(filterMood = trigger.filterMood)
                    emit(applyFiltersAndSearch(newState))
                }
                is ViewState.StateTriggers.DeleteEntry -> {
                    deleteEntry(trigger.entry)
                    emit(updateCurrentEntries())
                }
                ViewState.StateTriggers.ClearFilters -> {
                    val clearedState = currentState.copy(
                        filterMood = null
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

    private suspend fun updateCurrentEntries(): ViewState {
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

        state.filterMood?.let { mood ->
            result = result.filter { it.mood == mood }
        }

        val moodsForFilter = Mood.entries.map { mood ->
            MoodFilterItem(
                mood = mood,
                isSelected = mood == state.filterMood
            )
        }

        return state.copy(
            filteredEntries = result,
            moods = moodsForFilter,
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

            is ViewState.Intents.SetSearchExpanded -> {
                viewModelScope.launch {
                    refreshListener.emit(ViewState.StateTriggers.SetSearchExpanded(intent.open))
                }
            }

            is ViewState.Intents.OnMoodClicked -> {
                val currentFilter = currentState.filterMood

                if (currentFilter == intent.mood) {
                    onIntent(ViewState.Intents.ClearFilters)
                } else {
                    onIntent(ViewState.Intents.FilterByMood(intent.mood))
                }
            }

            is ViewState.Intents.FilterByMood -> {
                viewModelScope.launch {
                    refreshListener.emit(ViewState.StateTriggers.FilterChanged(intent.filterMood))
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