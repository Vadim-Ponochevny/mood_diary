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
import com.example.mood_diary.databinding.FragmentEntriesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import com.example.mood_diary.ui.common.FilterAdapter
import com.example.mood_diary.ui.common.MoodFilterItem
import com.example.mood_diary.ui.entries.EntriesViewModel.ViewState.Intents

@AndroidEntryPoint
class EntriesFragment : Fragment(), MenuProvider {

    private var _binding: FragmentEntriesBinding? = null
    private val binding get() = _binding!!

    private val entriesAdapter = EntriesAdapter()
    private val moodFilterAdapter = FilterAdapter()

    private val viewModel: EntriesViewModel by viewModels()
    private var lastState: EntriesViewModel.ViewState? = null

    private lateinit var searchView: SearchView
    private lateinit var searchMenuItem: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    override fun onStart() {
        super.onStart()

        viewModel.onIntent(Intents.LoadEntries)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTitleForFragment()

        connectToMenuActivity()

        setEntriesRView()
        setMoodFilterRView()

        observeToViewModel()

        setupClicks()
    }

    private fun connectToMenuActivity() {
        (requireActivity() as MenuHost).addMenuProvider(
            this,
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.options_menu, menu)

        searchMenuItem = menu.findItem(R.id.searchV)
        searchView = searchMenuItem.actionView as SearchView

        val currentState = lastState

        val savedQuery = currentState!!.searchQuery
        val isExpanded = currentState.isSearchExpanded

        if (isExpanded) {
            searchMenuItem.expandActionView()
        }

        if (savedQuery.isNotEmpty()) {
            searchView.setQuery(savedQuery, false)
        }

        if (isExpanded) {
            searchView.requestFocus()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.onIntent(Intents.SearchEntries(newText ?: ""))
                return true
            }
        })

        searchView.setOnCloseListener {
            viewModel.onIntent(Intents.SetSearchExpanded(false))
            viewModel.onIntent(Intents.SearchEntries(""))
            false
        }

        searchMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                viewModel.onIntent(Intents.SetSearchExpanded(true))
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                viewModel.onIntent(Intents.SetSearchExpanded(false))
                viewModel.onIntent(Intents.SearchEntries(""))
                return true
            }
        })
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return false
    }

    private fun setTitleForFragment() {
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Мои записи"
    }

    private fun observeToViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.entriesState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }

    private fun updateUI(state: EntriesViewModel.ViewState) {
        lastState = state

        binding.swipeRefreshLayout.isRefreshing = state.isLoading

        if (state.isLoading) {
            binding.emojiRecyclerView.visibility = View.GONE
        } else {
            binding.emojiRecyclerView.visibility = View.VISIBLE
        }

        if (state.isError) {
            Toast.makeText(requireContext(), "Error: Ошибка при загрузке данных", Toast.LENGTH_SHORT).show()
        }

        showMoodsForFilter(state.moods)
        showEntries(state.filteredEntries)
    }

    private fun showEntries(entries: List<Entry>) {
        entriesAdapter.submitList(entries)
    }

    private fun showMoodsForFilter(moods: List<MoodFilterItem> ) {
        moodFilterAdapter.submitList(moods)
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

    private fun setupClicks() {
        val swipeHandler = SwipeToDeleteEntryCallback(entriesAdapter) { entry, position ->
            showDeleteDialog(entry, position)
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.emojiRecyclerView)

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.onIntent(Intents.LoadEntries)
        }

        moodFilterAdapter.onMoodClick = { mood ->
            viewModel.onIntent(Intents.OnMoodClicked(mood))
        }

        binding.resetFilterButton.setOnClickListener {
            viewModel.onIntent(Intents.ClearFilters)
        }

        binding.addEntryButton.setOnClickListener {
            findNavController().navigate(R.id.addEditEntryFragment)
        }

        entriesAdapter.onEntryClick = { id ->
            val action = EntriesFragmentDirections.actionEntriesFragmentToEntryEditFragment(id)
            findNavController().navigate(action)
        }
    }

    private fun showDeleteDialog(entry: Entry, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление записи")
            .setMessage("Вы точно хотите удалить запись?")
            .setPositiveButton("Да") { _, _ ->
                viewModel.onIntent(Intents.DeleteEntry(entry))
            }
            .setNegativeButton("Нет") { dialog, _ ->
                entriesAdapter.notifyItemChanged(position)

                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}