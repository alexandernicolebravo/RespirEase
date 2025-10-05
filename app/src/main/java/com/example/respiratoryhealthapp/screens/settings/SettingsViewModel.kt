package com.example.respiratoryhealthapp.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.respiratoryhealthapp.data.database.AppDatabase
import com.example.respiratoryhealthapp.data.repository.RoomSymptomRepository
import com.example.respiratoryhealthapp.data.repository.SymptomRepository
import com.example.respiratoryhealthapp.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val userProfileRepository: UserProfileRepository = UserProfileRepository(application)
    private val symptomRepository: SymptomRepository

    private val _uiState = MutableStateFlow(SettingsUiState()) // Use this for dialogs, snackbars
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val isDarkModeEnabled: StateFlow<Boolean> = userProfileRepository.userProfileFlow
        .map { it.isDarkModeEnabled }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isMasterNotificationsEnabled: StateFlow<Boolean> = userProfileRepository.userProfileFlow
        .map { it.masterNotificationsEnabled }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true // Default to true, consistent with UserProfile
        )

    init {
        val symptomDao = AppDatabase.getDatabase(application).symptomDao()
        symptomRepository = RoomSymptomRepository(symptomDao)
    }

    fun updateDarkMode(isEnabled: Boolean) {
        viewModelScope.launch {
            userProfileRepository.updateDarkMode(isEnabled)
        }
    }

    fun updateMasterNotificationsEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            userProfileRepository.updateMasterNotificationsEnabled(isEnabled)
        }
    }

    fun clearSymptomHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, showClearSymptomsConfirmDialog = false) } // Close dialog, show loading
            try {
                symptomRepository.deleteAllSymptoms()
                _uiState.update { it.copy(isLoading = false, snackbarMessage = "Symptom history cleared.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to clear symptom history: ${e.message}", snackbarMessage = "Error: Could not clear history.") }
            }
        }
    }

    fun setShowClearSymptomsConfirmDialog(show: Boolean) {
        _uiState.update { it.copy(showClearSymptomsConfirmDialog = show) }
    }

    fun snackbarMessageShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
} 