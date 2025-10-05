package com.example.respiratoryhealthapp.screens.doctorsnotes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.respiratoryhealthapp.data.database.AppDatabase
import com.example.respiratoryhealthapp.data.model.DoctorNote
import com.example.respiratoryhealthapp.data.repository.DoctorNoteRepository
import com.example.respiratoryhealthapp.data.repository.RoomDoctorNoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DoctorsNotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DoctorNoteRepository

    private val _uiState = MutableStateFlow(DoctorsNotesUiState())
    val uiState: StateFlow<DoctorsNotesUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    // Expose search query for two-way binding or observation if needed from UI directly
    // val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        val doctorNoteDao = AppDatabase.getDatabase(application).doctorNoteDao()
        repository = RoomDoctorNoteRepository(doctorNoteDao)

        viewModelScope.launch {
            _searchQuery
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        repository.getAllDoctorNotes()
                    } else {
                        repository.searchDoctorNotes(query)
                    }
                }
                .onStart { _uiState.update { it.copy(isLoading = true, error = null) } }
                .catch { e ->
                    _uiState.update { currentState ->
                        currentState.copy(isLoading = false, error = "Failed to load notes: ${e.message}")
                    }
                }
                .collect { notes ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            notes = notes, 
                            isLoading = false, 
                            searchQuery = _searchQuery.value // Keep UI state searchQuery in sync
                        )
                    }
                }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        // No need to update uiState.searchQuery here directly, 
        // it will be updated when the collector above runs after flatMapLatest emits.
    }

    fun addDoctorNote(note: DoctorNote) {
        viewModelScope.launch {
            repository.insertDoctorNote(note)
            // UI will update via the flow collection
        }
    }

    fun updateDoctorNote(note: DoctorNote) {
        viewModelScope.launch {
            repository.updateDoctorNote(note)
            // UI will update via the flow collection
        }
    }

    fun deleteDoctorNote(note: DoctorNote) {
        viewModelScope.launch {
            repository.deleteDoctorNote(note)
            // UI will update via the flow collection
        }
    }

    // fun selectNote(note: DoctorNote?) {
    //     _uiState.update { it.copy(selectedNote = note) } // selectedNote removed from UiState
    // }
} 