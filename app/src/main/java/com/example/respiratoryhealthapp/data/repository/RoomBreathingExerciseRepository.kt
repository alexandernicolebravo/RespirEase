package com.example.respiratoryhealthapp.data.repository

import com.example.respiratoryhealthapp.data.dao.BreathingExerciseDao
import com.example.respiratoryhealthapp.data.model.BreathingExercise
import kotlinx.coroutines.flow.Flow

class RoomBreathingExerciseRepository(
    private val exerciseDao: BreathingExerciseDao
) : BreathingExerciseRepository {
    override fun getExercises(): Flow<List<BreathingExercise>> =
        exerciseDao.getExercises()

    override fun getExercisesByCondition(condition: String): Flow<List<BreathingExercise>> =
        exerciseDao.getExercisesByConditionName(condition) // Ensuring this matches the DAO

    override suspend fun insertExercise(exercise: BreathingExercise) {
        exerciseDao.insert(exercise) // Ensuring this matches the DAO
    }

    override fun getExerciseById(id: Long): Flow<BreathingExercise?> {
        return exerciseDao.getExerciseById(id)
    }
    
    // It might be useful to have an insertAll for pre-population
    // suspend fun insertAllExercises(exercises: List<BreathingExercise>) {
    //     exerciseDao.insertAll(exercises)
    // }
} 