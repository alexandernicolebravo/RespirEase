package com.example.respiratoryhealthapp.data.repository

import com.example.respiratoryhealthapp.data.dao.DoctorNoteDao
import com.example.respiratoryhealthapp.data.model.DoctorNote
import kotlinx.coroutines.flow.Flow

class RoomDoctorNoteRepository(private val doctorNoteDao: DoctorNoteDao) : DoctorNoteRepository {

    override fun getAllDoctorNotes(): Flow<List<DoctorNote>> {
        return doctorNoteDao.getAllDoctorNotes()
    }

    override fun getDoctorNoteById(id: Long): Flow<DoctorNote?> {
        return doctorNoteDao.getDoctorNoteById(id)
    }

    override suspend fun insertDoctorNote(doctorNote: DoctorNote): Long {
        return doctorNoteDao.insertDoctorNote(doctorNote)
    }

    override suspend fun updateDoctorNote(doctorNote: DoctorNote) {
        doctorNoteDao.updateDoctorNote(doctorNote)
    }

    override suspend fun deleteDoctorNote(doctorNote: DoctorNote) {
        doctorNoteDao.deleteDoctorNote(doctorNote)
    }

    override fun searchDoctorNotes(query: String): Flow<List<DoctorNote>> {
        return doctorNoteDao.searchDoctorNotes(query)
    }
} 