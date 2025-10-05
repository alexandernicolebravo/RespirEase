package com.example.respiratoryhealthapp.screens.profile

import com.example.respiratoryhealthapp.data.model.UserProfile

data class ProfileUiState(
    val userProfile: UserProfile = UserProfile(), // Initialize with default UserProfile
    val isLoading: Boolean = false,
    val error: String? = null,
    val showEditNameDialog: Boolean = false
    // Add other state flags as needed, e.g., for editing other fields
) 