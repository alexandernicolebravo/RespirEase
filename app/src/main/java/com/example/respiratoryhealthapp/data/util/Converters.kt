package com.example.respiratoryhealthapp.data.util

import androidx.room.TypeConverter
import com.example.respiratoryhealthapp.data.model.SymptomType
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
} 