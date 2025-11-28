package com.example.mood_diary.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "mood_daily_reminder",
                "Mood Journal Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "Напоминания о заполнении дневника настроения"

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}