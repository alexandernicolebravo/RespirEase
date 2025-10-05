package com.example.respiratoryhealthapp.screens.testresults

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.respiratoryhealthapp.data.database.AppDatabase
import com.example.respiratoryhealthapp.data.model.TestResult
import com.example.respiratoryhealthapp.data.repository.RoomTestResultRepository
import com.example.respiratoryhealthapp.data.repository.TestResultRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TestResultsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TestResultRepository

    private val _uiState = MutableStateFlow(TestResultsUiState())
    val uiState: StateFlow<TestResultsUiState> = _uiState.asStateFlow()

    // Flow for search query
    private val _searchQuery = MutableStateFlow("")

    init {
        val testResultDao = AppDatabase.getDatabase(application).testResultDao()
        repository = RoomTestResultRepository(testResultDao)

        viewModelScope.launch {
            _searchQuery.flatMapLatest { query ->
                if (query.isBlank()) {
                    repository.getAllTestResults()
                } else {
                    repository.getAllTestResults().transformLatest { allResults ->
                        val filtered = allResults.filter { result ->
                            result.testName.contains(query, ignoreCase = true) ||
                            result.value.contains(query, ignoreCase = true) ||
                            result.notes?.contains(query, ignoreCase = true) == true
                        }
                        emit(filtered)
                    }
                }
            }
            .onStart { _uiState.update { it.copy(isLoading = true, error = null, searchQuery = _searchQuery.value) } }
            .catch { e ->
                _uiState.update { it.copy(isLoading = false, error = "Failed to load test results: ${e.message}") }
            }
            .collect { results ->
                 _uiState.update {
                    it.copy(
                        testResults = results,
                        isLoading = false,
                        searchQuery = _searchQuery.value
                    )
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addTestResult(testResult: TestResult) {
        viewModelScope.launch {
            repository.insertTestResult(testResult)
        }
    }

    fun updateTestResult(testResult: TestResult) {
        viewModelScope.launch {
            repository.updateTestResult(testResult)
        }
    }

    fun deleteTestResult(testResult: TestResult) {
        viewModelScope.launch {
            repository.deleteTestResult(testResult)
        }
    }
} 