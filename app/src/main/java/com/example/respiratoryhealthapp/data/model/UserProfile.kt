package com.example.respiratoryhealthapp.data.model

// Enum for RespiratoryCondition is already defined in com.example.respiratoryhealthapp.data.model.RespiratoryCondition

data class UserProfile(
    val name: String = "",
    val selectedCondition: RespiratoryCondition? = null,
    val age: Int? = null,
    val gender: String = "",
    val medicationList: String = "",
    val emergencyContacts: String = "",
    val isDarkModeEnabled: Boolean = false,
    val masterNotificationsEnabled: Boolean = true
) 