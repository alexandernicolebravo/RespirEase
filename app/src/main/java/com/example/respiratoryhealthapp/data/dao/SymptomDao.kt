package com.example.respiratoryhealthapp.data.dao

import androidx.room.*
import com.example.respiratoryhealthapp.data.model.Symptom
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface SymptomDao {
    @Query("SELECT * FROM symptoms ORDER BY timestamp DESC")
    fun getAllSymptoms(): Flow<List<Symptom>>

    @Query("SELECT * FROM symptoms WHERE type = :type ORDER BY timestamp DESC")
    fun getSymptomsByType(type: String): Flow<List<Symptom>>

    @Query("SELECT * FROM symptoms WHERE timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getSymptomsBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Symptom>>

    @Query("SELECT * FROM symptoms WHERE id = :id")
    suspend fun getSymptomById(id: Long): Symptom?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptom(symptom: Symptom): Long

    @Update
    suspend fun updateSymptom(symptom: Symptom)

    @Delete
    suspend fun deleteSymptom(symptom: Symptom)

    @Query("SELECT * FROM symptoms WHERE notes LIKE '%' || :query || '%' OR trigger LIKE '%' || :query || '%'")
    fun searchSymptoms(query: String): Flow<List<Symptom>>

    @Query("DELETE FROM symptoms")
    suspend fun deleteAllSymptoms()
} 