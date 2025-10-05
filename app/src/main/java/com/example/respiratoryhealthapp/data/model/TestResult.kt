package com.example.respiratoryhealthapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "test_results")
data class TestResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: LocalDateTime,
    val testName: String, // e.g., "Peak Flow", "FEV1", "Oxygen Saturation"
    val value: String,    // e.g., "450", "2.5", "98%" - kept as String for flexibility
    val unit: String? = null,   // e.g., "L/min", "L", "%"
    val notes: String? = null
) 