package com.example.respiratoryhealthapp.data.model

enum class ReminderRecurrence(val displayName: String) {
    NONE("Does not repeat"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    // CUSTOM("Custom...") // For more complex patterns
} 