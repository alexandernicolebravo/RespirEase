package com.example.respiratoryhealthapp.data.dao

import androidx.room.*
import com.example.respiratoryhealthapp.data.model.DoctorNote
import kotlinx.coroutines.flow.Flow

@Dao
interface DoctorNoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctorNote(doctorNote: DoctorNote): Long

    @Update
    suspend fun updateDoctorNote(doctorNote: DoctorNote)

    @Delete
    suspend fun deleteDoctorNote(doctorNote: DoctorNote)

    @Query("SELECT * FROM doctors_notes WHERE id = :id")
    fun getDoctorNoteById(id: Long): Flow<DoctorNote?>

    @Query("SELECT * FROM doctors_notes ORDER BY timestamp DESC")
    fun getAllDoctorNotes(): Flow<List<DoctorNote>>

    // Optional: Query to search notes by content or title
    @Query("SELECT * FROM doctors_notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchDoctorNotes(query: String): Flow<List<DoctorNote>>
} 