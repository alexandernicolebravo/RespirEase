package com.example.respiratoryhealthapp.screens.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.respiratoryhealthapp.data.database.AppDatabase
import com.example.respiratoryhealthapp.data.model.Reminder
import com.example.respiratoryhealthapp.data.repository.ReminderRepository
import com.example.respiratoryhealthapp.data.repository.RoomReminderRepository
import com.example.respiratoryhealthapp.data.repository.RoomSymptomRepository
import com.example.respiratoryhealthapp.data.repository.SymptomRepository
import com.example.respiratoryhealthapp.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val symptomRepository: SymptomRepository
    private val reminderRepository: ReminderRepository
    private val userProfileRepository: UserProfileRepository

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        val appDatabase = AppDatabase.getDatabase(application)
        symptomRepository = RoomSymptomRepository(appDatabase.symptomDao())
        reminderRepository = RoomReminderRepository(appDatabase.reminderDao())
        userProfileRepository = UserProfileRepository(application)

        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Flow for symptoms today
            val todayStart = LocalDate.now().atStartOfDay()
            val todayEnd = LocalDate.now().atTime(LocalTime.MAX)

            val symptomsTodayFlow = symptomRepository.getSymptomsBetweenDates(todayStart, todayEnd)
                .map { it.size }
                .catch { emit(0) }

            // Flow for upcoming reminders (e.g., next 3 active reminders)
            val upcomingRemindersFlow = reminderRepository.getActiveReminders()
                .map { reminders -> reminders.sortedBy { it.dateTime }.take(3) }
                .catch { emit(emptyList<Reminder>()) }

            // Flow for user name
            val userNameFlow = userProfileRepository.userProfileFlow
                .map { it.name }
                .catch { emit("User") }

            combine(
                symptomsTodayFlow,
                upcomingRemindersFlow,
                userNameFlow
            ) { symptomsCount, upcomingRemindersList, userName ->
                DashboardUiState(
                    userName = userName,
                    upcomingReminders = upcomingRemindersList,
                    isLoading = false,
                    error = null
                )
            }
            .catch { e ->
                _uiState.update {
                    it.copy(isLoading = false, error = "Failed to load dashboard data: ${e.message}")
                }
            }
            .collect { combinedState ->
                _uiState.value = combinedState
            }
        }
    }
}