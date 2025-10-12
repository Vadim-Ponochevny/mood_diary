package com.example.mood_diary.ui

interface IntentAware<T> {
    fun onIntent(intent: T)
}