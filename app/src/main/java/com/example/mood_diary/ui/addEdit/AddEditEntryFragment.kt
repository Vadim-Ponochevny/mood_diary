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
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mood_diary.databinding.FragmentEntryEditBinding
import com.example.mood_diary.ui.common.FilterAdapter
import com.example.mood_diary.ui.common.MoodFilterItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue
import com.example.mood_diary.R
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import org.threeten.bp.LocalTime
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.TextStyle
import java.util.Locale

@AndroidEntryPoint
class AddEditEntryFragment : Fragment(), AdapterView.OnItemSelectedListener {

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
        setSpinner()
        setMaterialPickers()
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

    @SuppressLint("DefaultLocale")
    private fun render(state: AddEditEntryViewModel.ViewState) {
        showMoodSelector(state.moods)

        if (binding.descriptionEditText.text.toString() != state.description) {
            binding.descriptionEditText.setText(state.description)
        }

        if (binding.dateEditText.text.toString() != state.date?.toString()) {
            val dateToSee = state.date?.let { date ->
                val monthName = date.month.getDisplayName(TextStyle.FULL, Locale("ru"))
                "${date.dayOfMonth} $monthName ${date.year} г."
            } ?: ""

            binding.dateEditText.setText(dateToSee)
        }

        val formattedTime = state.time?.let { time ->
            String.format("%02d:%02d", time.hour, time.minute)
        } ?: ""

        if (binding.timeEditText.text.toString() != formattedTime) {
            binding.timeEditText.setText(formattedTime)
        }

        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        binding.saveButton.isEnabled = !state.isLoading

        if (state.saveSuccessful) {
            findNavController().popBackStack()
        }

        if (state.isError) {
            Toast.makeText(requireContext(), state.erorrDescrip, Toast.LENGTH_SHORT).show()
        }

    }

    private fun setupListeners() {
        binding.saveButton.setOnClickListener {
            viewModel.onIntent(AddEditEntryViewModel.Intents.SaveEntry)
        }

        moodFilterAdapter.onMoodClick = { mood ->
            viewModel.onIntent(AddEditEntryViewModel.Intents.UpdateMood(mood))
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

    }

    private fun setSpinner() {

        val spinner: Spinner = binding.tagSpinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.tags_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
        spinner.onItemSelectedListener = this

    }

    private fun setupMoodSelector() {
        binding.moodSelectedRecyclerView.setHasFixedSize(true)
        binding.moodSelectedRecyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.moodSelectedRecyclerView.adapter = moodFilterAdapter
    }

    private fun showMoodSelector(moods: List<MoodFilterItem>) {
        moodFilterAdapter.submitList(moods)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val selectedTag = p0?.getItemAtPosition(p2).toString()
        viewModel.onIntent(AddEditEntryViewModel.Intents.UpdateTag(selectedTag))
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    private fun setMaterialPickers() {
        binding.dateEditText.setOnClickListener {
            val datePicker = MaterialDatePicker
                .Builder
                .datePicker()
                .setTitleText("Выберите дату")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.addOnPositiveButtonClickListener { millis ->
                val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                viewModel.onIntent(AddEditEntryViewModel.Intents.UpdateDate(date))
            }

            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }

        binding.timeEditText.setOnClickListener {
            val timePicker = MaterialTimePicker
                .Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText("Выберите время")
                .build()

            timePicker.addOnPositiveButtonClickListener {
                val time = LocalTime.of(timePicker.hour, timePicker.minute)
                viewModel.onIntent(AddEditEntryViewModel.Intents.UpdateTime(time))
            }

            timePicker.show(parentFragmentManager, "TIME_PICKER")
        }
    }

}