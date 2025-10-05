package com.example.respiratoryhealthapp.screens.doctorsnotes

import com.example.respiratoryhealthapp.data.model.DoctorNote

data class DoctorsNotesUiState(
    val notes: List<DoctorNote> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    // val selectedNote: DoctorNote? = null, // For editing or viewing details - Removed as unused
    val searchQuery: String = ""
    // Potentially add states for dialog visibility, e.g., showAddEditDialog: Boolean = false
) 