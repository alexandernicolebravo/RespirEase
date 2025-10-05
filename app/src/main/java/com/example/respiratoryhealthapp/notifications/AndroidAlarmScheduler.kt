package com.example.respiratoryhealthapp.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.respiratoryhealthapp.data.model.Reminder
import com.example.respiratoryhealthapp.data.model.ReminderRecurrence
import com.example.respiratoryhealthapp.data.repository.UserProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId

class AndroidAlarmScheduler(
    private val context: Context,
    private val userProfileRepository: UserProfileRepository
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun schedule(reminder: Reminder) {
        scope.launch {
            val masterNotificationsEnabled = userProfileRepository.userProfileFlow.first().masterNotificationsEnabled

            if (!masterNotificationsEnabled) {
                Log.d("AndroidAlarmScheduler", "Master notifications disabled. Skipping schedule for reminder: ${reminder.id}")
                return@launch
            }

            if (!reminder.isActive) {
                Log.d("AndroidAlarmScheduler", "Skipping schedule for inactive reminder: ${reminder.id}")
                return@launch
            }

            val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
                putExtra(ReminderBroadcastReceiver.EXTRA_REMINDER_ID, reminder.id)
                putExtra(ReminderBroadcastReceiver.EXTRA_REMINDER_TITLE, reminder.title)
                putExtra(ReminderBroadcastReceiver.EXTRA_REMINDER_DESCRIPTION, reminder.description)
            }

            val schedulePendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.id.toInt(),
                intent,
                schedulePendingIntentFlags
            )

            val triggerAtMillis = reminder.dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Log.w("AndroidAlarmScheduler", "Cannot schedule exact alarms. Reminder ${reminder.id} might not be precise.")
            }

            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                Log.d("AndroidAlarmScheduler", "Scheduled reminder ${reminder.id} for ${reminder.dateTime}")
            } catch (se: SecurityException) {
                Log.e("AndroidAlarmScheduler", "SecurityException on scheduling reminder ${reminder.id}. Missing SCHEDULE_EXACT_ALARM or app in restricted mode?", se)
            }
        }
    }

    override fun reschedule(reminder: Reminder) {
        scope.launch {
            val masterNotificationsEnabled = userProfileRepository.userProfileFlow.first().masterNotificationsEnabled
            if (!masterNotificationsEnabled) {
                Log.d("AndroidAlarmScheduler", "Master notifications disabled. Skipping reschedule for reminder: ${reminder.id}")
                return@launch
            }

            if (!reminder.isActive) {
                Log.d("AndroidAlarmScheduler", "Skipping reschedule for inactive reminder: ${reminder.id}")
                return@launch
            }

            val nextDateTime = calculateNextOccurrence(reminder.dateTime, reminder.recurrence)

            if (nextDateTime != null) {
                Log.d("AndroidAlarmScheduler", "Rescheduling reminder ${reminder.id} for next occurrence at $nextDateTime")
                performSchedule(reminder, nextDateTime)
            } else {
                Log.d("AndroidAlarmScheduler", "Reminder ${reminder.id} is non-recurring or next date calculation failed. Not rescheduling.")
            }
        }
    }

    private fun performSchedule(reminder: Reminder, scheduleDateTime: LocalDateTime) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra(ReminderBroadcastReceiver.EXTRA_REMINDER_ID, reminder.id)
            putExtra(ReminderBroadcastReceiver.EXTRA_REMINDER_TITLE, reminder.title)
            putExtra(ReminderBroadcastReceiver.EXTRA_REMINDER_DESCRIPTION, reminder.description)
            putExtra("reminder_recurrence", reminder.recurrence.name)
            putExtra("reminder_original_datetime_millis", reminder.dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        }

        val schedulePendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            schedulePendingIntentFlags
        )

        val triggerAtMillis = scheduleDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.w("AndroidAlarmScheduler", "Cannot schedule exact alarms during performSchedule. Reminder ${reminder.id} might not be precise.")
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            Log.d("AndroidAlarmScheduler", "Performed schedule for reminder ${reminder.id} at $scheduleDateTime (Trigger: $triggerAtMillis)")
        } catch (se: SecurityException) {
            Log.e("AndroidAlarmScheduler", "SecurityException during performSchedule for reminder ${reminder.id}", se)
        }
    }

    private fun calculateNextOccurrence(currentDateTime: LocalDateTime, recurrence: ReminderRecurrence): LocalDateTime? {
        return when (recurrence) {
            ReminderRecurrence.DAILY -> currentDateTime.plusDays(1)
            ReminderRecurrence.WEEKLY -> currentDateTime.plusWeeks(1)
            ReminderRecurrence.MONTHLY -> currentDateTime.plusMonths(1)
            ReminderRecurrence.NONE -> null
        }
    }

    override fun cancel(reminder: Reminder) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra(ReminderBroadcastReceiver.EXTRA_REMINDER_ID, reminder.id)
        }

        val cancelPendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        val pendingIntentToCancel = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            cancelPendingIntentFlags
        )
        alarmManager.cancel(pendingIntentToCancel)
        Log.d("AndroidAlarmScheduler", "Cancelled reminder ${reminder.id}")
    }
} 