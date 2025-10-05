package com.example.respiratoryhealthapp.notifications

import com.example.respiratoryhealthapp.data.model.Reminder

interface AlarmScheduler {
    fun schedule(reminder: Reminder)
    fun cancel(reminder: Reminder)
    fun reschedule(reminder: Reminder)
} 