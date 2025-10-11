package com.example.mood_diary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mood_diary.data.model.Entry
import com.example.mood_diary.domain.EntryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class EntriesViewModel(
    private val repository: EntryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EntriesUiState>(EntriesUiState.Success(emptyList()))
    val uiState: StateFlow<EntriesUiState> = _uiState

    init {
        viewModelScope.launch {
            repository.getAllEntries()
                .catch { e ->
                    _uiState.value = EntriesUiState.Error(e)

                }
                .collect { allEntries ->
                    _uiState.value = EntriesUiState.Success(allEntries)
                }
        }
    }
}

sealed class EntriesUiState {
    data class Success(val entries: List<Entry>): EntriesUiState()
    data class Error(val exception: Throwable): EntriesUiState()
}