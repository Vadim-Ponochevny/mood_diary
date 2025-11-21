package com.example.mood_diary.ui.stats

import android.graphics.Color
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
import com.example.mood_diary.data.model.Mood
import com.example.mood_diary.databinding.FragmentMoodStatsBinding
import com.example.mood_diary.ui.stats.MoodStatsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class MoodStatsFragment : Fragment() {

    private var _binding: FragmentMoodStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MoodStatsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Статистика"

        observeViewState()

        // Фрагмент отправляет интент во ViewModel
        viewModel.onIntent(MoodStatsViewModel.Intents.LoadStatistics)
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

    private fun handleState(state: MoodStatsViewModel.ViewState) {

        if (state.isLoading) {
            binding.emotionStatsTextView.text = "Загрузка статистики..."
            return
        }

        state.error?.let { errorMsg ->
            binding.emotionStatsTextView.text = errorMsg
            binding.barChart.clear()
            return
        }

        // success branch
        displayDateRange()
        setupChart(state.chartData)
        displayEmotionStats(state.emotionStats)
    }

    private fun displayDateRange() {
        val today = LocalDate.now()
        val startDate = today.minusDays(6)
        val dateFormatter = DateTimeFormatter.ofPattern("d MMM", Locale("ru"))

        val dateRangeText = "Статистика: ${startDate.format(dateFormatter)} – ${today.format(dateFormatter)}"
        binding.dateRangeTextView.text = dateRangeText
    }

    private fun setupChart(data: List<BarEntry>) {
        // Карта для форматирования оси Y: значение (float) -> подпись (String)
        // ОБНОВЛЕНО: Используем только 4 уровня, как в вашем enum Mood (1-4)
        val moodLabels = mapOf(
            1f to "Скучаю/Грустно", // Соответствует SAD (1)
            2f to "Нейтрально",        // Соответствует NEUTRAL (2)
            3f to "Счастлив/Хорошо",   // Соответствует HAPPY (3)
            4f to "Злость/Агрессия"   // Соответствует ANGRY (4)
        )

        // --- 1. Набор данных (DataSet) ---
        val barDataSet = BarDataSet(data, "Динамика настроения").apply {
            color = Color.parseColor("#4B0082") // Индиго
            valueTextColor = Color.BLACK
            valueTextSize = 12f
            // Скрываем подписи значений, если значение 0 (нет данных)
            valueFormatter = object : ValueFormatter() {
                override fun getBarLabel(barEntry: BarEntry?): String {
                    return if (barEntry?.y == 0f) "" else super.getBarLabel(barEntry)
                }
            }
        }

        val barData = BarData(barDataSet)
        barData.barWidth = 0.5f // Делаем столбцы чуть уже

        // --- 2. Настройка графика ---
        binding.barChart.apply {
            this.data = barData
            description.isEnabled = false
            animateY(800)

            // --- Ось X: Динамические дни недели ---
            val today = LocalDate.now()
            val dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale("ru"))

            val dynamicDaysLabels = (0..6).map { i ->
                val date = today.minusDays(6 - i.toLong())
                date.format(dayFormatter)
            }

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(dynamicDaysLabels)
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                labelCount = 7
                granularity = 1f
            }

            // --- Ось Y (левая): Форматирование настроения ---
            axisLeft.apply {
                // Используем наш форматировщик для перевода 1..4 в текст
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        // Используем .roundToInt() для корректного поиска, если значение не целое (среднее)
                        return moodLabels[value.toInt().toFloat()] ?: ""
                    }
                }
                axisMinimum = 0.5f  // Начинаем чуть ниже 1
                // ИСПРАВЛЕНО: Максимальное значение для 4 уровней
                axisMaximum = 4.5f
                granularity = 1f
                // ИСПРАВЛЕНО: Количество меток - 4
                labelCount = 4
                setDrawGridLines(true)
            }

            // Отключаем правую ось и легенду
            axisRight.isEnabled = false
            legend.isEnabled = false

            setFitBars(true)
            setScaleEnabled(false)
        }

        binding.barChart.invalidate()
    }

    private fun displayEmotionStats(stats: Map<Mood, Int>) {
        // Пример отображения статистики (требует TextView или RecyclerView)
        val sb = StringBuilder("Статистика эмоций за период:\n")
        stats.entries.sortedByDescending { it.value }.forEach { (mood, count) ->
            sb.append("${mood.name.lowercase().replaceFirstChar { it.titlecase() }}: $count запис(ей)\n")
        }
        binding.emotionStatsTextView.text = sb.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
