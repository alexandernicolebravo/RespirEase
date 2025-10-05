package com.example.respiratoryhealthapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "doctors_notes")
data class DoctorNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: LocalDateTime,
    val title: String? = null,
    val content: String,
    val doctorName: String? = null,
    val nextAppointment: LocalDateTime? = null
) 