package com.example.respiratoryhealthapp.data.repository

import com.example.respiratoryhealthapp.data.dao.TestResultDao
import com.example.respiratoryhealthapp.data.model.TestResult
import kotlinx.coroutines.flow.Flow

class RoomTestResultRepository(private val testResultDao: TestResultDao) : TestResultRepository {

    override fun getAllTestResults(): Flow<List<TestResult>> = testResultDao.getAllTestResults()

    override fun getTestResultById(id: Long): Flow<TestResult?> = testResultDao.getTestResultById(id)

    override suspend fun insertTestResult(testResult: TestResult): Long = testResultDao.insertTestResult(testResult)

    override suspend fun updateTestResult(testResult: TestResult) = testResultDao.updateTestResult(testResult)

    override suspend fun deleteTestResult(testResult: TestResult) = testResultDao.deleteTestResult(testResult)

    override fun getTestResultsByName(testName: String): Flow<List<TestResult>> = testResultDao.getTestResultsByName(testName)
} 