package com.example.respiratoryhealthapp.screens.reminders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.respiratoryhealthapp.data.database.AppDatabase
import com.example.respiratoryhealthapp.data.model.Reminder
import com.example.respiratoryhealthapp.data.repository.ReminderRepository
import com.example.respiratoryhealthapp.data.repository.RoomReminderRepository
import com.example.respiratoryhealthapp.data.repository.UserProfileRepository
import com.example.respiratoryhealthapp.notifications.AlarmScheduler
import com.example.respiratoryhealthapp.notifications.AndroidAlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RemindersViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ReminderRepository
    private val scheduler: AlarmScheduler

    private val _uiState = MutableStateFlow(RemindersUiState())
    val uiState: StateFlow<RemindersUiState> = _uiState.asStateFlow()

    init {
        val reminderDao = AppDatabase.getDatabase(application).reminderDao()
        repository = RoomReminderRepository(reminderDao)
        val userProfileRepository = UserProfileRepository(application)
        scheduler = AndroidAlarmScheduler(application, userProfileRepository)

        viewModelScope.launch {
            repository.getAllReminders() // Always get all reminders
            .onStart { _uiState.update { it.copy(isLoading = true, error = null) } }
            .catch { e ->
                _uiState.update { it.copy(isLoading = false, error = "Failed to load reminders: ${e.message}") }
            }
            .collect { reminders ->
                _uiState.update { it.copy(reminders = reminders, isLoading = false) }
            }
        }
    }

    fun addReminder(reminder: Reminder) {
        viewModelScope.launch {
            val newId = repository.insertReminder(reminder)
            val newReminder = reminder.copy(id = newId)
            if (newReminder.isActive) {
                scheduler.schedule(newReminder)
            }
            // Optionally, refresh list or handle UI update based on insert result
        }
    }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            val oldReminder = repository.getReminderById(reminder.id).firstOrNull() // Get old state for comparison
            repository.updateReminder(reminder)

            if (reminder.isActive) {
                var shouldSchedule = false
                if (oldReminder == null) { // Should not happen in update, but good for safety
                    shouldSchedule = true
                } else {
                    if (!oldReminder.isActive) { // Was inactive, now active
                        shouldSchedule = true
                    } else if (oldReminder.dateTime != reminder.dateTime) { // Was active, and time changed
                        shouldSchedule = true
                    }
                }
                if (shouldSchedule) {
                    scheduler.schedule(reminder)
                }
            } else { // Reminder is now inactive
                if (oldReminder?.isActive == true) { // Was active, now inactive
                    scheduler.cancel(reminder)
                }
            }
            // Optionally, refresh list or handle UI update
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            scheduler.cancel(reminder)
            repository.deleteReminder(reminder)
            // Optionally, refresh list or handle UI update
        }
    }

    fun selectReminder(reminder: Reminder?) {
        _uiState.update { it.copy(selectedReminder = reminder) }
    }

    fun toggleReminderActiveState(reminder: Reminder, isActive: Boolean) {
        viewModelScope.launch {
            val updatedReminder = reminder.copy(isActive = isActive)
            repository.updateReminder(updatedReminder)
            if (updatedReminder.isActive) {
                scheduler.schedule(updatedReminder)
            } else {
                scheduler.cancel(updatedReminder)
            }
        }
    }
} 