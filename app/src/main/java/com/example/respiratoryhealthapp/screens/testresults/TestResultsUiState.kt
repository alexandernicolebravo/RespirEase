package com.example.respiratoryhealthapp.screens.testresults

import com.example.respiratoryhealthapp.data.model.TestResult

data class TestResultsUiState(
    val testResults: List<TestResult> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    // val selectedTestResult: TestResult? = null, // Removed as unused
    val searchQuery: String = "",
    // Add other relevant UI state properties here, e.g., dialog visibility
) 