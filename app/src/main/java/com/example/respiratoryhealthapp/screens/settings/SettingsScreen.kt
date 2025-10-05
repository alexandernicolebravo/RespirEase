@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.respiratoryhealthapp.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.respiratoryhealthapp.ui.theme.RespiratoryHealthAppTheme
import android.content.Intent
import android.provider.Settings
import android.os.Build

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel()
) {
    val isDarkMode by viewModel.isDarkModeEnabled.collectAsState()
    val isMasterNotificationsEnabled by viewModel.isMasterNotificationsEnabled.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            SettingsItem(
                icon = Icons.Filled.Brightness6,
                title = "Dark Mode",
                onClick = { viewModel.updateDarkMode(!isDarkMode) },
                trailingContent = {
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.updateDarkMode(it) }
                    )
                }
            )
            Divider()

            // Master Notification Toggle
            SettingsItem(
                icon = Icons.Filled.Notifications,
                title = "Enable All Notifications",
                subtitle = if (isMasterNotificationsEnabled) "All app notifications are active" else "All app notifications are currently disabled",
                onClick = { viewModel.updateMasterNotificationsEnabled(!isMasterNotificationsEnabled) },
                trailingContent = {
                    Switch(
                        checked = isMasterNotificationsEnabled,
                        onCheckedChange = { viewModel.updateMasterNotificationsEnabled(it) }
                    )
                }
            )
            Divider()

            // Link to System Notification Settings
            SettingsItem(
                icon = Icons.Filled.SettingsApplications,
                title = "System Notification Settings",
                subtitle = "Customize sound, vibration, and specific notification channels",
                onClick = {
                    val intent = Intent().apply {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        } else {
                            // Older versions: open general app details, user needs to find notifications
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = android.net.Uri.parse("package:${context.packageName}")
                        }
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Handle cases where settings cannot be opened, e.g. show a Toast
                        android.util.Log.e("SettingsScreen", "Error opening notification settings", e)
                    }
                }
            )
            Divider()

            // Add more settings items here
        }
    }
}

@Composable
fun SettingsItem(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 16.dp), // Consistent padding
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon?.let {
                Icon(imageVector = it, contentDescription = null, modifier = Modifier.padding(end = 16.dp))
            }
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                subtitle?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        trailingContent?.let {
            Box(modifier = Modifier.padding(start = 8.dp)) {
                 it()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    RespiratoryHealthAppTheme {
        // Use a state-driven preview to avoid ViewModel instantiation issues
        SettingsScreenWithState(isDarkMode = true, onDarkModeChange = {}, isMasterNotificationsEnabled = true, onMasterNotificationsChange = {})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenWithState(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    isMasterNotificationsEnabled: Boolean,
    onMasterNotificationsChange: (Boolean) -> Unit,
    // navController: NavController // Not strictly needed if just previewing content
) {
    val context = LocalContext.current // For system settings intent in preview
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { /* No-op for preview */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            SettingsItem(
                icon = Icons.Filled.Brightness6,
                title = "Dark Mode",
                onClick = { onDarkModeChange(!isDarkMode) },
                trailingContent = {
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = onDarkModeChange
                    )
                }
            )
            Divider()

            // Master Notification Toggle Preview
            SettingsItem(
                icon = Icons.Filled.Notifications,
                title = "Enable All Notifications",
                subtitle = if (isMasterNotificationsEnabled) "All app notifications are active" else "All app notifications are currently disabled",
                onClick = { onMasterNotificationsChange(!isMasterNotificationsEnabled) },
                trailingContent = {
                    Switch(
                        checked = isMasterNotificationsEnabled,
                        onCheckedChange = onMasterNotificationsChange
                    )
                }
            )
            Divider()

            // Link to System Notification Settings Preview
            SettingsItem(
                icon = Icons.Filled.SettingsApplications,
                title = "System Notification Settings",
                subtitle = "Customize sound, vibration, etc.",
                onClick = { /* Preview: Intent would not work here, but show UI */ }
            )
            Divider()

            // Add more settings items here for preview if desired
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsItemPreview() {
    RespiratoryHealthAppTheme {
        Column {
            SettingsItem(
                icon = Icons.Filled.Brightness6,
                title = "Dark Mode",
                trailingContent = { Switch(checked = true, onCheckedChange = {}) }
            )
            Divider()
            SettingsItem(
                title = "Another Setting",
                subtitle = "With a subtitle",
                onClick = {}
            )
            Divider()
        }
    }
} 