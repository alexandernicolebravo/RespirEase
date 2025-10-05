package com.example.respiratoryhealthapp.screens.testresults

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.respiratoryhealthapp.data.model.TestResult
import com.example.respiratoryhealthapp.screens.testresults.components.AddEditTestResultDialog
import com.example.respiratoryhealthapp.screens.testresults.components.TestResultCard
import com.example.respiratoryhealthapp.ui.components.dialogs.ConfirmationDialog
import com.example.respiratoryhealthapp.ui.theme.RespiratoryHealthAppTheme
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestResultsScreen(
    navController: NavController,
    viewModel: TestResultsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddEditDialog by remember { mutableStateOf(false) }
    var testResultToEdit by remember { mutableStateOf<TestResult?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var testResultToDelete by remember { mutableStateOf<TestResult?>(null) }

    var searchQuery by remember { mutableStateOf(TextFieldValue(uiState.searchQuery)) }
    var isSearchActive by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.searchQuery) {
        if (searchQuery.text != uiState.searchQuery) {
            searchQuery = TextFieldValue(uiState.searchQuery)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test Results") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                viewModel.setSearchQuery(it.text)
                            },
                            placeholder = { Text("Search Results...") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                            ),
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (searchQuery.text.isNotEmpty()) {
                                        searchQuery = TextFieldValue("")
                                        viewModel.setSearchQuery("")
                                    } else {
                                        isSearchActive = false
                                        viewModel.setSearchQuery("")
                                    }
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear or close search")
                                }
                            }
                        )
                    } else {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search Test Results")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                testResultToEdit = null
                showAddEditDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Test Result")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error ?: "An unknown error occurred",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.testResults.isEmpty()) {
                Text(
                    text = if (uiState.searchQuery.isNotBlank()) "No results found for '${uiState.searchQuery}'" else "No test results recorded yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else {
                TestResultList(
                    testResults = uiState.testResults,
                    onItemClick = { result ->
                        testResultToEdit = result
                        showAddEditDialog = true
                    }
                )
            }
        }
    }

    if (showAddEditDialog) {
        AddEditTestResultDialog(
            testResultToEdit = testResultToEdit,
            onDismiss = { showAddEditDialog = false },
            onConfirm = { result ->
                if (testResultToEdit == null) viewModel.addTestResult(result) else viewModel.updateTestResult(result)
                showAddEditDialog = false
            },
            onDelete = if (testResultToEdit != null) {
                { result ->
                    testResultToDelete = result
                    showAddEditDialog = false
                    showDeleteDialog = true
                }
            } else null
        )
    }

    if (showDeleteDialog && testResultToDelete != null) {
        ConfirmationDialog(
            title = "Delete Test Result",
            message = "Are you sure you want to delete the test result for '${testResultToDelete!!.testName}' recorded on ${testResultToDelete!!.timestamp.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"))}?",
            confirmButtonText = "Delete",
            onConfirm = {
                viewModel.deleteTestResult(testResultToDelete!!)
                showDeleteDialog = false
                testResultToDelete = null
            },
            onDismiss = {
                showDeleteDialog = false
                testResultToDelete = null
            }
        )
    }
}

@Composable
fun TestResultList(
    testResults: List<TestResult>,
    onItemClick: (TestResult) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(testResults, key = { it.id }) { result ->
            TestResultCard(
                testResult = result,
                onClick = { onItemClick(result) }
            )
        }
    }
}

@Preview(showBackground = true, name = "Test Results Screen - With Data")
@Composable
fun TestResultsScreenPreviewWithData() {
    RespiratoryHealthAppTheme {
        TestResultsScreenForPreview(
            uiState = TestResultsUiState(
                testResults = listOf(
                    TestResult(id = 1L, timestamp = LocalDateTime.now().minusDays(1), testName = "Peak Flow", value = "450", unit = "L/min", notes = "Morning check"),
                    TestResult(id = 2L, timestamp = LocalDateTime.now().minusHours(5), testName = "Oxygen Saturation", value = "98", unit = "%")
                )
            )
        )
    }
}

@Preview(showBackground = true, name = "Test Results Screen - Empty")
@Composable
fun TestResultsScreenPreviewEmpty() {
    RespiratoryHealthAppTheme {
        TestResultsScreenForPreview(uiState = TestResultsUiState(testResults = emptyList()))
    }
}

@Preview(showBackground = true, name = "Test Results Screen - Loading")
@Composable
fun TestResultsScreenPreviewLoading() {
    RespiratoryHealthAppTheme {
        TestResultsScreenForPreview(uiState = TestResultsUiState(isLoading = true))
    }
}

@Preview(showBackground = true, name = "Test Results Screen - Error")
@Composable
fun TestResultsScreenPreviewError() {
    RespiratoryHealthAppTheme {
        TestResultsScreenForPreview(uiState = TestResultsUiState(error = "Failed to load results."))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TestResultsScreenForPreview(uiState: TestResultsUiState) {
    var showAddEditDialog by remember { mutableStateOf(false) }
    var testResultToEdit by remember { mutableStateOf<TestResult?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var testResultToDelete by remember { mutableStateOf<TestResult?>(null) }
    var searchQuery by remember { mutableStateOf(TextFieldValue(uiState.searchQuery)) }
    var isSearchActive by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test Results") },
                navigationIcon = {
                    IconButton(onClick = { /* No-op for preview */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search Results...") },
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { isSearchActive = false; searchQuery = TextFieldValue("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear or close search")
                                }
                            }
                        )
                    } else {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search Test Results")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { testResultToEdit = null; showAddEditDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Test Result")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(uiState.error, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
            } else if (uiState.testResults.isEmpty()) {
                Text(
                    if (uiState.searchQuery.isNotBlank()) "No results for '${uiState.searchQuery}'" else "No test results yet.",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else {
                TestResultList(
                    testResults = uiState.testResults,
                    onItemClick = { result ->
                        testResultToEdit = result
                        showAddEditDialog = true
                    }
                )
            }
        }
    }

    if (showAddEditDialog) {
        AddEditTestResultDialog(
            testResultToEdit = testResultToEdit,
            onDismiss = { showAddEditDialog = false },
            onConfirm = { /* Store locally for preview if needed, or no-op */ showAddEditDialog = false },
            onDelete = if (testResultToEdit != null) { { result -> testResultToDelete = result; showAddEditDialog = false; showDeleteDialog = true } } else null
        )
    }

    if (showDeleteDialog && testResultToDelete != null) {
        ConfirmationDialog(
            title = "Delete Test Result",
            message = "Delete '${testResultToDelete!!.testName}'?",
            confirmButtonText = "Delete",
            onConfirm = { showDeleteDialog = false; testResultToDelete = null },
            onDismiss = { showDeleteDialog = false; testResultToDelete = null }
        )
    }
} 