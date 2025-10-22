package com.example.mood_diary.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mood_diary.R
import com.example.mood_diary.data.model.Entry
import com.example.mood_diary.data.model.Mood
import com.example.mood_diary.databinding.FragmentEntriesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EntriesFragment : Fragment(), MenuProvider {

    private var _binding: FragmentEntriesBinding? = null
    private val binding get() = _binding!!

    private val entriesAdapter = EntriesAdapter()
    private val moodFilterAdapter = FilterAdapter()

    private val viewModel: EntriesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.options_menu, menu)

        val searchItem = menu.findItem(R.id.search)
        val searchView = searchItem.actionView as androidx.appcompat.widget.SearchView

        searchView.setOnQueryTextListener(
            object : androidx.appcompat.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                viewModel.onIntent(EntriesViewModel.ViewState.Intents.SearchEntries(newText))
                return true
            }
        })

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

        setEntriesRView()
        setMoodFilterRView()


        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.entriesState.collect { state ->
                    updateUI(state)
                }
            }
        }

        setupClicks()
    }

    private fun updateUI(state: EntriesViewModel.ViewState) {

        showMoodsForFilter(state.moods)

        if (state.isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }

        if (state.isError) {
            return
        } else {
            Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
        }

        showEntries(state.entries)
    }

    private fun setEntriesRView() {
        binding.emojiRecyclerView.setHasFixedSize(true)
        binding.emojiRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.emojiRecyclerView.adapter = entriesAdapter
    }

    private fun setMoodFilterRView() {
        binding.moodFilterRecyclerView.setHasFixedSize(true)
        binding.moodFilterRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.moodFilterRecyclerView.adapter = moodFilterAdapter
    }

    private fun showEntries(entries: List<Entry>) {
        entriesAdapter.submitList(entries)
    }

    private fun showMoodsForFilter(moods: List<Mood> ) {
        moodFilterAdapter.submitList(moods)
    }

    private fun setupClicks() {
        binding.swipeRefreshLayout.setOnClickListener {
            viewModel.onIntent(EntriesViewModel.ViewState.Intents.LoadEntries)
        }

        moodFilterAdapter.onMoodClick = { mood ->
            viewModel.onIntent(EntriesViewModel.ViewState.Intents.FilterByMood(mood))
        }

        binding.resetFilterButton.setOnClickListener {
            viewModel.onIntent(EntriesViewModel.ViewState.Intents.ClearFilters)
        }

        binding.addEntryButton.setOnClickListener {
            findNavController().navigate(R.id.entryEditFragment)
        }
    }

//    private fun showDeleteDialog(entry: Entry) {
//        AlertDialog.Builder(requireContext())
//            .setTitle("Удалить запись?")
//            .setMessage("Вы уверены, что хотите удалить эту запись?")
//            .setPositiveButton("Удалить") { _, _ ->
//                viewModel.onIntent(EntriesViewModel.ViewState.Intents.DeleteEntry(entry))
//            }
//            .setNegativeButton("Отмена", null)
//            .show()
//    }

}