package com.example.mood_diary.ui.addEdit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mood_diary.data.model.Entry
import com.example.mood_diary.data.model.Mood
import com.example.mood_diary.domain.EntryRepository
import com.example.mood_diary.ui.base.IntentAware
import com.example.mood_diary.ui.common.MoodFilterItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import javax.inject.Inject

@HiltViewModel
class AddEditEntryViewModel @Inject constructor(
    private val repository: EntryRepository
) : ViewModel(), IntentAware<AddEditEntryViewModel.Intents> {

    data class ViewState(
        val entryId: Int = 0,
        val description: String = "",
        val tag: String = "",
        val selectedMood: Mood = Mood.NEUTRAL,
        val date: LocalDate = LocalDate.now(),
        val time: LocalTime = LocalTime.now(),
        val isLoading: Boolean = false,
        val saveSuccessful: Boolean = false,
        val isError: Boolean = false,
        val moods: List<MoodFilterItem> = Mood.entries.map {
            MoodFilterItem(mood = it, isSelected = false)
        },
    )

    sealed class Intents {
        data class LoadEntry(val id: Int?) : Intents()
        data class UpdateDescription(val text: String) : Intents()
        data class UpdateTag(val text: String) : Intents()
        data class UpdateMood(val mood: Mood) : Intents()
        data object SaveEntry : Intents()
    }

    private val _viewState = MutableStateFlow(ViewState())
    val viewState = _viewState.asStateFlow()

    override fun onIntent(intent: Intents) {
        when (intent) {
            is Intents.LoadEntry -> loadEntry(intent.id)
            is Intents.UpdateDescription -> updateDescription(intent.text)
            is Intents.UpdateTag -> updateTag(intent.text)
            is Intents.UpdateMood -> updateMood(intent.mood)
            Intents.SaveEntry -> saveEntry()
        }
    }

    private fun loadEntry(id: Int?) {
        if (id == null) return

        viewModelScope.launch {

            _viewState.update {
                it.copy(isLoading = true)
            }

            val entry = repository.getEntryById(id)

            entry?.let { founded ->
                val updatedMoods = Mood.entries.map {
                    MoodFilterItem(it, isSelected = it == founded.mood)
                }

                _viewState.update {
                    it.copy(
                        entryId = founded.id,
                        description = founded.des,
                        tag = founded.teg,
                        selectedMood = founded.mood,
                        moods = updatedMoods,
                        isLoading = false
                    )
                }
            }

        }
    }

    private fun updateDescription(description: String) {
        _viewState.update {
            it.copy(
                description = description
            )
        }
    }

    private fun updateTag(tag: String) {
        _viewState.update {
            it.copy(
                tag = tag
            )
        }
    }

    private fun updateMood(mood: Mood) {
        _viewState.update { state ->
            val updatedMoods = state.moods.map {
                MoodFilterItem(it.mood, isSelected = it.mood == mood)
            }
            state.copy(
                selectedMood = mood,
                moods = updatedMoods
            )
        }
    }

    private fun saveEntry() {
        viewModelScope.launch {
            val state = _viewState.value
            if (state.tag.isBlank()) {
                // Здесь можно установить флаг ошибки, если описание пустое
                return@launch
            }

            val entryToSave = Entry(
                id = state.entryId,
                des = state.description,
                teg = state.tag,
                mood = state.selectedMood,
                date = LocalDate.now(), // Используем текущую дату
                time = LocalTime.now(),  // Используем текущее время
            )

            try {
                repository.upsertEntryDatabase(entryToSave)
                _viewState.update { it.copy(saveSuccessful = true) }
            } catch (e: Exception) {
                _viewState.update { it.copy(isError = true) }
            }
        }
    }
}