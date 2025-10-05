package com.example.respiratoryhealthapp.screens.breathingexercises

import com.example.respiratoryhealthapp.data.model.BreathingExercise
 
data class BreathingExercisesUiState(
    val exercises: List<BreathingExercise> = emptyList(),
    val selectedExercise: BreathingExercise? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) 