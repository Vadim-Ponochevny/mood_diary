package com.example.mood_diary.ui.stats

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
import com.example.mood_diary.domain.model.Mood
import com.example.mood_diary.databinding.FragmentMoodStatsBinding
import com.example.mood_diary.ui.stats.components.formatters.DateRangeFormatter
import com.example.mood_diary.ui.stats.components.formatters.EmotionStatsFormatter
import com.example.mood_diary.ui.stats.components.chart.MoodChartConfigurator
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class MoodStatsFragment : Fragment() {
    private val viewModel: MoodStatsViewModel by viewModels()
    private var _binding: FragmentMoodStatsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMoodStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitleForFragment()
        observeViewState()
        loadStatistics()
    }

    private fun setTitleForFragment() {
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Статистика"
    }

    private fun loadStatistics() {
        viewModel.onIntent(MoodStatsViewModel.MoodStatsIntents.LoadStatistics)
    }

    private fun observeViewState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewState.collect { state ->
                    handleState(state)
                }
            }
        }
    }

    private fun handleState(state: MoodStatsState) {
        when {
            state.isLoading -> showLoading()
            state.error != null -> showError(state.error)
            else -> showData(state)
        }
    }

    private fun showLoading() {
        binding.emotionStatsTextView.text = "Загрузка статистики..."
    }

    private fun showError(errorMsg: String) {
        binding.emotionStatsTextView.text = errorMsg
        binding.barChart.clear()
    }

    private fun showData(state: MoodStatsState) {
        displayDateRange()
        setupChart(state.chartData)
        displayEmotionStats(state.emotionStats)
    }

    private fun displayDateRange() {
        binding.dateRangeTextView.text = DateRangeFormatter.getDateRangeText(Locale.forLanguageTag("ru"))
    }

    private fun setupChart(data: List<BarEntry>) {
        MoodChartConfigurator.configureChart(binding.barChart, data, requireContext())
    }

    private fun displayEmotionStats(stats: Map<Mood, Int>) {
        binding.emotionStatsTextView.text = EmotionStatsFormatter.format(stats, requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
