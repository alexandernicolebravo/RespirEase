package com.example.respiratoryhealthapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.respiratoryhealthapp.screens.breathingexercises.BreathingExercisesScreen
import com.example.respiratoryhealthapp.screens.breathingexercises.BreathingExercisesViewModel
import com.example.respiratoryhealthapp.screens.breathingexercises.ExerciseDetailScreen
import com.example.respiratoryhealthapp.screens.dashboard.DashboardScreen
import com.example.respiratoryhealthapp.screens.educationalcontent.ContentDetailScreen
import com.example.respiratoryhealthapp.screens.educationalcontent.EducationalContentScreen
import com.example.respiratoryhealthapp.screens.doctorsnotes.DoctorsNotesScreen
import com.example.respiratoryhealthapp.screens.profile.ProfileScreen
import com.example.respiratoryhealthapp.screens.reminders.RemindersScreen
import com.example.respiratoryhealthapp.screens.settings.SettingsScreen
import com.example.respiratoryhealthapp.screens.settings.SettingsViewModel
import com.example.respiratoryhealthapp.screens.symptomdiary.SymptomDiaryScreen
import com.example.respiratoryhealthapp.screens.symptomdiary.SymptomDiaryViewModel
import com.example.respiratoryhealthapp.screens.testresults.TestResultsScreen
import com.example.respiratoryhealthapp.screens.testresults.TestResultsViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Dashboard.route,
        modifier = modifier
    ) {
        composable(NavRoutes.Dashboard.route) {
            DashboardScreen(navController)
        }
        composable(NavRoutes.SymptomDiary.route) {
            val symptomViewModel: SymptomDiaryViewModel = viewModel()
            SymptomDiaryScreen(navController = navController, viewModel = symptomViewModel)
        }
        composable(NavRoutes.BreathingExercises.route) {
            val exerciseViewModel: BreathingExercisesViewModel = viewModel()
            BreathingExercisesScreen(navController = navController, viewModel = exerciseViewModel)
        }
        composable(
            route = NavRoutes.ExerciseDetail.route,
            arguments = listOf(navArgument("exerciseId") { type = NavType.LongType })
        ) {
            val exerciseId = it.arguments?.getLong("exerciseId")
            if (exerciseId != null) {
                ExerciseDetailScreen(navController = navController, exerciseId = exerciseId)
            }
        }
        composable(NavRoutes.Reminders.route) {
            RemindersScreen(navController)
        }
        composable(NavRoutes.EducationalContent.route) {
            EducationalContentScreen(navController)
        }
        composable(
            route = NavRoutes.EducationalContentDetail.route,
            arguments = listOf(
                navArgument("articleId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString("articleId") ?: ""
            ContentDetailScreen(
                navController = navController,
                articleId = articleId
            )
        }
        composable(NavRoutes.TestResults.route) {
            TestResultsScreen(navController = navController)
        }
        composable(NavRoutes.DoctorsNotes.route) {
            DoctorsNotesScreen(navController)
        }
        composable(NavRoutes.Profile.route) {
            ProfileScreen(navController)
        }
        composable(NavRoutes.Settings.route) {
            SettingsScreen(navController)
        }
    }
} 