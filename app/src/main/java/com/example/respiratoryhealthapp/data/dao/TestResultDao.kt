package com.example.respiratoryhealthapp.data.dao

import androidx.room.*
import com.example.respiratoryhealthapp.data.model.TestResult
import kotlinx.coroutines.flow.Flow

@Dao
interface TestResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestResult(testResult: TestResult): Long

    @Update
    suspend fun updateTestResult(testResult: TestResult)

    @Delete
    suspend fun deleteTestResult(testResult: TestResult)

    @Query("SELECT * FROM test_results WHERE id = :id")
    fun getTestResultById(id: Long): Flow<TestResult?>

    @Query("SELECT * FROM test_results ORDER BY timestamp DESC")
    fun getAllTestResults(): Flow<List<TestResult>>

    // Optional: Query by test name if needed later
    @Query("SELECT * FROM test_results WHERE testName = :testName ORDER BY timestamp DESC")
    fun getTestResultsByName(testName: String): Flow<List<TestResult>>
} 