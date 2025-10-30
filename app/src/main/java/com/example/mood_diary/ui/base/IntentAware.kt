package com.example.mood_diary.ui.base

interface IntentAware<T> {
    fun onIntent(intent: T)
}