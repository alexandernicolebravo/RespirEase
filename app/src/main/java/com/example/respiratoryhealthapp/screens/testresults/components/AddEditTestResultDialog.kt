package com.example.respiratoryhealthapp.screens.testresults.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.respiratoryhealthapp.data.model.TestResult
import com.example.respiratoryhealthapp.ui.theme.RespiratoryHealthAppTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.ui.tooling.preview.Preview
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTestResultDialog(
    testResultToEdit: TestResult? = null,
    onDismiss: () -> Unit,
    onConfirm: (TestResult) -> Unit,
    onDelete: ((TestResult) -> Unit)? = null
) {
    var testName by remember { mutableStateOf(testResultToEdit?.testName ?: "") }
    var value by remember { mutableStateOf(testResultToEdit?.value ?: "") }
    var unit by remember { mutableStateOf(testResultToEdit?.unit ?: "") }
    var notes by remember { mutableStateOf(testResultToEdit?.notes ?: "") }
    var selectedDate by remember { mutableStateOf(testResultToEdit?.timestamp?.toLocalDate() ?: LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(testResultToEdit?.timestamp?.toLocalTime() ?: LocalTime.now()) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val isTestNameValid = testName.isNotBlank()
    val isValueValid = value.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (testResultToEdit == null) "Add Test Result" else "Edit Test Result") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = testName,
                    onValueChange = { testName = it },
                    label = { Text("Test Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isTestNameValid
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Value") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isValueValid
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTimestamp = LocalDateTime.of(selectedDate, selectedTime)
                    val newOrUpdatedTestResult = TestResult(
                        id = testResultToEdit?.id ?: 0L,
                        timestamp = finalTimestamp,
                        testName = testName,
                        value = value,
                        unit = unit.takeIf { it.isNotBlank() },
                        notes = notes.takeIf { it.isNotBlank() }
                    )
                    onConfirm(newOrUpdatedTestResult)
                },
                enabled = isTestNameValid && isValueValid
            ) {
                Text(if (testResultToEdit == null) "Add" else "Save")
            }
        },
        dismissButton = {
            Row {
                if (testResultToEdit != null && onDelete != null) {
                    TextButton(
                        onClick = { onDelete(testResultToEdit) },
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
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            yearRange = (LocalDate.now().year - 100 .. LocalDate.now().year + 1)
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
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                    TimePicker(state = timePickerState)
                }
            },
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
        )
    }
}

// --- Previews ---
@Preview(name = "Add Test Result Dialog", showBackground = true)
@Composable
fun AddTestResultDialogPreview() {
    RespiratoryHealthAppTheme {
        Surface {
            AddEditTestResultDialog(
                onDismiss = {},
                onConfirm = {},
                onDelete = {}
            )
        }
    }
}

@Preview(name = "Edit Test Result Dialog", showBackground = true)
@Composable
fun EditTestResultDialogPreview() {
    val sampleResult = TestResult(
        id = 1L,
        timestamp = LocalDateTime.now().minusDays(1),
        testName = "Peak Flow",
        value = "420",
        unit = "L/min",
        notes = "Morning check, feeling a bit tight."
    )
    RespiratoryHealthAppTheme {
        Surface {
            AddEditTestResultDialog(
                testResultToEdit = sampleResult,
                onDismiss = {},
                onConfirm = {},
                onDelete = {}
            )
        }
    }
} 