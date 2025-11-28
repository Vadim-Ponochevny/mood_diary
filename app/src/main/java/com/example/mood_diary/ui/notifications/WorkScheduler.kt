package com.example.mood_diary.ui.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mood_diary.data.work.MoodReminderWorker
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime
import java.util.concurrent.TimeUnit

object WorkScheduler {

    fun scheduleDailyMoodReminder(context: Context) {

        val now = LocalDateTime.now()
        var target = now.withHour(21).withMinute(0).withSecond(0).withNano(0)

        if (now >= target) {
            target = target.plusDays(1)
        }

        val delay = Duration.between(now, target).toMillis()

        val request = PeriodicWorkRequestBuilder<MoodReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "daily_mood_reminder",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
    }
}