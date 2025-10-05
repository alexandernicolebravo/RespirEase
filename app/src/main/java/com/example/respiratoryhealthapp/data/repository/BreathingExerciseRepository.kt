package com.example.respiratoryhealthapp.data.repository

import com.example.respiratoryhealthapp.data.model.BreathingExercise
import kotlinx.coroutines.flow.Flow

interface BreathingExerciseRepository {
    fun getExercises(): Flow<List<BreathingExercise>>
    fun getExercisesByCondition(condition: String): Flow<List<BreathingExercise>>
    suspend fun insertExercise(exercise: BreathingExercise)
    fun getExerciseById(id: Long): Flow<BreathingExercise?>
} 