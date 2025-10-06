package com.example.mood_diary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mood_diary.EntriesAdapter.ViewHolder
import com.example.mood_diary.data.model.Entry
import com.example.mood_diary.data.model.Mood
import com.example.mood_diary.databinding.ItemMoodEntryBinding
import org.threeten.bp.format.TextStyle
import java.util.Locale

class EntriesAdapter() : ListAdapter<Entry, ViewHolder>(EntryDiffCallback()) {

    inner class ViewHolder(
        val binding: ItemMoodEntryBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind (item: Entry) {
            val dateTimeText = "${item.date.dayOfMonth} " +
                    "${item.date.month.getDisplayName(TextStyle.FULL, Locale("ru"))} " +
                    "${item.date.year} г. " +
                    item.time.toString().substring(0, 5)

            binding.tvMoodEmoji.text = item.mood.emoji
            binding.tvMoodTeg.text = item.teg
            binding.tvMoodDateTime.text = dateTimeText

            val color = when(item.mood) {
                Mood.SAD -> R.color.sad
                Mood.NEUTRAL -> R.color.neutral
                Mood.HAPPY -> R.color.happy
                Mood.ANGRY -> R.color.angry
            }

            binding.root.setBackgroundColor(color)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemMoodEntryBinding.inflate(
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

class EntryDiffCallback : DiffUtil.ItemCallback<Entry>() {

    override fun areItemsTheSame(
        oldItemPosition: Entry,
        newItemPosition: Entry
    ): Boolean {
        return oldItemPosition == newItemPosition
    }

    override fun areContentsTheSame(
        oldItemPosition: Entry,
        newItemPosition: Entry
    ): Boolean {
        return oldItemPosition == newItemPosition
    }
}