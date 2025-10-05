package com.example.respiratoryhealthapp.screens.symptomdiary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.respiratoryhealthapp.data.model.Symptom
import com.example.respiratoryhealthapp.data.model.SymptomType
import com.example.respiratoryhealthapp.screens.symptomdiary.components.DateRangePicker
import com.example.respiratoryhealthapp.screens.symptomdiary.components.SymptomCard
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomDiaryScreen(
    navController: NavController,
    viewModel: SymptomDiaryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showDateRangeDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }
    var editingSymptom by remember { mutableStateOf<Symptom?>(null) }
    var deletingSymptom by remember { mutableStateOf<Symptom?>(null) }
    var showSeverityFilterMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Symptom Diary") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Search
                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }

                    // Sort
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Date (Newest First)") },
                                onClick = {
                                    viewModel.setSortOrder(SortOrder.DATE_DESC)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Date (Oldest First)") },
                                onClick = {
                                    viewModel.setSortOrder(SortOrder.DATE_ASC)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Severity (High to Low)") },
                                onClick = {
                                    viewModel.setSortOrder(SortOrder.SEVERITY_DESC)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Severity (Low to High)") },
                                onClick = {
                                    viewModel.setSortOrder(SortOrder.SEVERITY_ASC)
                                    showSortMenu = false
                                }
                            )
                        }
                    }

                    // Filter
                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(Icons.Filled.FilterList, contentDescription = "Filter by Type/Date")
                        }
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Symptoms") },
                                onClick = {
                                    viewModel.filterSymptomsByType(null)
                                    showFilterMenu = false
                                }
                            )
                            SymptomType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = {
                                        Text(type.name.replace("_", " ").lowercase()
                                            .replaceFirstChar { it.uppercase() })
                                    },
                                    onClick = {
                                        viewModel.filterSymptomsByType(type)
                                        showFilterMenu = false
                                    }
                                )
                            }
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Date Range") },
                                onClick = {
                                    showDateRangeDialog = true
                                    showFilterMenu = false
                                }
                            )
                        }
                    }

                    // Severity Filter
                    Box {
                        IconButton(onClick = { showSeverityFilterMenu = true }) {
                            Icon(Icons.Filled.Star, contentDescription = "Filter by Severity")
                        }
                        DropdownMenu(
                            expanded = showSeverityFilterMenu,
                            onDismissRequest = { showSeverityFilterMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "All Severities",
                                        color = if (uiState.selectedSeverity == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    viewModel.setSeverityFilter(null)
                                    showSeverityFilterMenu = false
                                }
                            )
                            (1..5).forEach { severity ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Severity $severity",
                                            color = if (uiState.selectedSeverity == severity) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.setSeverityFilter(severity)
                                        showSeverityFilterMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Add
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Symptom")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Error message
                    uiState.error?.let { error ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                IconButton(onClick = { viewModel.clearError() }) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Dismiss",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }

                    // Search Bar
                    if (showSearchBar) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                viewModel.setSearchQuery(it)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                            placeholder = { Text("Search in notes, triggers...") },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        viewModel.setSearchQuery("")
                                    }) {
                                        Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                                    }
                                }
                            },
                            singleLine = true
                        )
                    }

                    // Empty state
                    if (uiState.symptoms.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "No symptoms logged yet",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Button(
                                    onClick = { showAddDialog = true }
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add First Symptom")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.symptoms) { symptom ->
                                SymptomCard(
                                    symptom = symptom,
                                    onDelete = { deletingSymptom = symptom },
                                    onEdit = { editingSymptom = symptom }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSymptomDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { type, severity, notes, trigger, peakFlow ->
                viewModel.addSymptom(type, severity, notes, trigger, peakFlow)
                showAddDialog = false
            }
        )
    }

    if (showDateRangeDialog) {
        DateRangeDialog(
            onDismiss = { showDateRangeDialog = false },
            onConfirm = { start, end ->
                viewModel.setDateRange(start, end)
                showDateRangeDialog = false
            }
        )
    }

    if (editingSymptom != null) {
        EditSymptomDialog(
            symptom = editingSymptom!!,
            onDismiss = { editingSymptom = null },
            onConfirm = { updatedSymptom ->
                viewModel.updateSymptom(updatedSymptom)
                editingSymptom = null
            }
        )
    }

    if (deletingSymptom != null) {
        AlertDialog(
            onDismissRequest = { deletingSymptom = null },
            title = { Text("Delete Symptom") },
            text = { Text("Are you sure you want to delete this symptom entry?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deletingSymptom?.let { viewModel.deleteSymptom(it) }
                        deletingSymptom = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingSymptom = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DateRangeDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalDateTime?, LocalDateTime?) -> Unit
) {
    var startDate by remember { mutableStateOf<LocalDateTime?>(null) }
    var endDate by remember { mutableStateOf<LocalDateTime?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Date Range") },
        text = {
            DateRangePicker(
                startDate = startDate,
                endDate = endDate,
                onStartDateChange = { startDate = it },
                onEndDateChange = { endDate = it },
                modifier = Modifier.padding(16.dp)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(startDate, endDate) },
                enabled = startDate != null && endDate != null
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddSymptomDialog(
    onDismiss: () -> Unit,
    onConfirm: (SymptomType, Int, String?, String?, Int?) -> Unit
) {
    var selectedType by remember { mutableStateOf<SymptomType?>(null) }
    var severity by remember { mutableIntStateOf(3) }
    var notes by remember { mutableStateOf("") }
    var trigger by remember { mutableStateOf("") }
    var peakFlow by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Symptom") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box {
                    OutlinedTextField(
                        value = selectedType?.name?.replace("_", " ")?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Symptom Type") },
                        trailingIcon = {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(
                                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = if (expanded) "Close menu" else "Open menu"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SymptomType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Text(type.name.replace("_", " ").lowercase()
                                        .replaceFirstChar { it.uppercase() })
                                },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Column {
                    Text("Severity: $severity")
                    Slider(
                        value = severity.toFloat(),
                        onValueChange = { severity = it.toInt() },
                        valueRange = 1f..5f,
                        steps = 3
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = trigger,
                    onValueChange = { trigger = it },
                    label = { Text("Trigger (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = peakFlow,
                    onValueChange = { peakFlow = it },
                    label = { Text("Peak Flow (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedType?.let { type ->
                        onConfirm(
                            type,
                            severity,
                            notes.takeIf { it.isNotBlank() },
                            trigger.takeIf { it.isNotBlank() },
                            peakFlow.toIntOrNull()
                        )
                    }
                },
                enabled = selectedType != null
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditSymptomDialog(
    symptom: Symptom,
    onDismiss: () -> Unit,
    onConfirm: (Symptom) -> Unit
) {
    var selectedType by remember { mutableStateOf(symptom.type) }
    var severity by remember { mutableIntStateOf(symptom.severity) }
    var notes by remember { mutableStateOf(symptom.notes ?: "") }
    var trigger by remember { mutableStateOf(symptom.trigger ?: "") }
    var peakFlow by remember { mutableStateOf(symptom.peakFlow?.toString() ?: "") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Symptom") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box {
                    OutlinedTextField(
                        value = selectedType.name.replace("_", " ").lowercase()
                            .replaceFirstChar { it.uppercase() },
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Symptom Type") },
                        trailingIcon = {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(
                                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = if (expanded) "Close menu" else "Open menu"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SymptomType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Text(type.name.replace("_", " ").lowercase()
                                        .replaceFirstChar { it.uppercase() })
                                },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Column {
                    Text("Severity: $severity")
                    Slider(
                        value = severity.toFloat(),
                        onValueChange = { severity = it.toInt() },
                        valueRange = 1f..5f,
                        steps = 3
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = trigger,
                    onValueChange = { trigger = it },
                    label = { Text("Trigger (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = peakFlow,
                    onValueChange = { peakFlow = it },
                    label = { Text("Peak Flow (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        symptom.copy(
                            type = selectedType,
                            severity = severity,
                            notes = notes.takeIf { it.isNotBlank() },
                            trigger = trigger.takeIf { it.isNotBlank() },
                            peakFlow = peakFlow.toIntOrNull()
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun SymptomDiaryScreenPreview() {
    MaterialTheme {
        val previewSymptoms = remember {
            listOf(
                Symptom(
                    id = 1,
                    timestamp = LocalDateTime.now(),
                    type = SymptomType.COUGH,
                    severity = 3,
                    notes = "Sample cough symptom",
                    trigger = "Cold weather",
                    peakFlow = 450
                ),
                Symptom(
                    id = 2,
                    timestamp = LocalDateTime.now().minusHours(2),
                    type = SymptomType.SHORTNESS_OF_BREATH,
                    severity = 4,
                    notes = "Sample shortness of breath",
                    trigger = "Exercise",
                    peakFlow = 400
                )
            )
        }

        val previewUiState = remember {
            MutableStateFlow(SymptomDiaryUiState(
                symptoms = previewSymptoms,
                isLoading = false
            ))
        }

        val uiState by previewUiState.collectAsState()
        var showAddDialog by remember { mutableStateOf(false) }
        var showFilterMenu by remember { mutableStateOf(false) }
        var showSortMenu by remember { mutableStateOf(false) }
        var showDateRangeDialog by remember { mutableStateOf(false) }
        var showSearchBarPreview by remember { mutableStateOf(true) }
        var editingSymptom by remember { mutableStateOf<Symptom?>(null) }
        var deletingSymptom by remember { mutableStateOf<Symptom?>(null) }
        var showSeverityFilterMenu by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Symptom Diary") },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Search
                        IconButton(onClick = { showSearchBarPreview = !showSearchBarPreview }) {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        }
                        
                        // Sort
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Date (Newest First)") },
                                    onClick = { showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Date (Oldest First)") },
                                    onClick = { showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Severity (High to Low)") },
                                    onClick = { showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Severity (Low to High)") },
                                    onClick = { showSortMenu = false }
                                )
                            }
                        }

                        // Filter
                        Box {
                            IconButton(onClick = { showFilterMenu = true }) {
                                Icon(Icons.Filled.FilterList, contentDescription = "Filter by Type/Date")
                            }
                            DropdownMenu(
                                expanded = showFilterMenu,
                                onDismissRequest = { showFilterMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Symptoms") },
                                    onClick = { showFilterMenu = false }
                                )
                                SymptomType.entries.forEach { type ->
                                    DropdownMenuItem(
                                        text = { 
                                            Text(type.name.replace("_", " ").lowercase()
                                                .replaceFirstChar { it.uppercase() }) 
                                        },
                                        onClick = { showFilterMenu = false }
                                    )
                                }
                                Divider()
                                DropdownMenuItem(
                                    text = { Text("Date Range") },
                                    onClick = {
                                        showDateRangeDialog = true
                                        showFilterMenu = false
                                    }
                                )
                            }
                        }

                        // Severity Filter
                        Box {
                            IconButton(onClick = { showSeverityFilterMenu = true }) {
                                Icon(Icons.Filled.Star, contentDescription = "Filter by Severity")
                            }
                            DropdownMenu(
                                expanded = showSeverityFilterMenu,
                                onDismissRequest = { showSeverityFilterMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "All Severities"
                                        )
                                    },
                                    onClick = {
                                        showSeverityFilterMenu = false
                                    }
                                )
                                (1..5).forEach { severity ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Severity $severity"
                                            )
                                        },
                                        onClick = {
                                            showSeverityFilterMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Add
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Filled.Add, contentDescription = "Add Symptom")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Error message (simplified for preview)
                    uiState.error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }

                    // Search Bar for Preview
                    if (showSearchBarPreview) {
                        var previewSearchQuery by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = previewSearchQuery,
                            onValueChange = { previewSearchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                            placeholder = { Text("Search symptoms...") },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            trailingIcon = {
                                if (previewSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { previewSearchQuery = "" }) {
                                        Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                                    }
                                }
                            },
                            singleLine = true
                        )
                    }

                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
                    } else if (uiState.symptoms.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "No symptoms logged yet",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Button(
                                    onClick = { showAddDialog = true }
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add First Symptom")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.symptoms) { symptom ->
                                SymptomCard(
                                    symptom = symptom,
                                    onDelete = { deletingSymptom = symptom },
                                    onEdit = { editingSymptom = symptom }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddSymptomDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { _, _, _, _, _ -> showAddDialog = false }
            )
        }

        if (showDateRangeDialog) {
            DateRangeDialog(
                onDismiss = { showDateRangeDialog = false },
                onConfirm = { _, _ -> showDateRangeDialog = false }
            )
        }

        if (editingSymptom != null) {
            EditSymptomDialog(
                symptom = editingSymptom!!,
                onDismiss = { editingSymptom = null },
                onConfirm = { editingSymptom = null }
            )
        }

        if (deletingSymptom != null) {
            AlertDialog(
                onDismissRequest = { deletingSymptom = null },
                title = { Text("Delete Symptom") },
                text = { Text("Are you sure you want to delete this symptom entry?") },
                confirmButton = {
                    TextButton(onClick = { deletingSymptom = null }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deletingSymptom = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddSymptomDialogPreview() {
    MaterialTheme {
        AddSymptomDialog(
            onDismiss = {},
            onConfirm = { _, _, _, _, _ -> }
        )
    }
} 