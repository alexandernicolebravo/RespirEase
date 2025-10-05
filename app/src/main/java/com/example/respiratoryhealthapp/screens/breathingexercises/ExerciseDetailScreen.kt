package com.example.respiratoryhealthapp.screens.breathingexercises

import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.respiratoryhealthapp.data.model.BreathingExercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    navController: NavController,
    exerciseId: Long,
    viewModel: BreathingExercisesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(exerciseId) {
        viewModel.loadExerciseById(exerciseId)
    }

    // Clear selected exercise when screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearSelectedExercise()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.selectedExercise?.name ?: "Exercise Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            contentAlignment = Alignment.TopStart // Changed to TopStart for scrollable content
        ) {
            if (uiState.isLoading && uiState.selectedExercise == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null && uiState.selectedExercise == null) {
                Text(
                    text = uiState.error ?: "Failed to load details",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.selectedExercise != null) {
                ExerciseDetailsContent(exercise = uiState.selectedExercise!!, onOpenVideo = {
                    val intent = Intent(Intent.ACTION_VIEW, it.toUri())
                    try {
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Log.e("ExerciseDetailScreen", "No app found to handle video URL: $it", e)
                        // Optionally, show a Toast to the user
                    }
                })
            } else {
                // Handles the case where ID is invalid or exercise not found after loading
                Text(
                    text = "Exercise not found.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun ExerciseDetailsContent(exercise: BreathingExercise, onOpenVideo: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(exercise.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Duration: ${exercise.durationMinutes} minutes", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Description:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(exercise.description, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Instructions:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        exercise.instructions.forEachIndexed { index, instruction ->
            Text("${index + 1}. $instruction", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (exercise.benefits.isNotEmpty()) {
            Text("Benefits:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            exercise.benefits.forEach {
                Text("• $it", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (exercise.targetConditions.isNotEmpty()) {
            Text("Good for conditions like:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                exercise.targetConditions.joinToString { it.displayName },
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        exercise.videoUrl?.let {
            if (it.isNotBlank()) {
                Button(onClick = { onOpenVideo(it) }) {
                    Text("Watch Video Guide")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExerciseDetailsContentPreview() {
    val sampleExercise = BreathingExercise(
        id = 1L,
        name = "Deep Dive Breathing",
        description = "A calming breathing technique to center yourself. It involves slow, deep inhales and even slower, controlled exhales, focusing on the sensation of breath filling and leaving your lungs.",
        instructions = listOf(
            "Find a comfortable seated position.",
            "Close your eyes and relax your shoulders.",
            "Inhale slowly through your nose for 4 counts.",
            "Hold your breath gently for 2 counts.",
            "Exhale slowly and completely through your mouth for 6 counts.",
            "Repeat for 5-10 minutes."
        ),
        durationMinutes = 7,
        targetConditions = listOf(com.example.respiratoryhealthapp.data.model.RespiratoryCondition.GENERAL_WELLNESS, com.example.respiratoryhealthapp.data.model.RespiratoryCondition.ASTHMA),
        videoUrl = "https://www.example.com/video",
        benefits = listOf("Reduces stress", "Improves focus", "Calms the nervous system", "Increases oxygen intake")
    )
    // Assuming your app has a Theme defined, wrap the preview in it for consistent styling
    // For example: com.example.respiratoryhealthapp.ui.theme.RespiratoryHealthAppTheme {
        Surface {
            ExerciseDetailsContent(exercise = sampleExercise, onOpenVideo = {})
        }
    // }
} 