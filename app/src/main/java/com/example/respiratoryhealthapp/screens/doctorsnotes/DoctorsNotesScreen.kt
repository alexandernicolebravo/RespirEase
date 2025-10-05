package com.example.respiratoryhealthapp.screens.doctorsnotes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.respiratoryhealthapp.data.model.DoctorNote
import com.example.respiratoryhealthapp.screens.doctorsnotes.components.AddEditDoctorNoteDialog
import com.example.respiratoryhealthapp.ui.components.dialogs.ConfirmationDialog
import com.example.respiratoryhealthapp.ui.theme.RespiratoryHealthAppTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorsNotesScreen(
    navController: NavController,
    viewModel: DoctorsNotesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddEditDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<DoctorNote?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<DoctorNote?>(null) }

    var searchQuery by remember { mutableStateOf(TextFieldValue(uiState.searchQuery)) }
    var isSearchActive by remember { mutableStateOf(false) }

    // Update search query in TextField when ViewModel's query changes (e.g. on clear from TopAppBar)
    LaunchedEffect(uiState.searchQuery) {
        if (searchQuery.text != uiState.searchQuery) {
            searchQuery = TextFieldValue(uiState.searchQuery)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Doctor's Notes") },
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
                            placeholder = { Text("Search Notes...") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
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
                            Icon(Icons.Default.Search, contentDescription = "Search Notes")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                noteToEdit = null
                showAddEditDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Doctor's Note")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error ?: "An unknown error occurred",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.notes.isEmpty()) {
                Text(
                    text = if (uiState.searchQuery.isNotBlank()) "No notes found for '${uiState.searchQuery}'" else "No doctor's notes recorded yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp).align(Alignment.Center)
                )
            } else {
                DoctorNoteList(
                    notes = uiState.notes,
                    onEditClick = {
                        noteToEdit = it
                        showAddEditDialog = true
                    },
                    onDeleteClick = {
                        noteToDelete = it
                        showDeleteDialog = true
                    }
                )
            }
        }
    }

    if (showAddEditDialog) {
        AddEditDoctorNoteDialog(
            noteToEdit = noteToEdit,
            onDismiss = { showAddEditDialog = false },
            onConfirm = {
                if (noteToEdit == null) viewModel.addDoctorNote(it) else viewModel.updateDoctorNote(it)
                showAddEditDialog = false
            },
            onDelete = if (noteToEdit != null) {
                { note ->
                    noteToDelete = note
                    showAddEditDialog = false
                    showDeleteDialog = true
                }
            } else null
        )
    }

    if (showDeleteDialog && noteToDelete != null) {
        ConfirmationDialog(
            title = "Delete Doctor's Note",
            message = "Are you sure you want to delete this note${noteToDelete?.title?.takeIf { it.isNotBlank() }?.let { "\" titled '$it'" } ?: ""}?",
            onConfirm = {
                viewModel.deleteDoctorNote(noteToDelete!!)
                showDeleteDialog = false
                noteToDelete = null
            },
            onDismiss = {
                showDeleteDialog = false
                noteToDelete = null
            }
        )
    }
}

