package com.example.respiratoryhealthapp.screens.doctorsnotes.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.respiratoryhealthapp.data.model.DoctorNote
import com.example.respiratoryhealthapp.ui.theme.RespiratoryHealthAppTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDoctorNoteDialog(
    noteToEdit: DoctorNote?,
    onDismiss: () -> Unit,
    onConfirm: (DoctorNote) -> Unit,
    onDelete: ((DoctorNote) -> Unit)? = null // Nullable if not editing
) {
    var title by remember { mutableStateOf(noteToEdit?.title ?: "") }
    var content by remember { mutableStateOf(noteToEdit?.content ?: "") }
    var doctorName by remember { mutableStateOf(noteToEdit?.doctorName ?: "") }
    var nextAppointmentDate by remember { mutableStateOf(noteToEdit?.nextAppointment?.toLocalDate()) }
    var nextAppointmentTime by remember { mutableStateOf(noteToEdit?.nextAppointment?.toLocalTime()) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (noteToEdit == null) "Add Doctor Note" else "Edit Doctor Note") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Note Content") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5
                )
                OutlinedTextField(
                    value = doctorName,
                    onValueChange = { doctorName = it },
                    label = { Text("Doctor's Name (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Next Appointment (Optional)", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = nextAppointmentDate?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: "Select Date",
                            onValueChange = {}, readOnly = true,
                            enabled = false,
                            label = { Text("Date") },
                            trailingIcon = { Icon(Icons.Default.DateRange, "Select Date") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showDatePicker = true }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = nextAppointmentTime?.format(DateTimeFormatter.ofPattern("hh:mm a")) ?: "Select Time",
                            onValueChange = {}, readOnly = true,
                            enabled = false,
                            label = { Text("Time") },
                            trailingIcon = { Icon(Icons.Default.Schedule, "Select Time") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showTimePicker = true }
                        )
                    }
                }
                 if (nextAppointmentDate != null || nextAppointmentTime != null) {
                    TextButton(onClick = {
                        nextAppointmentDate = null
                        nextAppointmentTime = null
                    }) {
                        Text("Clear Appointment")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (content.isNotBlank()) {
                        val nextAppDateTime = if (nextAppointmentDate != null && nextAppointmentTime != null) {
                            LocalDateTime.of(nextAppointmentDate!!, nextAppointmentTime!!)
                        } else if (nextAppointmentDate != null) {
                            LocalDateTime.of(nextAppointmentDate!!, LocalTime.MIDNIGHT) // Default time if only date is set
                        } else {
                            null
                        }

                        val note = DoctorNote(
                            id = noteToEdit?.id ?: 0L,
                            title = title.takeIf { it.isNotBlank() },
                            content = content,
                            timestamp = noteToEdit?.timestamp ?: LocalDateTime.now(), // Preserve original timestamp on edit, new for add
                            doctorName = doctorName.takeIf { it.isNotBlank() },
                            nextAppointment = nextAppDateTime
                        )
                        onConfirm(note)
                    }
                }
            ) {
                Text(if (noteToEdit == null) "Add" else "Save")
            }
        },
        dismissButton = {
            Row {
                if (noteToEdit != null && onDelete != null) {
                    TextButton(
                        onClick = { onDelete(noteToEdit) },
                         colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = nextAppointmentDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        nextAppointmentDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        if (nextAppointmentTime == null) nextAppointmentTime = LocalTime.now() // Sensible default
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = nextAppointmentTime?.hour ?: LocalTime.now().hour,
            initialMinute = nextAppointmentTime?.minute ?: LocalTime.now().minute
        )
        // TimePickerDialog composable needs to be defined or imported, using a simpler structure for now
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = { TimePicker(state = timePickerState, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                 TextButton(onClick = {
                    nextAppointmentTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                     if (nextAppointmentDate == null) nextAppointmentDate = LocalDate.now() // Sensible default
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } }
        )
    }
}

@Preview(showBackground = true, name = "Add Note Dialog")
@Composable
fun AddDoctorNoteDialogPreview() {
    RespiratoryHealthAppTheme {
        AddEditDoctorNoteDialog(
            noteToEdit = null,
            onDismiss = { },
            onConfirm = { },
            onDelete = { }
        )
    }
}

@Preview(showBackground = true, name = "Edit Note Dialog")
@Composable
fun EditDoctorNoteDialogPreview() {
    RespiratoryHealthAppTheme {
        AddEditDoctorNoteDialog(
            noteToEdit = DoctorNote(
                id = 1L, 
                title = "Follow-up", 
                content = "Discussed test results and next steps.", 
                timestamp = LocalDateTime.now().minusDays(1),
                doctorName = "Dr. Smith",
                nextAppointment = LocalDateTime.now().plusWeeks(1)
            ),
            onDismiss = { },
            onConfirm = { },
            onDelete = { }
        )
    }
} 