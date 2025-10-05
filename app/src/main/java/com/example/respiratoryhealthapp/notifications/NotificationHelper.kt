package com.example.respiratoryhealthapp.notifications

// import android.app.Application // Removed unused import
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationHelper {

    const val REMINDER_CHANNEL_ID = "reminder_channel"
    const val REMINDER_CHANNEL_NAME = "Reminders"
    const val REMINDER_CHANNEL_DESCRIPTION = "Channel for medication and appointment reminders"

    fun createNotificationChannel(context: Context) {
        // No SDK_INT check needed as minSdk is 26 (Android O)
        val importance = NotificationManager.IMPORTANCE_HIGH // Or IMPORTANCE_DEFAULT
        val channel = NotificationChannel(REMINDER_CHANNEL_ID, REMINDER_CHANNEL_NAME, importance).apply {
            description = REMINDER_CHANNEL_DESCRIPTION
            // Optionally enable lights, vibration, etc.
            // enableLights(true)
            // lightColor = Color.RED
            // enableVibration(true)
            // vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
} 