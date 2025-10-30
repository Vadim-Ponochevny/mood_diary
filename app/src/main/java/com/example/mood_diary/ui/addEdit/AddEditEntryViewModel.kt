package com.example.mood_diary.ui.addEdit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mood_diary.data.model.Entry
import com.example.mood_diary.data.model.Mood
import com.example.mood_diary.domain.EntryRepository
import com.example.mood_diary.ui.base.IntentAware
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
        val entryId: Int? = null,
        val description: String = "",
        val tag: String = "",
        val selectedMood: Mood = Mood.NEUTRAL,
        val date: LocalDate = LocalDate.now(),
        val time: LocalTime = LocalTime.now(),
        val isLoading: Boolean = false,
        val saveSuccessful: Boolean = false,
        val isError: Boolean = false
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

            _viewState.update {
                it.copy(
                    entryId = entry.id,
                    description = entry.des,
                    tag = entry.teg,
                    selectedMood = entry.mood,
                    isLoading = false
                )
            }
        }
    }

    private fun updateDescription(text: String) {
        _viewState.update { it.copy(description = text) }
    }

    private fun updateTag(text: String) {
        _viewState.update { it.copy(tag = text) }
    }

    private fun updateMood(mood: Mood) {
        _viewState.update { it.copy(selectedMood = mood) }
    }

    private fun saveEntry() {
        viewModelScope.launch {
            val state = _viewState.value
            if (state.description.isBlank()) {
                // Здесь можно установить флаг ошибки, если описание пустое
                return@launch
            }

            // Создание новой или обновление существующей записи
            val entryToSave = Entry(
                id = state.entryId ?: 0, // 0 для новой записи (автоинкремент)
                des = state.description,
                teg = state.tag,
                mood = state.selectedMood,
                date = LocalDate.now(), // Используем текущую дату
                time = LocalTime.now()  // Используем текущее время
            )

            try {
                repository.upsertEntryDatabase(entryToSave)
                // Устанавливаем флаг успешного сохранения для навигации
                _viewState.update { it.copy(saveSuccessful = true) }
            } catch (e: Exception) {
                _viewState.update { it.copy(isError = true) }
            }
        }
    }
}