package com.example.respiratoryhealthapp.screens.reminders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.respiratoryhealthapp.data.model.Reminder
import com.example.respiratoryhealthapp.data.model.ReminderRecurrence
import com.example.respiratoryhealthapp.data.model.ReminderType
import com.example.respiratoryhealthapp.screens.reminders.components.AddEditReminderDialog
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    navController: NavController,
    viewModel: RemindersViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var reminderToDelete by remember { mutableStateOf<Reminder?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminders") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                viewModel.selectReminder(null)
                showAddEditDialog = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp, bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else if (uiState.error != null) {
                    Text(
                        text = uiState.error ?: "An unknown error occurred",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else if (uiState.reminders.isEmpty()) {
                    Text(
                        text = "No reminders set yet. Tap + to add.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    ReminderList(
                        reminders = uiState.reminders,
                        viewModel = viewModel,
                        onEditReminder = {
                            viewModel.selectReminder(it)
                            showAddEditDialog = true
                        }
                    )
                }
            }
        }

        if (showAddEditDialog) {
            AddEditReminderDialog(
                reminderToEdit = uiState.selectedReminder,
                onDismiss = { 
                    showAddEditDialog = false 
                    viewModel.selectReminder(null)
                },
                onConfirm = { reminder ->
                    if (reminder.id == 0L) {
                        viewModel.addReminder(reminder)
                    } else {
                        viewModel.updateReminder(reminder)
                    }
                    showAddEditDialog = false
                    viewModel.selectReminder(null)
                },
                onDelete = { reminder ->
                    reminderToDelete = reminder
                    showAddEditDialog = false
                    showDeleteConfirmDialog = true
                }
            )
        }

        if (showDeleteConfirmDialog && reminderToDelete != null) {
            ConfirmationDialog(
                title = "Delete Reminder",
                message = "Are you sure you want to delete the reminder \"${reminderToDelete?.title}\"?",
                onConfirm = {
                    reminderToDelete?.let { viewModel.deleteReminder(it) }
                    showDeleteConfirmDialog = false
                    reminderToDelete = null
                },
                onDismiss = {
                    showDeleteConfirmDialog = false
                    reminderToDelete = null
                }
            )
        }
    }
}

@Composable
fun ReminderList(
    reminders: List<Reminder>,
    viewModel: RemindersViewModel,
    onEditReminder: (Reminder) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(reminders) { reminder ->
            ReminderItem(
                reminder = reminder,
                onToggleActive = { newActiveState ->
                    viewModel.toggleReminderActiveState(reminder, newActiveState)
                },
                onItemClick = {
                    onEditReminder(reminder)
                }
            )
        }
    }
}

@Composable
fun ReminderItem(
    reminder: Reminder,
    onToggleActive: (Boolean) -> Unit,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(reminder.title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${reminder.type.displayName} - ${reminder.recurrence.displayName}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = reminder.dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(
                checked = reminder.isActive,
                onCheckedChange = onToggleActive
            )
        }
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// --- Previews --- //

@Preview(name = "Reminders Screen - Empty", showBackground = true)
@Composable
fun RemindersScreenPreview_Empty() {
    // com.example.respiratoryhealthapp.ui.theme.RespiratoryHealthAppTheme {
        RemindersScreenWithState(
            navController = rememberNavController(),
            uiState = RemindersUiState(reminders = emptyList(), isLoading = false, error = null),
            onToggleActive = { _, _ -> }
        )
    // }
}

@Preview(name = "Reminders Screen - With Data", showBackground = true)
@Composable
fun RemindersScreenPreview_WithData() {
    val sampleReminders = listOf(
        Reminder(1, "Morning Meds", "Take with water", LocalDateTime.now().plusHours(1), ReminderType.MEDICATION, ReminderRecurrence.DAILY, true),
        Reminder(2, "Evening Walk", null, LocalDateTime.now().plusHours(8), ReminderType.EXERCISE, ReminderRecurrence.NONE, true),
        Reminder(3, "Doctor Appointment", "Dr. Smith", LocalDateTime.now().plusDays(2).withHour(10).withMinute(0), ReminderType.APPOINTMENT, ReminderRecurrence.NONE, false)
    )
    // com.example.respiratoryhealthapp.ui.theme.RespiratoryHealthAppTheme {
        RemindersScreenWithState(
            navController = rememberNavController(),
            uiState = RemindersUiState(reminders = sampleReminders, isLoading = false, error = null),
            onToggleActive = { _, _ -> }
        )
    // }
}

@Preview(name = "Reminders Screen - Loading", showBackground = true)
@Composable
fun RemindersScreenPreview_Loading() {
    // com.example.respiratoryhealthapp.ui.theme.RespiratoryHealthAppTheme {
        RemindersScreenWithState(
            navController = rememberNavController(),
            uiState = RemindersUiState(isLoading = true),
            onToggleActive = { _, _ -> }
        )
    // }
}

@Preview(name = "Reminders Screen - Error", showBackground = true)
@Composable
fun RemindersScreenPreview_Error() {
    // com.example.respiratoryhealthapp.ui.theme.RespiratoryHealthAppTheme {
        RemindersScreenWithState(
            navController = rememberNavController(),
            uiState = RemindersUiState(error = "Failed to load reminders!"),
            onToggleActive = { _, _ -> }
        )
    // }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemindersScreenWithState(
    navController: NavController,
    uiState: RemindersUiState,
    onToggleActive: (Reminder, Boolean) -> Unit = { _, _ -> }
) {
    var showAddEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var reminderToEditOrDelete by remember { mutableStateOf<Reminder?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminders") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                reminderToEditOrDelete = null
                showAddEditDialog = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp, bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else if (uiState.error != null) {
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else if (uiState.reminders.isEmpty()) {
                    Text(
                        text = "No reminders set yet. Tap + to add.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.reminders) { reminder ->
                            ReminderItem(
                                reminder = reminder,
                                onToggleActive = { newActiveState ->
                                    onToggleActive(reminder, newActiveState)
                                },
                                onItemClick = {
                                    reminderToEditOrDelete = reminder
                                    showAddEditDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showAddEditDialog) {
            AddEditReminderDialog(
                reminderToEdit = reminderToEditOrDelete,
                onDismiss = { showAddEditDialog = false },
                onConfirm = { _ -> showAddEditDialog = false },
                onDelete = { _ -> 
                    showAddEditDialog = false 
                    showDeleteConfirmDialog = true 
                }
            )
        }

        if (showDeleteConfirmDialog && reminderToEditOrDelete != null) {
            ConfirmationDialog(
                title = "Delete Reminder",
                message = "Are you sure you want to delete \"${reminderToEditOrDelete?.title}\"?",
                onConfirm = {
                    showDeleteConfirmDialog = false
                    reminderToEditOrDelete = null
                },
                onDismiss = {
                    showDeleteConfirmDialog = false
                    reminderToEditOrDelete = null
                }
            )
        }
    }
} 