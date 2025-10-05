package com.example.respiratoryhealthapp.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.respiratoryhealthapp.MainActivity
import com.example.respiratoryhealthapp.R
import com.example.respiratoryhealthapp.data.database.AppDatabase
import com.example.respiratoryhealthapp.data.model.ReminderRecurrence
import com.example.respiratoryhealthapp.data.repository.ReminderRepository
import com.example.respiratoryhealthapp.data.repository.RoomReminderRepository
import com.example.respiratoryhealthapp.data.repository.UserProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReminderBroadcastReceiver : BroadcastReceiver() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_REMINDER_TITLE = "reminder_title"
        const val EXTRA_REMINDER_DESCRIPTION = "reminder_description"
        // Keys for extras passed by AndroidAlarmScheduler's performSchedule method
        const val EXTRA_REMINDER_RECURRENCE = "reminder_recurrence"
        const val EXTRA_REMINDER_ORIGINAL_DATETIME_MILLIS = "reminder_original_datetime_millis"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, 0L)
        if (reminderId == 0L) {
            Log.e("ReminderReceiver", "Received reminder with invalid ID. Aborting.")
            return
        }
        val title = intent.getStringExtra(EXTRA_REMINDER_TITLE) ?: "Reminder"
        val description = intent.getStringExtra(EXTRA_REMINDER_DESCRIPTION) ?: "Time for your reminder."
        val recurrenceString = intent.getStringExtra(EXTRA_REMINDER_RECURRENCE)
        val originalDateTimeMillis = intent.getLongExtra(EXTRA_REMINDER_ORIGINAL_DATETIME_MILLIS, 0L)

        scope.launch {
            val applicationContext = context.applicationContext
            val userProfileRepository = UserProfileRepository(applicationContext)
            val masterNotificationsEnabled = userProfileRepository.userProfileFlow.first().masterNotificationsEnabled

            if (!masterNotificationsEnabled) {
                Log.d("ReminderReceiver", "Master notifications disabled. Suppressing notification for reminder: $reminderId")
                return@launch
            }

            // --- Display Notification --- (Moved up, before rescheduling)
            val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val pendingIntent = PendingIntent.getActivity(context, reminderId.toInt(), mainActivityIntent, pendingIntentFlags)

            val builder = NotificationCompat.Builder(context, NotificationHelper.REMINDER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) 
                .setContentTitle(title)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            with(NotificationManagerCompat.from(context)) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    Log.w("ReminderReceiver", "POST_NOTIFICATIONS permission not granted for reminder $reminderId. Notification may not be shown.")
                    // Even if permission is missing, we might still attempt to reschedule if it's a recurring one,
                    // assuming the permission might be granted later.
                    // However, for this flow, if we can't show the current notification, we might not reschedule immediately.
                    // For simplicity, if notification isn't shown, we won't proceed to reschedule now.
                    // This could be revisited if finer control is needed.
                } else {
                     notify(reminderId.toInt(), builder.build())
                     Log.d("ReminderReceiver", "Notification shown for reminder $reminderId")
                }
            }

            // --- Reschedule if Recurring --- 
            if (recurrenceString != null && originalDateTimeMillis > 0) {
                val recurrence = ReminderRecurrence.entries.firstOrNull { it.name == recurrenceString }
                if (recurrence != null && recurrence != ReminderRecurrence.NONE) {
                    val reminderRepository: ReminderRepository = RoomReminderRepository(AppDatabase.getDatabase(applicationContext).reminderDao())
                    val originalReminder = reminderRepository.getReminderById(reminderId).first()

                    if (originalReminder != null && originalReminder.isActive) {
                        // Important: The AndroidAlarmScheduler.reschedule method now expects the *original* reminder
                        // as it contains the original dateTime from which to calculate the next occurrence.
                        // The `performSchedule` within `AndroidAlarmScheduler` uses this original reminder's details
                        // and the *calculated* nextDateTime for the actual alarm setting.
                        
                        // We need to ensure the originalReminder.dateTime is what `calculateNextOccurrence` expects.
                        // The `AndroidAlarmScheduler.reschedule` handles the calculation using the reminder's stored datetime.
                        val scheduler: AlarmScheduler = AndroidAlarmScheduler(applicationContext, userProfileRepository)
                        
                        // The variable reminderThatJustFired was unused.
                        // The originalReminder already contains the necessary original datetime for rescheduling logic.
                        // val justFiredDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(originalDateTimeMillis), ZoneId.systemDefault())
                        // val reminderThatJustFired = originalReminder.copy(dateTime = justFiredDateTime, recurrence = recurrence) // Ensure recurrence is current

                        Log.d("ReminderReceiver", "Attempting to reschedule reminder: ${originalReminder.id} which was originally for ${originalReminder.dateTime}, recurrence: $recurrence")
                        scheduler.reschedule(originalReminder) // Pass the original reminder for rescheduling logic
                    } else {
                        Log.d("ReminderReceiver", "Original reminder $reminderId not found or inactive. Not rescheduling.")
                    }
                }
            }
        }
    }
} 