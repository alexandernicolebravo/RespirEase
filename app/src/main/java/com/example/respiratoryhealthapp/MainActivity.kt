package com.example.respiratoryhealthapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.respiratoryhealthapp.data.repository.UserProfileRepository
import com.example.respiratoryhealthapp.navigation.NavGraph
import com.example.respiratoryhealthapp.navigation.NavRoutes
import com.example.respiratoryhealthapp.notifications.NotificationHelper
import com.example.respiratoryhealthapp.ui.theme.RespiratoryHealthAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    @get:SuppressLint("InlinedApi")
    private var showPermissionRationaleDialog by mutableStateOf(false)
    private var showExactAlarmRationaleDialog by mutableStateOf(false)
    private lateinit var userProfileRepository: UserProfileRepository
    private var isReady by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "POST_NOTIFICATIONS permission granted.")
            requestExactAlarmPermission()
        } else {
            Log.d(TAG, "POST_NOTIFICATIONS permission denied.")
            requestExactAlarmPermission()
        }
    }

    private val requestExactAlarmSettingLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        checkAndLogExactAlarmPermission()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle splash screen - keep it visible until app is ready
        installSplashScreen().apply {
            setKeepOnScreenCondition { !isReady }
        }

        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannel(this)
        userProfileRepository = UserProfileRepository(applicationContext)

        // Simulate some initial loading
        lifecycleScope.launch {
            delay(3500) // Add a longer delay to show splash screen (3.5 seconds)
            isReady = true
        }

        val isDarkModeFlow = userProfileRepository.userProfileFlow
            .map { it.isDarkModeEnabled }
            .stateIn(
                scope = lifecycleScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false
            )

        setContent {
            val isDarkMode by isDarkModeFlow.collectAsState()

            RespiratoryHealthAppTheme(
                darkTheme = isDarkMode
            ) {
                // Splash animation
                AnimatedVisibility(
                    visible = isReady,
                    enter = fadeIn(animationSpec = tween(700)) + scaleIn(initialScale = 0.8f, animationSpec = tween(700))
            ) {
                MainScreen()
                }

                if (showPermissionRationaleDialog) {
                    NotificationPermissionRationaleDialog(
                        onConfirm = {
                            showPermissionRationaleDialog = false
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        },
                        onDismiss = {
                            showPermissionRationaleDialog = false
                            Log.i(TAG, "User dismissed notification permission rationale.")
                            requestExactAlarmPermission()
                        }
                    )
                }

                if (showExactAlarmRationaleDialog) {
                    ExactAlarmPermissionRationaleDialog(
                        onConfirm = {
                            showExactAlarmRationaleDialog = false
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.fromParts("package", packageName, null)
                            }
                            try {
                                requestExactAlarmSettingLauncher.launch(intent)
                            } catch (e: Exception) {
                                Log.e(TAG, "Could not open exact alarm settings", e)
                            }
                        },
                        onDismiss = {
                            showExactAlarmRationaleDialog = false
                            Log.i(TAG, "User dismissed exact alarm permission rationale.")
                        }
                    )
                }
            }
        }
        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "Notification permission already granted.")
                    requestExactAlarmPermission()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Log.d(TAG, "Showing notification permission rationale.")
                    showPermissionRationaleDialog = true
                }
                else -> {
                    Log.d(TAG, "Requesting notification permission for the first time or was denied permanently.")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            requestExactAlarmPermission()
        }
    }

    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (alarmManager.canScheduleExactAlarms()) {
                Log.d(TAG, "SCHEDULE_EXACT_ALARM permission already granted.")
            } else {
                Log.d(TAG, "SCHEDULE_EXACT_ALARM permission not granted. Showing rationale.")
                showExactAlarmRationaleDialog = true
            }
        } else {
            Log.d(TAG, "SCHEDULE_EXACT_ALARM not required or handled differently before Android S.")
        }
    }

    private fun checkAndLogExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (alarmManager.canScheduleExactAlarms()) {
                Log.d(TAG, "SCHEDULE_EXACT_ALARM permission is NOW granted after returning from settings.")
            } else {
                Log.d(TAG, "SCHEDULE_EXACT_ALARM permission is STILL NOT granted after returning from settings.")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPermissionRationaleDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permission Required") },
        text = { Text("To ensure you receive timely reminders for medications, appointments, and exercises, RespirEase needs permission to send you notifications. Please grant this permission to enable the Reminders feature.") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Grant Permission")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not Now")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExactAlarmPermissionRationaleDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Additional Permission Needed") },
        text = { Text("For reminders to work precisely as scheduled (e.g., for specific medication times), RespirEase needs permission to schedule exact alarms. Please grant this permission in the next step in your system settings.") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Go to Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not Now")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val items = listOf(
        Triple("Symptoms", Icons.AutoMirrored.Filled.List, NavRoutes.SymptomDiary.route),
        Triple("Exercises", Icons.Filled.FitnessCenter, NavRoutes.BreathingExercises.route),
        Triple("Dashboard", Icons.Filled.Home, NavRoutes.Dashboard.route),
        Triple("Learn", Icons.Filled.School, NavRoutes.EducationalContent.route),
        Triple("Profile", Icons.Filled.Person, NavRoutes.Profile.route)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, (title, defaultIcon, route) ->
                    val isSelected = currentRoute == route
                    val iconToShow = if (title == "Dashboard") {
                        if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                    } else {
                        defaultIcon
                    }
                    val iconSize = if (title == "Dashboard" && isSelected) 28.dp else 24.dp

                    NavigationBarItem(
                        icon = { Icon(iconToShow, contentDescription = title, modifier = Modifier.size(iconSize)) },
                        label = { Text(title) },
                        selected = isSelected,
                        onClick = {
                            if (route == NavRoutes.Dashboard.route) {
                                navController.navigate(NavRoutes.Dashboard.route) {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            } else {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}