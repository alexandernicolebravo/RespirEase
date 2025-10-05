package com.example.respiratoryhealthapp.screens.testresults.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.respiratoryhealthapp.data.model.TestResult
import com.example.respiratoryhealthapp.ui.theme.RespiratoryHealthAppTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun TestResultCard(
    testResult: TestResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = testResult.testName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Value: ${testResult.value} ${testResult.unit ?: ""}".trimEnd(),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = testResult.timestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!testResult.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Notes: ${testResult.notes}",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TestResultCardPreview() {
    RespiratoryHealthAppTheme {
        TestResultCard(
            testResult = TestResult(
                id = 1L,
                timestamp = LocalDateTime.now().minusDays(1),
                testName = "Peak Flow Meter",
                value = "450",
                unit = "L/min",
                notes = "Feeling pretty good today, slightly better than yesterday morning. Consistent with medication."
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TestResultCardMinimalPreview() {
    RespiratoryHealthAppTheme {
        TestResultCard(
            testResult = TestResult(
                id = 2L,
                timestamp = LocalDateTime.now().minusHours(2),
                testName = "Oxygen Saturation",
                value = "97",
                unit = "%",
                notes = null
            ),
            onClick = {}
        )
    }
} 