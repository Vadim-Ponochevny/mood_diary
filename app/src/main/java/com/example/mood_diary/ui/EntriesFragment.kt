package com.example.mood_diary.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mood_diary.R
import com.example.mood_diary.databinding.FragmentEntriesBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
/**
 * A simple [androidx.fragment.app.Fragment] subclass.
 * Use the [EntriesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EntriesFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentEntriesBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(com.example.mood_diary.ui.ARG_PARAM1)
            param2 = it.getString(com.example.mood_diary.ui.ARG_PARAM2)
        }
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

        val controller = findNavController()

        binding.searchActionButton.setOnClickListener {
            controller.navigate(R.id.entryEditFragment)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EntriesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EntriesFragment().apply {
                arguments = Bundle().apply {
                    putString(com.example.mood_diary.ui.ARG_PARAM1, param1)
                    putString(com.example.mood_diary.ui.ARG_PARAM2, param2)
                }
            }
    }
}