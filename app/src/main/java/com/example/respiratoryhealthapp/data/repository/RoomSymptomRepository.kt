package com.example.respiratoryhealthapp.data.repository

import com.example.respiratoryhealthapp.data.dao.SymptomDao
import com.example.respiratoryhealthapp.data.model.Symptom
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class RoomSymptomRepository(private val symptomDao: SymptomDao) : SymptomRepository {
    override val allSymptoms: Flow<List<Symptom>> = symptomDao.getAllSymptoms()

    override fun getSymptomsByType(type: String): Flow<List<Symptom>> {
        return symptomDao.getSymptomsByType(type)
    }

    override fun getSymptomsBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Symptom>> {
        return symptomDao.getSymptomsBetweenDates(startDate, endDate)
    }

    override suspend fun getSymptomById(id: Long): Symptom? {
        return symptomDao.getSymptomById(id)
    }

    override suspend fun insertSymptom(symptom: Symptom): Long {
        return symptomDao.insertSymptom(symptom)
    }

    override suspend fun updateSymptom(symptom: Symptom) {
        symptomDao.updateSymptom(symptom)
    }

    override suspend fun deleteSymptom(symptom: Symptom) {
        symptomDao.deleteSymptom(symptom)
    }

    override suspend fun deleteAllSymptoms() {
        symptomDao.deleteAllSymptoms()
    }

    override fun searchSymptoms(query: String): Flow<List<Symptom>> {
        return symptomDao.searchSymptoms(query)
    }
} 