package com.example.respiratoryhealthapp.screens.dashboard

import com.example.respiratoryhealthapp.data.model.Reminder // Import Reminder

data class DashboardUiState(
    val userName: String = "User", // Placeholder for user's name
    val upcomingReminders: List<Reminder> = emptyList(), // For upcoming reminders
    val isLoading: Boolean = false,
    val error: String? = null
) 