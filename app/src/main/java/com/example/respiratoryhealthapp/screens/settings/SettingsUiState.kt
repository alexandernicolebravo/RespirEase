package com.example.respiratoryhealthapp.screens.settings

// Currently, SettingsUiState might be very simple if the ViewModel directly exposes flows
// from the repository. It can be expanded later if more complex UI-specific state is needed.
data class SettingsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val showClearSymptomsConfirmDialog: Boolean = false,
    val snackbarMessage: String? = null
    // Example: could hold a copy of UserPreferences if needed for complex transformations
    // val userPreferences: UserPreferences? = null
) 