@Composable
fun DoctorNoteList(
    notes: List<DoctorNote>,
    onEditClick: (DoctorNote) -> Unit,
    onDeleteClick: (DoctorNote) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(notes, key = { it.id }) { note ->
            DoctorNoteCard(
                note = note,
                onClick = { onEditClick(note) }, // Single click to edit
                onLongClick = { onDeleteClick(note) } // Long click to trigger delete confirmation
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DoctorNoteCard(
    note: DoctorNote,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            if (!note.title.isNullOrBlank()) {
                Text(note.title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recorded: ${note.timestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                note.doctorName?.let {
                    if (it.isNotBlank()) {
                        Text(
                            text = "Dr. $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            note.nextAppointment?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Next Appt: ${it.format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary // Highlight appointment
                )
            }
        }
    }
}

// --- Previews --- //

@Preview(showBackground = true, name = "Doctors Notes Screen - With Data")
@Composable
fun DoctorsNotesScreenPreviewWithData() {
    RespiratoryHealthAppTheme {
        DoctorsNotesScreenForPreview(uiState = DoctorsNotesUiState(
            notes = listOf(
                DoctorNote(id = 1, title = "Checkup", content = "Regular checkup, all good.", timestamp = LocalDateTime.now().minusDays(5), doctorName = "Dr. Feelgood", nextAppointment = LocalDateTime.now().plusMonths(3)),
                DoctorNote(id = 2, title = "Follow-up on cough", content = "Prescribed new inhaler. Symptoms improving. Continue monitoring.", timestamp = LocalDateTime.now().minusWeeks(1), doctorName = "Dr. John Doe"),
                DoctorNote(id = 3, content = "Quick consultation about medication side effects. Adjusted dosage.", timestamp = LocalDateTime.now().minusDays(2))
            )
        ))
    }
}

@Preview(showBackground = true, name = "Doctors Notes Screen - Empty")
@Composable
fun DoctorsNotesScreenPreviewEmpty() {
    RespiratoryHealthAppTheme {
        DoctorsNotesScreenForPreview(uiState = DoctorsNotesUiState(notes = emptyList()))
    }
}

@Preview(showBackground = true, name = "Doctors Notes Screen - Loading")
@Composable
fun DoctorsNotesScreenPreviewLoading() {
    RespiratoryHealthAppTheme {
        DoctorsNotesScreenForPreview(uiState = DoctorsNotesUiState(isLoading = true))
    }
}

@Preview(showBackground = true, name = "Doctors Notes Screen - Error")
@Composable
fun DoctorsNotesScreenPreviewError() {
    RespiratoryHealthAppTheme {
        DoctorsNotesScreenForPreview(uiState = DoctorsNotesUiState(error = "Failed to load notes"))
    }
}

@Preview(showBackground = true, name = "Doctors Notes Screen - Search No Results")
@Composable
fun DoctorsNotesScreenPreviewSearchNoResults() {
    RespiratoryHealthAppTheme {
        DoctorsNotesScreenForPreview(uiState = DoctorsNotesUiState(notes = emptyList(), searchQuery = "nonexistent"))
    }
}


/**
 * A stateful preview helper for DoctorsNotesScreen. 
 * This avoids needing a NavController and ViewModel for basic UI previews.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DoctorsNotesScreenForPreview(uiState: DoctorsNotesUiState) {
    var showAddEditDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<DoctorNote?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<DoctorNote?>(null) }
    var searchQuery by remember { mutableStateOf(TextFieldValue(uiState.searchQuery)) }
    var isSearchActive by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Doctor's Notes") },
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
                            placeholder = { Text("Search Notes...") },
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            singleLine = true,
                             trailingIcon = { IconButton(onClick = { isSearchActive = false }) { Icon(Icons.Default.Close, "Close") } }
                        )
                    } else {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search Notes")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                noteToEdit = null
                showAddEditDialog = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Doctor's Note")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.error != null) {
                Text(uiState.error, color = MaterialTheme.colorScheme.error)
            } else if (uiState.notes.isEmpty()) {
                Text(if (uiState.searchQuery.isNotBlank()) "No notes for '${uiState.searchQuery}'" else "No notes yet.")
            } else {
                DoctorNoteList(
                    notes = uiState.notes,
                    onEditClick = { noteToEdit = it; showAddEditDialog = true },
                    onDeleteClick = { noteToDelete = it; showDeleteDialog = true }
                )
            }
        }
    }

    if (showAddEditDialog) {
        AddEditDoctorNoteDialog(
            noteToEdit = noteToEdit,
            onDismiss = { showAddEditDialog = false },
            onConfirm = { showAddEditDialog = false /* No-op for preview */ },
            onDelete = if (noteToEdit != null) {
                { note ->
                    noteToDelete = note
                    showAddEditDialog = false
                    showDeleteDialog = true
                }
            } else null
        )
    }

    if (showDeleteDialog && noteToDelete != null) {
        ConfirmationDialog(
            title = "Delete Note?",
            message = "Are you sure?",
            onConfirm = { 
                showDeleteDialog = false
                noteToDelete = null 
            },
            onDismiss = { 
                showDeleteDialog = false
                noteToDelete = null 
            }
        )
    }
} 