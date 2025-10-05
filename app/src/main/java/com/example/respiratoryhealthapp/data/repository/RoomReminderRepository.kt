package com.example.respiratoryhealthapp.data.repository

import com.example.respiratoryhealthapp.data.dao.ReminderDao
import com.example.respiratoryhealthapp.data.model.Reminder
import kotlinx.coroutines.flow.Flow

class RoomReminderRepository(
    private val reminderDao: ReminderDao
) : ReminderRepository {

    override fun getAllReminders(): Flow<List<Reminder>> =
        reminderDao.getAllReminders()

    override fun getReminderById(id: Long): Flow<Reminder?> =
        reminderDao.getReminderById(id)

    override suspend fun insertReminder(reminder: Reminder): Long =
        reminderDao.insert(reminder)

    override suspend fun updateReminder(reminder: Reminder) {
        reminderDao.update(reminder)
    }

    override suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.delete(reminder)
    }

    override fun getActiveReminders(): Flow<List<Reminder>> =
        reminderDao.getActiveReminders()
} 