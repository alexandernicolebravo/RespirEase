package com.example.respiratoryhealthapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val dateTime: LocalDateTime,
    val type: ReminderType,
    val recurrence: ReminderRecurrence,
    val isActive: Boolean = true // To allow users to toggle reminders on/off
) 