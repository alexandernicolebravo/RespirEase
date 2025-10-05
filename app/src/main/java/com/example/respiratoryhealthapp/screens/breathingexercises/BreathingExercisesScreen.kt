package com.example.respiratoryhealthapp.screens.breathingexercises

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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.respiratoryhealthapp.data.model.BreathingExercise
import com.example.respiratoryhealthapp.data.model.RespiratoryCondition
import com.example.respiratoryhealthapp.navigation.NavRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingExercisesScreen(
    navController: NavController,
    viewModel: BreathingExercisesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterMenu by remember { mutableStateOf(false) }
    // We can derive the selected filter text from the ViewModel's state if we add it there,
    // or manage it locally if it's just for display in the TopAppBar.
    // For now, let's assume the ViewModel handles the actual filtering logic.

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Breathing Exercises") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter Exercises")
                        }
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All") },
                                onClick = {
                                    viewModel.filterExercisesByCondition(null)
                                    showFilterMenu = false
                                }
                            )
                            RespiratoryCondition.entries.forEach { condition ->
                                DropdownMenuItem(
                                    text = { Text(condition.displayName) },
                                    onClick = {
                                        viewModel.filterExercisesByCondition(condition)
                                        showFilterMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
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
            } else if (uiState.exercises.isEmpty()) {
                Text(
                    text = "No breathing exercises available at the moment.",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                ExerciseList(exercises = uiState.exercises, onExerciseClick = {
                    // TODO: Navigate to exercise detail screen
                    // navController.navigate("exercise_detail/${it.id}") // Old TODO
                    navController.navigate(NavRoutes.ExerciseDetail.exerciseDetailWithArg(it.id))
                })
            }
        }
    }
}

@Composable
fun ExerciseList(
    exercises: List<BreathingExercise>,
    onExerciseClick: (BreathingExercise) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(exercises) { exercise ->
            ExerciseCard(exercise = exercise, onClick = { onExerciseClick(exercise) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseCard(
    exercise: BreathingExercise,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = exercise.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Duration: ${exercise.durationMinutes} min",
                    style = MaterialTheme.typography.bodySmall
                )
                // Display target conditions if needed
                // Text(
                //     text = "For: ${exercise.targetConditions.joinToString { it.name.lowercase().replaceFirstChar(Char::titlecase) }}",
                //     style = MaterialTheme.typography.bodySmall
                // )
            }
        }
    }
}

// Preview for the list of exercises
@Preview(name = "Exercises List", showBackground = true)
@Composable
fun BreathingExercisesScreenPreview_WithData() {
    val sampleExercises = listOf(
        BreathingExercise(
            id = 1L,
            name = "Pursed Lip Breathing Preview",
            description = "Helps slow your breathing effectively.",
            instructions = listOf("Step 1", "Step 2"),
            durationMinutes = 5,
            targetConditions = listOf(RespiratoryCondition.COPD, RespiratoryCondition.ASTHMA),
            benefits = listOf("Improves lung mechanics")
        ),
        BreathingExercise(
            id = 2L,
            name = "Belly Breathing Preview",
            description = "Strengthens the diaphragm.",
            instructions = listOf("Lie down", "Breathe deep"),
            durationMinutes = 10,
            targetConditions = listOf(RespiratoryCondition.GENERAL_WELLNESS),
            benefits = listOf("Reduces oxygen demand")
        )
    )
    // For preview, we mock the ViewModel and UI state
    // val mockViewModel: BreathingExercisesViewModel = viewModel() // This was unused and potentially problematic
    // To show data, we would ideally use a fake ViewModel that emits this state.
    // For simplicity here, we'll call ExerciseList directly, or the screen with a fake state.

    // A more direct preview of the list content:
    // com.example.respiratoryhealthapp.ui.theme.RespiratoryHealthAppTheme {
    //     Surface {
    //         ExerciseList(exercises = sampleExercises, onExerciseClick = {})
    //     }
    // }

    // Previewing the whole screen requires faking the ViewModel's state.
    // This is a simplified approach for showing the list directly within the screen structure.
    val navController = androidx.navigation.compose.rememberNavController()
    // com.example.respiratoryhealthapp.ui.theme.RespiratoryHealthAppTheme {
        BreathingExercisesScreenWithState( // Helper for previewing with specific state
            navController = navController,
            uiState = BreathingExercisesUiState(exercises = sampleExercises)
        )
    // }
}

@Preview(name = "Loading State", showBackground = true)
@Composable
fun BreathingExercisesScreenPreview_Loading() {
    val navController = androidx.navigation.compose.rememberNavController()
    // com.example.respiratoryhealthapp.ui.theme.RespiratoryHealthAppTheme {
        BreathingExercisesScreenWithState(
            navController = navController,
            uiState = BreathingExercisesUiState(isLoading = true)
        )
    // }
}

@Preview(name = "Error State", showBackground = true)
@Composable
fun BreathingExercisesScreenPreview_Error() {
    val navController = androidx.navigation.compose.rememberNavController()
    // com.example.respiratoryhealthapp.ui.theme.RespiratoryHealthAppTheme {
        BreathingExercisesScreenWithState(
            navController = navController,
            uiState = BreathingExercisesUiState(error = "Failed to load exercises. Please try again.")
        )
    // }
}

@Preview(name = "Empty State", showBackground = true)
@Composable
fun BreathingExercisesScreenPreview_Empty() {
    val navController = androidx.navigation.compose.rememberNavController()
    // com.example.respiratoryhealthapp.ui.theme.RespiratoryHealthAppTheme {
        BreathingExercisesScreenWithState(
            navController = navController,
            uiState = BreathingExercisesUiState(exercises = emptyList())
        )
    // }
}

// Helper Composable to inject state for previewing BreathingExercisesScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BreathingExercisesScreenWithState(
    navController: NavController,
    uiState: BreathingExercisesUiState
) {
    var showFilterMenu by remember { mutableStateOf(false) } // Local state for preview

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Breathing Exercises") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter Exercises")
                        }
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All") },
                                onClick = { showFilterMenu = false }
                            )
                            RespiratoryCondition.entries.forEach { condition ->
                                DropdownMenuItem(
                                    text = { Text(condition.displayName) },
                                    onClick = { showFilterMenu = false }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
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
            } else if (uiState.exercises.isEmpty()) {
                Text(
                    text = "No breathing exercises available at the moment.",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                ExerciseList(exercises = uiState.exercises, onExerciseClick = {})
            }
        }
    }
} 