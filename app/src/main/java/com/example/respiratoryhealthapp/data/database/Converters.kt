package com.example.respiratoryhealthapp.data.database

import androidx.room.TypeConverter
import com.example.respiratoryhealthapp.data.model.SymptomType
import com.example.respiratoryhealthapp.data.model.ReminderType
import com.example.respiratoryhealthapp.data.model.ReminderRecurrence
import java.time.LocalDateTime
import java.time.ZoneOffset

@Suppress("UNUSED")
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.toEpochSecond(ZoneOffset.UTC)
    }

    @TypeConverter
    fun fromSymptomType(value: SymptomType): String {
        return value.name
    }

    @TypeConverter
    fun toSymptomType(value: String): SymptomType {
        return SymptomType.valueOf(value)
    }

    // Converters for ReminderType Enum
    @TypeConverter
    fun fromReminderType(value: String?): ReminderType? {
        return value?.let { ReminderType.valueOf(it) }
    }

    @TypeConverter
    fun toReminderType(type: ReminderType?): String? {
        return type?.name
    }

    // Converters for ReminderRecurrence Enum
    @TypeConverter
    fun fromReminderRecurrence(value: String?): ReminderRecurrence? {
        return value?.let { ReminderRecurrence.valueOf(it) }
    }

    @TypeConverter
    fun toReminderRecurrence(recurrence: ReminderRecurrence?): String? {
        return recurrence?.name
    }
} 