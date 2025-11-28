package com.example.mood_diary.ui.entries

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mood_diary.ui.entries.EntriesAdapter.ViewHolder
import com.example.mood_diary.data.model.Entry
import com.example.mood_diary.databinding.ItemMoodEntryBinding
import org.threeten.bp.format.TextStyle
import java.util.Locale
import com.bumptech.glide.Glide

class EntriesAdapter(
) : ListAdapter<Entry, ViewHolder>(EntryDiffCallback()) {

    var onEntryClick: ((Int) -> Unit) = {}

    inner class ViewHolder(
        val binding: ItemMoodEntryBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind (item: Entry) {
            val dateTimeText = "${item.date.dayOfMonth} " +
                    "${item.date.month.getDisplayName(TextStyle.FULL, Locale("ru"))} " +
                    "${item.date.year} г. " +
                    item.time.toString().substring(0, 5)

            Glide.with(binding.root.context)
                .load(item.mood.emoji)
                .into(binding.emodjiView)

            binding.tvMoodTeg.text = item.teg
            binding.tvMoodDateTime.text = dateTimeText

            binding.root.setOnClickListener {
                onEntryClick.invoke(item.id)
            }

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
        return oldItemPosition.id == newItemPosition.id
    }

    override fun areContentsTheSame(
        oldItemPosition: Entry,
        newItemPosition: Entry
    ): Boolean {
        return oldItemPosition == newItemPosition
    }
}