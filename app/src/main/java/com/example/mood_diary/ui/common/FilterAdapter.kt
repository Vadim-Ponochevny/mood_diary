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
import androidx.core.content.ContextCompat
import com.example.mood_diary.R

class FilterAdapter(
) : ListAdapter<MoodFilterItem, ViewHolder>(MoodFilterItemDiffCallback()) {

    var onMoodClick: ((Mood) -> Unit)? = null

    inner class ViewHolder(
        private val binding: ItemMoodFilterBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MoodFilterItem) {
            Glide.with(binding.root.context)
                .load(item.mood.emoji)
                .into(binding.moodIcon)

            if (item.isSelected) {
                binding.moodCard.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.white)
                )
                binding.moodCard.alpha = 1f
            } else {
                binding.moodCard.strokeColor =
                    ContextCompat.getColor(binding.root.context, android.R.color.transparent)
                binding.moodCard.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, android.R.color.white)
                )
                binding.moodCard.alpha = 0.5f
            }

            binding.root.setOnClickListener {
                onMoodClick?.invoke(item.mood)
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

class MoodFilterItemDiffCallback : DiffUtil.ItemCallback<MoodFilterItem>() {
    override fun areItemsTheSame(oldItem: MoodFilterItem, newItem: MoodFilterItem): Boolean {
        return oldItem.mood == newItem.mood
    }

    override fun areContentsTheSame(oldItem: MoodFilterItem, newItem: MoodFilterItem): Boolean {
        return oldItem == newItem

    }
}