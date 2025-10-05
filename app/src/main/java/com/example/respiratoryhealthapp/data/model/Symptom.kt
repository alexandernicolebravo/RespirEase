package com.example.respiratoryhealthapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.respiratoryhealthapp.data.database.Converters
import java.time.LocalDateTime

@Entity(tableName = "symptoms")
@TypeConverters(Converters::class)
data class Symptom(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: LocalDateTime,
    val type: SymptomType,
    val severity: Int, // 1-5 scale
    val notes: String? = null,
    val trigger: String? = null,
    val peakFlow: Int? = null // Optional peak flow reading
)

enum class SymptomType {
    COUGH,
    SHORTNESS_OF_BREATH,
    WHEEZING,
    CHEST_TIGHTNESS,
    FATIGUE,
    OTHER
} 