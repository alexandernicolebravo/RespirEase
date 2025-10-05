package com.example.respiratoryhealthapp.data.database

import androidx.room.TypeConverter
import com.example.respiratoryhealthapp.data.model.RespiratoryCondition

class BreathingExerciseTypeConverters {
    @TypeConverter
    fun fromStringList(value: String?): List<String>? {
        return value?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun fromRespiratoryConditionList(value: String?): List<RespiratoryCondition>? {
        return value?.split(",")?.mapNotNull { enumName ->
            try {
                RespiratoryCondition.valueOf(enumName.trim())
            } catch (e: IllegalArgumentException) {
                android.util.Log.e("TypeConverter", "Invalid RespiratoryCondition enum name: $enumName", e)
                null // Or handle error, e.g., log it
            }
        }?.filter { it.name.isNotEmpty() }
    }

    @TypeConverter
    fun toRespiratoryConditionList(list: List<RespiratoryCondition>?): String? {
        return list?.joinToString(",") { it.name }
    }
} 