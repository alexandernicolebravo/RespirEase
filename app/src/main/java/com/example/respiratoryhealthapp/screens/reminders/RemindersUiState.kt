package com.example.respiratoryhealthapp.screens.reminders

import com.example.respiratoryhealthapp.data.model.Reminder

data class RemindersUiState(
    val reminders: List<Reminder> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedReminder: Reminder? = null, // For editing or viewing details
    // We might also want to include state for any dialogs (e.g., add/edit dialog)
    // val showAddEditDialog: Boolean = false,
    // val reminderToEdit: Reminder? = null
) 