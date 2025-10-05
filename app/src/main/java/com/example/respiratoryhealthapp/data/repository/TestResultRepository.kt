package com.example.respiratoryhealthapp.data.repository

import com.example.respiratoryhealthapp.data.model.TestResult
import kotlinx.coroutines.flow.Flow

interface TestResultRepository {
    fun getAllTestResults(): Flow<List<TestResult>>
    fun getTestResultById(id: Long): Flow<TestResult?>
    suspend fun insertTestResult(testResult: TestResult): Long
    suspend fun updateTestResult(testResult: TestResult)
    suspend fun deleteTestResult(testResult: TestResult)
    fun getTestResultsByName(testName: String): Flow<List<TestResult>>
} 