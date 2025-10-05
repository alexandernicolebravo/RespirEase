package com.example.respiratoryhealthapp.ui.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.respiratoryhealthapp.ui.theme.RespiratoryHealthAppTheme

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmButtonText: String = "Confirm",
    dismissButtonText: String = "Cancel"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissButtonText)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ConfirmationDialogPreview() {
    RespiratoryHealthAppTheme {
        ConfirmationDialog(
            title = "Confirm Deletion",
            message = "Are you sure you want to delete this item?",
            onConfirm = {},
            onDismiss = {}
        )
    }
} 