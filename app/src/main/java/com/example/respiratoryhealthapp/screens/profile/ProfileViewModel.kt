package com.example.respiratoryhealthapp.screens.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.respiratoryhealthapp.data.model.RespiratoryCondition
import com.example.respiratoryhealthapp.data.model.UserProfile
import com.example.respiratoryhealthapp.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserProfileRepository = UserProfileRepository(application)

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.userProfileFlow
                .distinctUntilChanged()
                .onStart { _uiState.update { it.copy(isLoading = true, error = null) } }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load profile: ${e.message}") }
                }
                .collect { userProfile ->
                    _uiState.update {
                        it.copy(
                            userProfile = userProfile,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun updateName(name: String) {
        viewModelScope.launch {
            repository.updateUserName(name)
            // UI will update automatically due to observing userProfileFlow
            _uiState.update { it.copy(showEditNameDialog = false) } // Close dialog after update
        }
    }

    fun updateSelectedCondition(condition: RespiratoryCondition?) {
        viewModelScope.launch {
            repository.updateSelectedCondition(condition)
        }
    }

    fun updateAge(age: Int?) {
        viewModelScope.launch {
            repository.updateAge(age)
        }
    }

    fun updateGender(gender: String) {
        viewModelScope.launch {
            repository.updateGender(gender)
        }
    }

    fun updateMedicationList(medications: String) {
        viewModelScope.launch {
            repository.updateMedicationList(medications)
        }
    }

    fun updateEmergencyContacts(contacts: String) {
        viewModelScope.launch {
            repository.updateEmergencyContacts(contacts)
        }
    }

    fun updateDarkMode(isEnabled: Boolean) {
        viewModelScope.launch {
            repository.updateDarkMode(isEnabled)
        }
    }

    fun setShowEditNameDialog(show: Boolean) {
        _uiState.update { it.copy(showEditNameDialog = show) }
    }
} 