package com.example.respiratoryhealthapp.navigation

sealed class NavRoutes(val route: String) {
    object Dashboard : NavRoutes("dashboard")
    object SymptomDiary : NavRoutes("symptom_diary")
    data object BreathingExercises : NavRoutes("breathing_exercises")
    data object ExerciseDetail : NavRoutes("exercise_detail/{exerciseId}") {
        fun exerciseDetailWithArg(exerciseId: Long): String = "exercise_detail/$exerciseId"
    }
    data object Reminders : NavRoutes("reminders")
    data object EducationalContent : NavRoutes("educational_content")
    object EducationalContentDetail : NavRoutes("educational_content_detail/{articleId}") {
        fun createRoute(articleId: String) = "educational_content_detail/$articleId"
    }
    data object ContentDetail : NavRoutes("content_detail/{articleId}") {
        fun contentDetailWithArg(articleId: String) = "content_detail/$articleId"
    }
    data object TestResults : NavRoutes("test_results")
    object DoctorsNotes : NavRoutes("doctors_notes")
    object Profile : NavRoutes("profile")
    object Settings : NavRoutes("settings")
} 