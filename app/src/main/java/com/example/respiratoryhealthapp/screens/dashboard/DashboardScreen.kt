package com.example.respiratoryhealthapp.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.respiratoryhealthapp.data.model.Reminder
import com.example.respiratoryhealthapp.data.model.ReminderRecurrence
import com.example.respiratoryhealthapp.data.model.ReminderType
import com.example.respiratoryhealthapp.navigation.NavRoutes
import com.example.respiratoryhealthapp.ui.theme.RespiratoryHealthAppTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    DashboardScreenWithState(uiState = uiState, navController = navController)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreenWithState(
    uiState: DashboardUiState,
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RespirEase") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Greeting Card with gradient background
                    WelcomeCard(userName = uiState.userName)
                    
                    QuickActionButtons(navController = navController)
                    
                    UpcomingRemindersSection(navController = navController, reminders = uiState.upcomingReminders)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionCard(
                            title = "Doctor Notes",
                            icon = Icons.AutoMirrored.Filled.Notes,
                            contentDescription = "View Doctor Notes",
                            onClick = { navController.navigate(NavRoutes.DoctorsNotes.route) },
                            modifier = Modifier.weight(1f)
                        )
                        
                        ActionCard(
                            title = "Test Results",
                            icon = Icons.Filled.Science,
                            contentDescription = "View Test Results",
                            onClick = { navController.navigate(NavRoutes.TestResults.route) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeCard(userName: String) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary
        )
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Text(
                    text = "Welcome back,",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "How are you feeling today?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun QuickActionButtons(navController: NavController) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                icon = Icons.Filled.EditNote,
                text = "Log Symptoms",
                onClick = { navController.navigate(NavRoutes.SymptomDiary.route) },
                modifier = Modifier.weight(1f)
            )
            
            QuickActionButton(
                icon = Icons.Filled.Air,
                text = "Breathing Exercise",
                onClick = { navController.navigate(NavRoutes.BreathingExercises.route) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionCard(
    title: String,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun UpcomingRemindersSection(
    navController: NavController,
    reminders: List<Reminder>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Upcoming Reminders",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            TextButton(onClick = { navController.navigate(NavRoutes.Reminders.route) }) {
                Text("View All")
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "View All Reminders")
            }
        }
        
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            if (reminders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No upcoming reminders",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    reminders.forEachIndexed { index, reminder ->
                        ReminderRow(reminder = reminder, navController = navController)
                        if (index < reminders.size - 1) {
                            Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderRow(reminder: Reminder, navController: NavController) {
    val backgroundColor = when (reminder.type) {
        ReminderType.MEDICATION -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ReminderType.APPOINTMENT -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
        ReminderType.EXERCISE -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
        ReminderType.CUSTOM -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate(NavRoutes.Reminders.route) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(backgroundColor)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = reminder.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = reminder.type.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = reminder.dateTime.format(DateTimeFormatter.ofPattern("hh:mm a")),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// --- Previews ---
@Preview(showBackground = true, name = "Dashboard Screen - Populated")
@Composable
fun DashboardScreenPreview() {
    val sampleReminders = listOf(
        Reminder(id = 1, title = "Morning Meds", dateTime = LocalDateTime.now().plusHours(1), type = ReminderType.MEDICATION, recurrence = ReminderRecurrence.DAILY),
        Reminder(id = 2, title = "Evening Walk", dateTime = LocalDateTime.now().plusHours(4), type = ReminderType.EXERCISE, recurrence = ReminderRecurrence.NONE)
    )
    RespiratoryHealthAppTheme {
        DashboardScreenWithState(
            uiState = DashboardUiState(userName = "Tester", upcomingReminders = sampleReminders),
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true, name = "Dashboard Screen - No Reminders")
@Composable
fun DashboardScreenNoRemindersPreview() {
    RespiratoryHealthAppTheme {
        DashboardScreenWithState(
            uiState = DashboardUiState(userName = "Tester", upcomingReminders = emptyList()),
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true, name = "Dashboard Screen - Loading")
@Composable
fun DashboardScreenPreview_Loading() {
    RespiratoryHealthAppTheme {
        DashboardScreenWithState(uiState = DashboardUiState(isLoading = true), navController = rememberNavController())
    }
}

@Preview(showBackground = true, name = "Dashboard Screen - Error")
@Composable
fun DashboardScreenErrorPreview() {
    RespiratoryHealthAppTheme {
        DashboardScreenWithState(
            uiState = DashboardUiState(error = "Network connection lost!"),
            navController = rememberNavController()
        )
    }
} 