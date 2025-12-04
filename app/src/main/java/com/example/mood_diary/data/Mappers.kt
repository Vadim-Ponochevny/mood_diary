package com.example.mood_diary.data

import com.example.mood_diary.data.model.DataEntry as DataEntry
import com.example.mood_diary.domain.model.DomainEntry as DomainEntry
import com.example.mood_diary.data.model.Mood as DataMood
import com.example.mood_diary.domain.model.Mood as DomainMood

fun DomainMood.toData(): DataMood {
    return DataMood.fromValue(this.value)
}

fun DataMood.toDomain(): DomainMood {
    return DomainMood.fromValue(this.value)
}

fun DataEntry.toDomain(): DomainEntry {
    return DomainEntry(
        id = this.id,
        mood = this.mood.toDomain(),
        teg = this.teg,
        des = this.des,
        date = this.date,
        time = this.time
    )
}

fun DomainEntry.toData(): DataEntry {
    return DataEntry(
        id = this.id,
        mood = this.mood.toData(),
        teg = this.teg,
        des = this.des,
        date = this.date,
        time = this.time
    )
}