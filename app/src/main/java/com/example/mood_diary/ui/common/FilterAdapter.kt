package com.example.mood_diary.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mood_diary.data.model.Mood
import com.example.mood_diary.databinding.ItemMoodFilterBinding
import com.example.mood_diary.ui.common.FilterAdapter.ViewHolder

class FilterAdapter(
) : ListAdapter<Mood, ViewHolder>(MoodDiffCallback()) {

    var selectedMood: Mood? = null

    var onMoodClick: ((Mood) -> Unit)? = null

    inner class ViewHolder(
        private val binding: ItemMoodFilterBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(mood: Mood) {
            Glide.with(binding.root.context)
                .load(mood.emoji)
                .into(binding.moodIcon)

            val isSelected = mood == selectedMood

            binding.root.alpha = if (isSelected) 1f else 0.5f

            binding.root.setOnClickListener {
                if (selectedMood == mood) return@setOnClickListener

                val oldPosition = currentList.indexOf(selectedMood)
                val newPosition = bindingAdapterPosition

                selectedMood = mood
                onMoodClick?.invoke(mood)

                if (oldPosition != -1) notifyItemChanged(oldPosition)
                notifyItemChanged(newPosition)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemMoodFilterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        return holder.bind(getItem(position))
    }
    
}

class MoodDiffCallback : DiffUtil.ItemCallback<Mood>() {

    override fun areItemsTheSame(
        oldItemPosition: Mood,
        newItemPosition: Mood
    ): Boolean {
        return oldItemPosition.emoji == newItemPosition.emoji
    }

    override fun areContentsTheSame(
        oldItemPosition: Mood,
        newItemPosition: Mood
    ): Boolean {
        return oldItemPosition.value == newItemPosition.value
    }
}