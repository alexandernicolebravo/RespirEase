@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.respiratoryhealthapp.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.respiratoryhealthapp.data.model.RespiratoryCondition
import com.example.respiratoryhealthapp.data.model.UserProfile
import com.example.respiratoryhealthapp.navigation.NavRoutes
import com.example.respiratoryhealthapp.ui.components.SimpleTopAppBar
import com.example.respiratoryhealthapp.ui.theme.RespiratoryHealthAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(LocalContext.current.applicationContext as android.app.Application))
) {
    val uiState by viewModel.uiState.collectAsState()
    val userProfile = uiState.userProfile

    var showEditNameDialog by remember { mutableStateOf(false) }
    var tempName by remember(userProfile.name) { mutableStateOf(userProfile.name) }

    var tempAge by remember(userProfile.age) { mutableStateOf(userProfile.age?.toString() ?: "") }
    var tempGender by remember(userProfile.gender) { mutableStateOf(userProfile.gender) }
    var tempMedications by remember(userProfile.medicationList) { mutableStateOf(userProfile.medicationList) }
    var tempEmergencyContacts by remember(userProfile.emergencyContacts) { mutableStateOf(userProfile.emergencyContacts) }


    if (showEditNameDialog) {
        EditNameDialog(
            currentName = tempName,
            onNameChange = { tempName = it },
            onConfirm = {
                viewModel.updateName(tempName)
                showEditNameDialog = false
            },
            onDismiss = { showEditNameDialog = false }
        )
    }

    Scaffold(
        topBar = {
            SimpleTopAppBar(title = "Profile", navController = navController, showBackButton = false)
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Error: ${uiState.error}")
            }
        } else {
            Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                ProfileHeader(userProfile = userProfile, onEditNameClick = { showEditNameDialog = true })
                Spacer(modifier = Modifier.height(24.dp))

                Text("Personal Information", style = MaterialTheme.typography.titleMedium)
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Name (handled by dialog)
                ProfileItem(label = "Name", value = userProfile.name) {
                    showEditNameDialog = true
                }

                // Age
                EditableProfileTextField(
                    label = "Age",
                    value = tempAge,
                    onValueChange = { tempAge = it },
                    onSave = { viewModel.updateAge(tempAge.toIntOrNull()) },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )

                // Gender Dropdown
                GenderSelector(
                    selectedGender = tempGender,
                    onGenderSelected = { 
                        tempGender = it
                        viewModel.updateGender(it) 
                    }
                )

                RespiratoryConditionSelector(
                    selectedCondition = userProfile.selectedCondition,
                    onConditionSelected = { viewModel.updateSelectedCondition(it) }
                )
                Spacer(modifier = Modifier.height(24.dp))

                Text("Health & Emergency", style = MaterialTheme.typography.titleMedium)
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Medication List
                EditableProfileTextField(
                    label = "Medications",
                    value = tempMedications,
                    placeholder = "e.g., Salbutamol, Seretide",
                    onValueChange = { tempMedications = it },
                    onSave = { viewModel.updateMedicationList(tempMedications) },
                     keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = false,
                    maxLines = 3
                )

                // Emergency Contacts
                EditableProfileTextField(
                    label = "Emergency Contacts",
                    value = tempEmergencyContacts,
                    placeholder = "e.g., Jane Doe: 123-456-7890",
                    onValueChange = { tempEmergencyContacts = it },
                    onSave = { viewModel.updateEmergencyContacts(tempEmergencyContacts) },
                     keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = false,
                    maxLines = 3
                )


                Spacer(modifier = Modifier.height(24.dp))
                Text("App Settings", style = MaterialTheme.typography.titleMedium)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileItem(label = "Settings", value = "") {
                    navController.navigate(NavRoutes.Settings.route)
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(userProfile: UserProfile, onEditNameClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = "Profile Icon",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(userProfile.name, style = MaterialTheme.typography.headlineSmall)
            // Potentially add email or other primary identifier here if available
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onEditNameClick) {
            Icon(Icons.Filled.Edit, contentDescription = "Edit Name")
        }
    }
}

@Composable
fun ProfileItem(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Icon(
            imageVector = Icons.AutoMirrored.Filled.NavigateNext,
            contentDescription = "Go to $label",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNameDialog(
    currentName: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Name") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; onNameChange(it) },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
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
@Composable
fun RespiratoryConditionSelector(
    selectedCondition: RespiratoryCondition?,
    onConditionSelected: (RespiratoryCondition) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val conditions = RespiratoryCondition.entries

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text("Respiratory Condition", style = MaterialTheme.typography.bodyLarge)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedCondition?.displayName ?: "Select Condition",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                conditions.forEach { condition ->
                    DropdownMenuItem(
                        text = { Text(condition.displayName) },
                        onClick = {
                            onConditionSelected(condition)
                            expanded = false
                        }
            )
        }
    }
}
        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    var isEditing by remember { mutableStateOf(false) }
    var textFieldValue by remember(value, isEditing) { mutableStateOf(value) } // Re-init when original value changes or edit mode toggles

    LaunchedEffect(value) { // If the underlying value changes from VM, update local text field
        if (!isEditing) {
            textFieldValue = value
        }
    }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            if (isEditing) {
                Button(onClick = {
                    onValueChange(textFieldValue) // Ensure the latest value is passed
                    onSave()
                    isEditing = false
                }) {
                    Text("Save")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = {
                    isEditing = false
                    textFieldValue = value // Reset to original value on cancel
                    onValueChange(value) // also update the ViewModel's temporary state if it was changed
                }) {
                    Text("Cancel")
                }
            } else {
                IconButton(onClick = { isEditing = true }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit $label")
                }
            }
        }
        if (isEditing) {
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                    // Optionally, call onValueChange here if you want live updates to VM's temp state
                    // onValueChange(it)
                },
                label = { Text(label) },
                placeholder = { Text(placeholder) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = keyboardOptions,
                singleLine = singleLine,
                maxLines = maxLines
            )
        } else {
            Text(
                text = value.ifEmpty { placeholder.ifEmpty { "Not set" } },
                style = MaterialTheme.typography.bodyMedium,
                color = if (value.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
        Divider(modifier = Modifier.padding(top = 8.dp))
                        }
                    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenderSelector(
    selectedGender: String,
    onGenderSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val genders = listOf("Male", "Female", "Non-binary", "Prefer not to say")

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text("Gender", style = MaterialTheme.typography.bodyLarge)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = if (selectedGender.isEmpty()) "Select Gender" else selectedGender,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                genders.forEach { gender ->
                    DropdownMenuItem(
                        text = { Text(gender) },
                        onClick = {
                            onGenderSelected(gender)
                            expanded = false
                        }
                    )
                }
            }
        }
        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}

// ViewModelFactory for ProfileViewModel
class ProfileViewModelFactory(private val application: android.app.Application) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
            }
        }


// --- Previews ---
@Preview(showBackground = true, name = "Edit Name Dialog")
@Composable
fun EditNameDialogPreview() {
    RespiratoryHealthAppTheme {
        Surface {
            EditNameDialog(
                currentName = "Jane Doe",
                onNameChange = {},
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
} 