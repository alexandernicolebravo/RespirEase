package com.example.respiratoryhealthapp.screens.breathingexercises

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.respiratoryhealthapp.data.model.RespiratoryCondition
import com.example.respiratoryhealthapp.data.repository.BreathingExerciseRepository
import com.example.respiratoryhealthapp.data.database.AppDatabase
import com.example.respiratoryhealthapp.data.repository.RoomBreathingExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BreathingExercisesViewModel(application: Application) : AndroidViewModel(application) {

    // In a real app, this would be injected (e.g., using Hilt)
    private val repository: BreathingExerciseRepository

    private val _uiState = MutableStateFlow(BreathingExercisesUiState())
    val uiState: StateFlow<BreathingExercisesUiState> = _uiState.asStateFlow()

    init {
        val exerciseDao = AppDatabase.getDatabase(application).breathingExerciseDao()
        repository = RoomBreathingExerciseRepository(exerciseDao)
        loadAllExercises()
    }

    fun loadAllExercises() {
        viewModelScope.launch {
            repository.getExercises()
                .onStart { _uiState.update { it.copy(isLoading = true, error = null) } }
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = "Failed to load exercises: ${e.message}")
                    }
                }
                .collect { exercises ->
                    _uiState.update {
                        it.copy(exercises = exercises, isLoading = false, error = null)
                    }
                }
        }
    }

    fun filterExercisesByCondition(condition: RespiratoryCondition?) {
        viewModelScope.launch {
            if (condition == null) {
                loadAllExercises() // Load all if condition is null
            } else {
                repository.getExercisesByCondition(condition.name)
                    .onStart { _uiState.update { it.copy(isLoading = true, error = null) } }
                    .catch { e ->
                        _uiState.update {
                            it.copy(isLoading = false, error = "Failed to filter exercises: ${e.message}")
                        }
                    }
                    .collect { exercises ->
                        _uiState.update {
                            it.copy(exercises = exercises, isLoading = false, error = null)
                        }
                    }
            }
        }
    }

    fun loadExerciseById(id: Long) {
        viewModelScope.launch {
            repository.getExerciseById(id)
                .onStart { _uiState.update { it.copy(isLoading = true, error = null, selectedExercise = null) } }
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = "Failed to load exercise details: ${e.message}")
                    }
                }
                .collect { exercise ->
                    _uiState.update {
                        it.copy(selectedExercise = exercise, isLoading = false, error = null)
                    }
                }
        }
    }

    fun clearSelectedExercise() {
        _uiState.update { it.copy(selectedExercise = null) }
    }
} 