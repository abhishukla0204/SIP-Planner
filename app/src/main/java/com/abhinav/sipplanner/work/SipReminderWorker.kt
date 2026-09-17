package com.abhinav.sipplanner.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.abhinav.sipplanner.R
import com.abhinav.sipplanner.core.format.Money
import com.abhinav.sipplanner.domain.repository.GoalRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * A local reminder of what you've committed to each month.
 *
 * WorkManager runs entirely on the device — there is no push service, no server,
 * and therefore no bill. That is the whole reason this works for free.
 */
@HiltWorker
class SipReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val goalRepository: GoalRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val goals = goalRepository.observeGoals().first()
        if (goals.isEmpty()) return Result.success()

        val total = goals.sumOf { it.monthlyContribution }
        notify(
            title = "${Money.rupees(total)} due this month",
            body = "Across ${goals.size} ${if (goals.size == 1) "goal" else "goals"}. " +
                "Log the instalments once they go through.",
        )
        return Result.success()
    }

    private fun notify(title: String, body: String) {
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "SIP reminders",
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )

        val granted = ActivityCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val CHANNEL_ID = "sip_reminders"
        private const val NOTIFICATION_ID = 1001
        private const val WORK_NAME = "sip-monthly-reminder"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<SipReminderWorker>(30, TimeUnit.DAYS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
