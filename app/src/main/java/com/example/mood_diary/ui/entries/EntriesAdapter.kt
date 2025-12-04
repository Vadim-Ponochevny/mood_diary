package com.example.mood_diary.ui.entries

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mood_diary.ui.entries.EntriesAdapter.ViewHolder
import com.example.mood_diary.domain.model.DomainEntry
import com.example.mood_diary.databinding.ItemMoodEntryBinding
import org.threeten.bp.format.TextStyle
import java.util.Locale
import com.bumptech.glide.Glide
import com.example.mood_diary.R
import org.threeten.bp.LocalDate

class EntriesAdapter(

) : ListAdapter<DomainEntry, ViewHolder>(EntryDiffCallback()) {

    var onEntryClick: ((Int) -> Unit) = {}

    inner class ViewHolder(
        val binding: ItemMoodEntryBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind (item: DomainEntry) {

            val context = itemView.context

            val date = LocalDate.parse(item.date)

            val formattedDate = context.getString(
                R.string.entry_date_format_long,
                date.dayOfMonth,
                date.month.getDisplayName(
                    TextStyle.FULL,
                    Locale.forLanguageTag("ru")
                ),
                date.year
            )

            val formattedTime = item.time.take(5)

            val dateTimeText = context.getString(
                R.string.entry_date_time_format,
                formattedDate,
                formattedTime
            )

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

class EntryDiffCallback : DiffUtil.ItemCallback<DomainEntry>() {

    override fun areItemsTheSame(
        oldItemPosition: DomainEntry,
        newItemPosition: DomainEntry
    ): Boolean {
        return oldItemPosition.id == newItemPosition.id
    }

    override fun areContentsTheSame(
        oldItemPosition: DomainEntry,
        newItemPosition: DomainEntry
    ): Boolean {
        return oldItemPosition == newItemPosition
    }
}