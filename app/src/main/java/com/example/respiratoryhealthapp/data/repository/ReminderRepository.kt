package com.example.respiratoryhealthapp.data.repository

import com.example.respiratoryhealthapp.data.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getAllReminders(): Flow<List<Reminder>>
    fun getReminderById(id: Long): Flow<Reminder?>
    suspend fun insertReminder(reminder: Reminder): Long
    suspend fun updateReminder(reminder: Reminder)
    suspend fun deleteReminder(reminder: Reminder)
    fun getActiveReminders(): Flow<List<Reminder>>
} 