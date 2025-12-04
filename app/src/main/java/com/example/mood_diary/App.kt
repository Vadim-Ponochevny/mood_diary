package com.example.mood_diary

import android.app.Application
import com.example.mood_diary.ui.notifications.NotificationHelper
import com.example.mood_diary.ui.notifications.WorkScheduler
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)

        NotificationHelper.createChannel(this)

        WorkScheduler.scheduleDailyMoodReminder(this)
    }
}