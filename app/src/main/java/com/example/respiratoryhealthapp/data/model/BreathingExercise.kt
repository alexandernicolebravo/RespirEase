package com.example.respiratoryhealthapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
// We'll need a TypeConverter for List<String> and List<RespiratoryCondition> if storing in Room
// For now, let's assume we are not using Room for this or will add converters later.

@Entity(tableName = "breathing_exercises")
// We'll add TypeConverters to AppDatabase for List<String> and List<RespiratoryCondition>
data class BreathingExercise(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val instructions: List<String>,
    val durationMinutes: Int,
    val targetConditions: List<RespiratoryCondition>,
    val videoUrl: String? = null, // e.g., a YouTube link or local resource identifier
    val benefits: List<String> = emptyList()
) 