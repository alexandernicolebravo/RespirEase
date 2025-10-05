package com.example.respiratoryhealthapp.screens.reminders.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.respiratoryhealthapp.data.model.Reminder
import com.example.respiratoryhealthapp.data.model.ReminderRecurrence
import com.example.respiratoryhealthapp.data.model.ReminderType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditReminderDialog(
    reminderToEdit: Reminder? = null, // Pass existing reminder for editing, null for adding
    onDismiss: () -> Unit,
    onConfirm: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit // Added delete callback
) {
    var title by remember { mutableStateOf(reminderToEdit?.title ?: "") }
    var description by remember { mutableStateOf(reminderToEdit?.description ?: "") }
    var selectedDate by remember { mutableStateOf(reminderToEdit?.dateTime?.toLocalDate() ?: LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(reminderToEdit?.dateTime?.toLocalTime() ?: LocalTime.now()) }
    var selectedType by remember { mutableStateOf(reminderToEdit?.type ?: ReminderType.CUSTOM) }
    var selectedRecurrence by remember { mutableStateOf(reminderToEdit?.recurrence ?: ReminderRecurrence.NONE) }
    val isActive = reminderToEdit?.isActive != false // Default to true for new reminders

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showTypeDropdown by remember { mutableStateOf(false) }
    var showRecurrenceDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (reminderToEdit == null) "Add Reminder" else "Edit Reminder") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()), // Make dialog content scrollable
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // Date Picker
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        onValueChange = {}, // Not directly editable
                        label = { Text("Date") },
                        readOnly = true,
                        enabled = false, // disables internal pointer input
                        trailingIcon = { Icon(Icons.Default.DateRange, "Select Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }

                // Time Picker
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedTime.format(DateTimeFormatter.ofPattern("hh:mm a")),
                        onValueChange = {}, // Not directly editable
                        label = { Text("Time") },
                        readOnly = true,
                        enabled = false, // disables internal pointer input
                        trailingIcon = { Icon(Icons.Default.Schedule, "Select Time") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showTimePicker = true }
                    )
                }

                // Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = showTypeDropdown,
                    onExpandedChange = { showTypeDropdown = !showTypeDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedType.displayName,
                        onValueChange = {}, // Not editable
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeDropdown) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = showTypeDropdown,
                        onDismissRequest = { showTypeDropdown = false }
                    ) {
                        ReminderType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    selectedType = type
                                    showTypeDropdown = false
                                }
                            )
                        }
                    }
                }

                // Recurrence Dropdown
                ExposedDropdownMenuBox(
                    expanded = showRecurrenceDropdown,
                    onExpandedChange = { showRecurrenceDropdown = !showRecurrenceDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedRecurrence.displayName,
                        onValueChange = {}, // Not editable
                        readOnly = true,
                        label = { Text("Repeats") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showRecurrenceDropdown) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = showRecurrenceDropdown,
                        onDismissRequest = { showRecurrenceDropdown = false }
                    ) {
                        ReminderRecurrence.entries.forEach { recurrence ->
                            DropdownMenuItem(
                                text = { Text(recurrence.displayName) },
                                onClick = {
                                    selectedRecurrence = recurrence
                                    showRecurrenceDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val finalDateTime = LocalDateTime.of(selectedDate, selectedTime)
                        val newOrUpdatedReminder = Reminder(
                            id = reminderToEdit?.id ?: 0L,
                            title = title,
                            description = description.takeIf { it.isNotBlank() },
                            dateTime = finalDateTime,
                            type = selectedType,
                            recurrence = selectedRecurrence,
                            isActive = isActive
                        )
                        onConfirm(newOrUpdatedReminder)
                    } // Else, maybe show a validation error toast/message
                }
            ) {
                Text(if (reminderToEdit == null) "Add" else "Save")
            }
        },
        dismissButton = {
            Row {
                // Show delete button only when editing
                if (reminderToEdit != null) {
                    TextButton(
                        onClick = { onDelete(reminderToEdit) }, // Call delete callback
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                    Spacer(modifier = Modifier.weight(1f)) // Pushes buttons apart
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            yearRange = (LocalDate.now().year .. LocalDate.now().year + 100)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, selectedTime.hour)
            set(Calendar.MINUTE, selectedTime.minute)
        }
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE),
            is24Hour = false // Or true, based on preference/locale
        )
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        ) { // Content of the dialog
            // In a TimePickerDialog, the TimePicker is often part of the dialog's internal content.
            // If you need to customize, you might use TimeInput inside the dialog.
            // For standard behavior, this is sufficient.
            // This is a bit of a workaround for how TimePickerDialog expects its content.
            // The TimePicker is shown by the dialog itself usually.
             Box(modifier = Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(state = timePickerState)
             }
        }
    }
}

// Helper for TimePickerDialog content as it expects a specific structure or provides its own
@Composable
fun TimePickerDialog(
    title: String = "Select Time",
    onDismissRequest: () -> Unit,
    confirmButton: @Composable (() -> Unit),
    dismissButton: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = content, // The TimePicker or TimeInput goes here
        confirmButton = confirmButton,
        dismissButton = dismissButton
    )
}

// --- Previews --- //

@Preview(name = "Add Reminder Dialog", showBackground = true)
@Composable
fun AddReminderDialogPreview() {
    // Wrap in Theme if needed
    // com.example.respiratoryhealthapp.ui.theme.RespiratoryHealthAppTheme {
        Surface {
            AddEditReminderDialog(
                onDismiss = {},
                onConfirm = {},
                onDelete = {}
            )
        }
    // }
}

@Preview(name = "Edit Reminder Dialog", showBackground = true)
@Composable
fun EditReminderDialogPreview() {
    val sampleReminder = Reminder(
        id = 1L,
        title = "Existing Medication Reminder",
        description = "Take this with food.",
        dateTime = LocalDateTime.now().plusHours(2),
        type = ReminderType.MEDICATION,
        recurrence = ReminderRecurrence.DAILY,
        isActive = false
    )
    // Wrap in Theme if needed
    // com.example.respiratoryhealthapp.ui.theme.RespiratoryHealthAppTheme {
        Surface {
            AddEditReminderDialog(
                reminderToEdit = sampleReminder,
                onDismiss = {},
                onConfirm = {},
                onDelete = {} // Add dummy delete callback for preview
            )
        }
    // }
} 