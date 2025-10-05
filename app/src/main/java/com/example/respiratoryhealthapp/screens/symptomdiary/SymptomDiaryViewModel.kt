package com.example.respiratoryhealthapp.screens.symptomdiary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.respiratoryhealthapp.data.database.AppDatabase
import com.example.respiratoryhealthapp.data.model.Symptom
import com.example.respiratoryhealthapp.data.model.SymptomType
import com.example.respiratoryhealthapp.data.repository.RoomSymptomRepository
import com.example.respiratoryhealthapp.data.repository.SymptomRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
open class SymptomDiaryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SymptomRepository = RoomSymptomRepository(
        AppDatabase.getDatabase(application).symptomDao()
    )

    private val _uiState = MutableStateFlow(SymptomDiaryUiState())
    open val uiState: StateFlow<SymptomDiaryUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _dateRange = MutableStateFlow<Pair<LocalDateTime?, LocalDateTime?>>(null to null)
    private val _sortOrder = MutableStateFlow(SortOrder.DATE_DESC)
    private val _severityFilter = MutableStateFlow<Int?>(null)
    private val _symptomTypeFilter = MutableStateFlow<SymptomType?>(null)


    init {
        viewModelScope.launch {
            // Base flow determined by search query and symptom type
            val baseSymptomsFlow = combine(_searchQuery, _symptomTypeFilter) { query, type ->
                query to type
            }.flatMapLatest { (query, type) ->
                if (query.isNotBlank()) {
                    repository.searchSymptoms(query).map { symptoms ->
                        // If type filter is also active with search, apply it in Kotlin
                        if (type != null) {
                            symptoms.filter { it.type == type }
                        } else {
                            symptoms
                        }
                    }
                } else if (type != null) {
                    repository.getSymptomsByType(type.name) // Assuming type.name matches DAO's expected string
                } else {
                    repository.allSymptoms
                }
            }

            // Combine the base flow with other filters and sorting
            combine(
                baseSymptomsFlow,
                _dateRange,
                _sortOrder,
                _severityFilter
            ) { symptomsFromBase, dateRange, sortOrder, severity ->
                try {
                    _uiState.update { it.copy(isLoading = true, error = null) } // Start loading state for subsequent filters
                    
                    var filteredSymptoms = symptomsFromBase // Already pre-filtered by search/type

                    // Apply date range filter
                    dateRange.let { (start, end) ->
                        if (start != null && end != null) {
                            filteredSymptoms = filteredSymptoms.filter { symptom ->
                                // Ensure the symptom's timestamp is within the selected day
                                val symptomDate = symptom.timestamp.toLocalDate()
                                val startDate = start.toLocalDate()
                                val endDate = end.toLocalDate()
                                !symptomDate.isBefore(startDate) && !symptomDate.isAfter(endDate)
                            }
                        }
                    }

                    // Apply severity filter
                    if (severity != null) {
                        filteredSymptoms = filteredSymptoms.filter { it.severity == severity }
                    }

                    // Apply sorting
                    filteredSymptoms = when (sortOrder) {
                        SortOrder.DATE_DESC -> filteredSymptoms.sortedByDescending { it.timestamp }
                        SortOrder.DATE_ASC -> filteredSymptoms.sortedBy { it.timestamp }
                        SortOrder.SEVERITY_DESC -> filteredSymptoms.sortedByDescending { it.severity }
                        SortOrder.SEVERITY_ASC -> filteredSymptoms.sortedBy { it.severity }
                    }
                    
                    // Update UI state with the final list and current filter states
                    _uiState.update { currentState ->
                        currentState.copy(
                            symptoms = filteredSymptoms,
                            isLoading = false,
                            error = null,
                            selectedSymptomType = _symptomTypeFilter.value, // ensure UI reflects current type filter
                            selectedSeverity = severity
                        )
                    }

                } catch (e: Exception) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            error = "Failed to load symptoms: ${e.message}"
                        )
                    }
                }
            }.collect()
        }
    }

    open fun addSymptom(
        type: SymptomType,
        severity: Int,
        notes: String? = null,
        trigger: String? = null,
        peakFlow: Int? = null
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val symptom = Symptom(
                    timestamp = LocalDateTime.now(),
                    type = type,
                    severity = severity,
                    notes = notes,
                    trigger = trigger,
                    peakFlow = peakFlow
                )
                repository.insertSymptom(symptom)
                _uiState.update { it.copy(error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Failed to add symptom: ${e.message}"
                ) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    open fun updateSymptom(symptom: Symptom) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                repository.updateSymptom(symptom)
                _uiState.update { it.copy(error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Failed to update symptom: ${e.message}"
                ) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    open fun deleteSymptom(symptom: Symptom) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                repository.deleteSymptom(symptom)
                _uiState.update { it.copy(error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Failed to delete symptom: ${e.message}"
                ) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    open fun filterSymptomsByType(type: SymptomType?) {
        // _uiState.update { it.copy(selectedSymptomType = type) } // Old way
        _symptomTypeFilter.value = type
    }

    open fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    open fun setDateRange(start: LocalDateTime?, end: LocalDateTime?) {
        // Ensure end date includes the whole day for "between" comparisons
        val adjustedEnd = end?.withHour(23)?.withMinute(59)?.withSecond(59)
        _dateRange.value = start to adjustedEnd
    }

    open fun setSortOrder(sortOrder: SortOrder) {
        _sortOrder.value = sortOrder
    }

    open fun setSeverityFilter(severity: Int?) {
        _severityFilter.value = severity
    }

    open fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class SymptomDiaryUiState(
    val symptoms: List<Symptom> = emptyList(),
    val isLoading: Boolean = true,
    val selectedSymptomType: SymptomType? = null,
    val error: String? = null,
    val selectedSeverity: Int? = null
)

enum class SortOrder {
    DATE_DESC,
    DATE_ASC,
    SEVERITY_DESC,
    SEVERITY_ASC
} 