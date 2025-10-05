package com.example.respiratoryhealthapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.respiratoryhealthapp.data.model.RespiratoryCondition
import com.example.respiratoryhealthapp.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Extension property to delegate DataStore creation to the Context
val Context.userProfileDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_profile_prefs")

class UserProfileRepository(context: Context) {

    private val dataStore = context.userProfileDataStore

    private object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val SELECTED_CONDITION = stringPreferencesKey("selected_condition")
        val USER_AGE = stringPreferencesKey("user_age") // Using String for age to store Int? easily
        val USER_GENDER = stringPreferencesKey("user_gender")
        val MEDICATION_LIST = stringPreferencesKey("medication_list")
        val EMERGENCY_CONTACTS = stringPreferencesKey("emergency_contacts")
        val IS_DARK_MODE_ENABLED = booleanPreferencesKey("is_dark_mode_enabled")
        val MASTER_NOTIFICATIONS_ENABLED = booleanPreferencesKey("master_notifications_enabled")
    }

    val userProfileFlow: Flow<UserProfile> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val name = preferences[PreferencesKeys.USER_NAME] ?: "User" // Default name
            val conditionName = preferences[PreferencesKeys.SELECTED_CONDITION]
            val selectedCondition = try {
                conditionName?.let { RespiratoryCondition.valueOf(it) }
            } catch (_: IllegalArgumentException) {
                null // Handle case where stored enum name is invalid
            }
            val ageString = preferences[PreferencesKeys.USER_AGE]
            val age = ageString?.toIntOrNull()
            val gender = preferences[PreferencesKeys.USER_GENDER] ?: ""
            val medicationList = preferences[PreferencesKeys.MEDICATION_LIST] ?: ""
            val emergencyContacts = preferences[PreferencesKeys.EMERGENCY_CONTACTS] ?: ""
            val isDarkModeEnabled = preferences[PreferencesKeys.IS_DARK_MODE_ENABLED] == true
            val masterNotificationsEnabled = preferences[PreferencesKeys.MASTER_NOTIFICATIONS_ENABLED] != false

            UserProfile(
                name = name,
                selectedCondition = selectedCondition,
                age = age,
                gender = gender,
                medicationList = medicationList,
                emergencyContacts = emergencyContacts,
                isDarkModeEnabled = isDarkModeEnabled,
                masterNotificationsEnabled = masterNotificationsEnabled
            )
        }

    suspend fun updateUserName(name: String) {
        dataStore.edit {
            it[PreferencesKeys.USER_NAME] = name
        }
    }

    suspend fun updateSelectedCondition(condition: RespiratoryCondition?) {
        dataStore.edit {
            if (condition != null) {
                it[PreferencesKeys.SELECTED_CONDITION] = condition.name
            } else {
                it.remove(PreferencesKeys.SELECTED_CONDITION)
            }
        }
    }

    suspend fun updateAge(age: Int?) {
        dataStore.edit {
            if (age != null) {
                it[PreferencesKeys.USER_AGE] = age.toString()
            } else {
                it.remove(PreferencesKeys.USER_AGE)
            }
        }
    }

    suspend fun updateGender(gender: String) {
        dataStore.edit {
            it[PreferencesKeys.USER_GENDER] = gender
        }
    }

    suspend fun updateMedicationList(medicationList: String) {
        dataStore.edit {
            it[PreferencesKeys.MEDICATION_LIST] = medicationList
        }
    }

    suspend fun updateEmergencyContacts(emergencyContacts: String) {
        dataStore.edit {
            it[PreferencesKeys.EMERGENCY_CONTACTS] = emergencyContacts
        }
    }

    suspend fun updateDarkMode(isEnabled: Boolean) {
        dataStore.edit {
            it[PreferencesKeys.IS_DARK_MODE_ENABLED] = isEnabled
        }
    }

    suspend fun updateMasterNotificationsEnabled(isEnabled: Boolean) {
        dataStore.edit {
            it[PreferencesKeys.MASTER_NOTIFICATIONS_ENABLED] = isEnabled
        }
    }
    
    // Convenience function to update the whole profile if needed, though individual updates are fine
    suspend fun updateUserProfile(userProfile: UserProfile) {
        dataStore.edit {
            it[PreferencesKeys.USER_NAME] = userProfile.name
            userProfile.selectedCondition?.let { rc ->
                it[PreferencesKeys.SELECTED_CONDITION] = rc.name
            } ?: it.remove(PreferencesKeys.SELECTED_CONDITION)
            userProfile.age?.let { age ->
                it[PreferencesKeys.USER_AGE] = age.toString()
            } ?: it.remove(PreferencesKeys.USER_AGE)
            it[PreferencesKeys.USER_GENDER] = userProfile.gender
            it[PreferencesKeys.MEDICATION_LIST] = userProfile.medicationList
            it[PreferencesKeys.EMERGENCY_CONTACTS] = userProfile.emergencyContacts
            it[PreferencesKeys.IS_DARK_MODE_ENABLED] = userProfile.isDarkModeEnabled
            it[PreferencesKeys.MASTER_NOTIFICATIONS_ENABLED] = userProfile.masterNotificationsEnabled
        }
    }
} 