package com.example.mood_diary.ui.addEdit

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mood_diary.databinding.FragmentEntryEditBinding
import com.example.mood_diary.ui.common.FilterAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue
import com.example.mood_diary.R
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import org.threeten.bp.LocalTime
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.format.TextStyle
import java.util.Locale

@AndroidEntryPoint
class AddEditEntryFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private val args: AddEditEntryFragmentArgs by navArgs()
    private var _binding: FragmentEntryEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditEntryViewModel by viewModels()
    private val moodFilterAdapter = FilterAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEntryEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupObservers()
        setupUI()
    }

    private fun setupUI() {
        setupListeners()
        setupMoodSelector()
        setupTagSpinner()
        setupMaterialPickers()
    }

    private fun setupToolbar() {
        val title = if (args.entryId != 0) {
            viewModel.onIntent(AddEditEntryViewModel.Intents.LoadEntry(args.entryId))
            "Редактировать запись"
        } else {
            "Добавить запись"
        }
        (requireActivity() as AppCompatActivity).supportActionBar?.title = title
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateUI(state: AddEditEntryViewModel.ViewState) {

        moodFilterAdapter.submitList(state.moods)

        binding.tagSpinner.setSelection(getTagPosition(state.tag))

        updateTextIfChanged(binding.descriptionEditText, state.description)

        updateTextIfChanged(binding.dateEditText, state.date ?: "")
        updateTextIfChanged(binding.timeEditText, state.time ?: "")

        binding.progressBar.isVisible = state.isLoading
        binding.saveButton.isEnabled = !state.isLoading

        if (state.saveSuccessful) findNavController().popBackStack()

        if (state.isError) {
            Toast.makeText(requireContext(), state.erorrDescrip, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTextIfChanged(editText: EditText, value: String) {
        if (editText.text.toString() != value) {
            editText.setText(value)
        }
    }

    private fun formatDate(date: LocalDate): String {
        val month = date.month.getDisplayName(TextStyle.FULL, Locale("ru"))
        return "${date.dayOfMonth} $month ${date.year} г."
    }

    private fun formatTime(time: LocalTime): String =
        "%02d:%02d".format(time.hour, time.minute)


    private fun setupListeners() {
        binding.saveButton.setOnClickListener {
            viewModel.onIntent(AddEditEntryViewModel.Intents.SaveEntry)
        }

        binding.descriptionEditText.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}
                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                }

                override fun afterTextChanged(s: Editable?) {
                    viewModel.onIntent(AddEditEntryViewModel.Intents.UpdateDescription(s.toString()))
                }
            }
        )


        moodFilterAdapter.onMoodClick = {
            viewModel.onIntent(AddEditEntryViewModel.Intents.UpdateMood(it))
        }
    }

    private fun setupTagSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.tags_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.tagSpinner.adapter = adapter
        }

        binding.tagSpinner.onItemSelectedListener = this
    }

    private fun getTagPosition(tag: String): Int =
        resources.getStringArray(R.array.tags_array).indexOf(tag)

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        viewModel.onIntent(AddEditEntryViewModel.Intents.UpdateTag(parent!!.getItemAtPosition(pos).toString()))
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    private fun setupMoodSelector() {
        binding.moodSelectedRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = moodFilterAdapter
        }
    }

    private fun setupMaterialPickers() {

        binding.dateEditText.setOnClickListener {
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Выберите дату")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()
                .apply {
                    addOnPositiveButtonClickListener { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                        viewModel.onIntent(AddEditEntryViewModel.Intents.UpdateDate(date.toString()))
                    }
                }.show(parentFragmentManager, "DATE_PICKER")
        }

        binding.timeEditText.setOnClickListener {
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText("Выберите время")
                .build().apply {
                    addOnPositiveButtonClickListener {
                        val time = LocalTime.of(hour, minute)
                        viewModel.onIntent(AddEditEntryViewModel.Intents.UpdateTime(time.toString()))
                    }
                }.show(parentFragmentManager, "TIME_PICKER")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
