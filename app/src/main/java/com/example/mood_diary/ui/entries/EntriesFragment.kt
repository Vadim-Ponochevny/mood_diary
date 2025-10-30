package com.example.mood_diary.ui.entries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mood_diary.R
import com.example.mood_diary.data.model.Entry
import com.example.mood_diary.data.model.Mood
import com.example.mood_diary.databinding.FragmentEntriesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import com.example.mood_diary.ui.addEdit.AddEditEntryViewModel
import com.example.mood_diary.ui.common.FilterAdapter


@AndroidEntryPoint
class EntriesFragment : Fragment(), MenuProvider {

    private var _binding: FragmentEntriesBinding? = null
    private val binding get() = _binding!!

    private val entriesAdapter = EntriesAdapter()
    private val moodFilterAdapter = FilterAdapter()

    private val entriesViewModel: EntriesViewModel by viewModels()
    private val addEditEntryViewModel: AddEditEntryViewModel by viewModels()

    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.options_menu, menu)

        val searchItem = menu.findItem(R.id.search)
        searchView = searchItem?.actionView as SearchView

        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                entriesViewModel.onIntent(EntriesViewModel.ViewState.Intents.SearchEntries(newText))
                return true
            }
        })

    }

    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)

    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEntriesBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Мои записи"

        setEntriesRView()
        setMoodFilterRView()


        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                entriesViewModel.entriesState.collect { state ->
                    updateUI(state)

                }
            }
        }

        setupClicks()
    }

    private fun updateUI(state: EntriesViewModel.ViewState) {

        binding.swipeRefreshLayout.isRefreshing = state.isLoading

        if (state.isLoading) {
            binding.emojiRecyclerView.visibility = View.GONE
        } else {
            binding.emojiRecyclerView.visibility = View.VISIBLE
        }

        if (state.isError) {
            Toast.makeText(requireContext(), "Error: Ошибка при загрузке данных", Toast.LENGTH_SHORT).show()
        }

        // Адаптер поучает из стэйта эмоцию для фильтра
        moodFilterAdapter.selectedMood = state.filterMood

        showMoodsForFilter(state.moods)
        showEntries(state.filteredEntries)
    }

    private fun setEntriesRView() {
        binding.emojiRecyclerView.setHasFixedSize(true)
        binding.emojiRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.emojiRecyclerView.adapter = entriesAdapter
    }

    private fun setMoodFilterRView() {
        binding.moodFilterRecyclerView.setHasFixedSize(true)
        binding.moodFilterRecyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.moodFilterRecyclerView.adapter = moodFilterAdapter
    }

    private fun showEntries(entries: List<Entry>) {
        entriesAdapter.submitList(entries)
    }

    private fun showMoodsForFilter(moods: List<Mood> ) {
        moodFilterAdapter.submitList(moods)
    }

    private fun setupClicks() {

        val swipeHandler = SwipeToDeleteEntryCallback(entriesAdapter) { entry, position ->
            showDeleteDialog(entry, position)
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.emojiRecyclerView)

        binding.swipeRefreshLayout.setOnRefreshListener {
            entriesViewModel.onIntent(EntriesViewModel.ViewState.Intents.LoadEntries)
        }

        moodFilterAdapter.onMoodClick = { mood ->
            entriesViewModel.onIntent(EntriesViewModel.ViewState.Intents.FilterByMood(mood))
        }

        binding.resetFilterButton.setOnClickListener {
            entriesViewModel.onIntent(EntriesViewModel.ViewState.Intents.ClearFilters)
        }

        binding.addEntryButton.setOnClickListener {
            findNavController().navigate(R.id.entryEditFragment)
        }

        // Переход к другому фрагменту
        entriesAdapter.onEntryClick = { id ->
            addEditEntryViewModel.viewState.value.entryId = id
        }
    }

    private fun showDeleteDialog(entry: Entry, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление записи")
            .setMessage("Вы точно хотите удалить запись?")
            .setPositiveButton("Да") { _, _ ->
                entriesViewModel.onIntent(EntriesViewModel.ViewState.Intents.DeleteEntry(entry))
            }
            .setNegativeButton("Нет") { dialog, _ ->
                entriesAdapter.notifyItemChanged(position)

                dialog.dismiss()
            }
            .show()
    }

}