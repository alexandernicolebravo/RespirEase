package com.example.respiratoryhealthapp.screens.symptomdiary.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePicker(
    startDate: LocalDateTime?,
    endDate: LocalDateTime?,
    onStartDateChange: (LocalDateTime?) -> Unit,
    onEndDateChange: (LocalDateTime?) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val startDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = startDate?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    )
    val endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = endDate?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Start Date
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Start Date",
                style = MaterialTheme.typography.bodyLarge
            )
            TextButton(
                onClick = { showStartDatePicker = true }
            ) {
                Text(
                    text = startDate?.format(dateFormatter) ?: "Select date",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // End Date
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "End Date",
                style = MaterialTheme.typography.bodyLarge
            )
            TextButton(
                onClick = { showEndDatePicker = true }
            ) {
                Text(
                    text = endDate?.format(dateFormatter) ?: "Select date",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Clear button
        if (startDate != null || endDate != null) {
            TextButton(
                onClick = {
                    onStartDateChange(null)
                    onEndDateChange(null)
                    // Reset picker states if needed, though they re-initialize on next open
                    startDatePickerState.selectedDateMillis = null
                    endDatePickerState.selectedDateMillis = null
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Clear Dates")
            }
        }
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        startDatePickerState.selectedDateMillis?.let { millis ->
                            val selectedLdt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()).withHour(0).withMinute(0).withSecond(0)
                            onStartDateChange(selectedLdt)
                        }
                        showStartDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        endDatePickerState.selectedDateMillis?.let { millis ->
                             val selectedLdt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()).withHour(23).withMinute(59).withSecond(59)
                            onEndDateChange(selectedLdt)
                        }
                        showEndDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }
} 