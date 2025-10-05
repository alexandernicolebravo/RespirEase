package com.example.respiratoryhealthapp.screens.symptomdiary.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.respiratoryhealthapp.data.model.Symptom
import java.time.format.DateTimeFormatter

@Composable
fun SymptomCard(
    symptom: Symptom,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onEdit
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = symptom.type.name.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Severity: ${symptom.severity}/5",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = symptom.timestamp.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            symptom.notes?.let { notes ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            symptom.trigger?.let { trigger ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Trigger: $trigger",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            symptom.peakFlow?.let { peakFlow ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Peak Flow: $peakFlow",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
} 