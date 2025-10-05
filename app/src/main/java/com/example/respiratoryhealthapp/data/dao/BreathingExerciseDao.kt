package com.example.respiratoryhealthapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.respiratoryhealthapp.data.model.BreathingExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface BreathingExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: BreathingExercise): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<BreathingExercise>)

    @Query("SELECT * FROM breathing_exercises ORDER BY name ASC")
    fun getExercises(): Flow<List<BreathingExercise>>

    @Query("SELECT * FROM breathing_exercises WHERE id = :id")
    fun getExerciseById(id: Long): Flow<BreathingExercise?>

    // Query to get exercises by condition. This is more complex due to List<RespiratoryCondition>.
    // We'll store it as a comma-separated string and use LIKE. This isn't the most efficient
    // for large datasets but is okay for a moderate number of exercises.
    // The :conditionName parameter should be an enum's .name property.
    @Query("SELECT * FROM breathing_exercises WHERE targetConditions LIKE '%' || :conditionName || '%' ORDER BY name ASC")
    fun getExercisesByConditionName(conditionName: String): Flow<List<BreathingExercise>>
} 