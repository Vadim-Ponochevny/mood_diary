package com.example.mood_diary.ui.addEdit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mood_diary.data.model.Mood
import com.example.mood_diary.databinding.FragmentEntryEditBinding
import com.example.mood_diary.ui.common.FilterAdapter
import com.example.mood_diary.ui.entries.EntriesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class AddEditEntryFragment : Fragment() {

    private val args: AddEditEntryFragmentArgs by navArgs()

    private var _binding: FragmentEntryEditBinding? = null
    private val binding get() = _binding!!

    private val moodFilterAdapter = FilterAdapter()

    private val viewModel: AddEditEntryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEntryEditBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val entryId = args.entryId

        if (entryId != 0) {
            (requireActivity() as AppCompatActivity).supportActionBar?.title = "Редактировать запись"
            viewModel.onIntent(AddEditEntryViewModel.Intents.LoadEntry(entryId))

        } else {
            (requireActivity() as AppCompatActivity).supportActionBar?.title = "Добавить запись"
        }

        observeViewState()

        setupListeners()
        setupMoodSelector()
    }

    private fun observeViewState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewState.collect { state ->
                    render(state)
                }
            }
        }
    }

    /**
     * Отрисовывает состояние ViewState на UI.
     */
    private fun render(state: AddEditEntryViewModel.ViewState) {
        // Установка данных в поля ввода
        binding.descriptionEditText.setText(state.description)
        binding.tagEditText.setText(state.tag)

        // TODO: Обновить UI для выбранного настроения (state.selectedMood)

        // Обработка загрузки
        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        binding.saveButton.isEnabled = !state.isLoading

        // Обработка успешного сохранения
        if (state.saveSuccessful) {
            findNavController().popBackStack() // Возвращаемся на предыдущий экран
        }

        // TODO: Обработка ошибки (state.isError)
    }

    private fun setupListeners() {
        // Слушатель для кнопки сохранения
        binding.saveButton.setOnClickListener {
            viewModel.onIntent(AddEditEntryViewModel.Intents.SaveEntry)
        }

        moodFilterAdapter.onMoodClick = { mood ->
            viewModel.onIntent(AddEditEntryViewModel.Intents.UpdateMood(mood))
        }

        // Слушатели для ввода текста (один из способов, нужно использовать TextWatcher)
        // Пример для описания:
        // binding.editTextDescription.addTextChangedListener { text ->
        //     viewModel.onIntent(AddEditEntryViewModel.ViewState.Intents.UpdateDescription(text.toString()))
        // }

        // TODO: Добавить слушатели для изменения текста и настроения
    }

    private fun setupMoodSelector() {
        binding.moodSelectedRecyclerView.setHasFixedSize(true)
        binding.moodSelectedRecyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.moodSelectedRecyclerView.adapter = moodFilterAdapter

//        moodFilterAdapter.submitList(Mood.values().toList())
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}