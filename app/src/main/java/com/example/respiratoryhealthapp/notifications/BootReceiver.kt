package com.example.respiratoryhealthapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.respiratoryhealthapp.data.database.AppDatabase
import com.example.respiratoryhealthapp.data.repository.RoomReminderRepository
import com.example.respiratoryhealthapp.data.repository.UserProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class BootReceiver : BroadcastReceiver() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device booted. Re-scheduling reminders.")

            // It's important to use applicationContext for long-lived operations or when passing context
            // to objects that might outlive the receiver, like the repository or scheduler.
            val applicationContext = context.applicationContext

            // Dependencies need to be instantiated here as the receiver is created by the system
            val database = AppDatabase.getDatabase(applicationContext)
            val repository = RoomReminderRepository(database.reminderDao())
            val userProfileRepository = UserProfileRepository(applicationContext)
            val scheduler = AndroidAlarmScheduler(applicationContext, userProfileRepository)

            scope.launch {
                try {
                    val activeReminders = repository.getActiveReminders().firstOrNull()
                    if (activeReminders.isNullOrEmpty()) {
                        Log.d("BootReceiver", "No active reminders to re-schedule.")
                        return@launch
                    }
                    activeReminders.forEach { reminder ->
                        if (reminder.isActive) { // Double check, though getActiveReminders should handle this
                            scheduler.schedule(reminder)
                            Log.d("BootReceiver", "Re-scheduled reminder: ${reminder.id} - ${reminder.title}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error re-scheduling reminders", e)
                }
            }
        }
    }
    // Note: job.cancel() is not called here as BootReceiver is short-lived.
    // For more complex scenarios with ongoing work, ensure coroutine cancellation.
} 