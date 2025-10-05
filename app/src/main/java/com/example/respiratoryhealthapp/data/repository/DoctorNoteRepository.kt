package com.example.respiratoryhealthapp.data.repository

import com.example.respiratoryhealthapp.data.model.DoctorNote
import kotlinx.coroutines.flow.Flow

interface DoctorNoteRepository {
    fun getAllDoctorNotes(): Flow<List<DoctorNote>>
    fun getDoctorNoteById(id: Long): Flow<DoctorNote?>
    suspend fun insertDoctorNote(doctorNote: DoctorNote): Long
    suspend fun updateDoctorNote(doctorNote: DoctorNote)
    suspend fun deleteDoctorNote(doctorNote: DoctorNote)
    fun searchDoctorNotes(query: String): Flow<List<DoctorNote>>
} 