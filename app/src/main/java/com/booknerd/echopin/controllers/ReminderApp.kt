package com.booknerd.echopin.controllers

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.booknerd.echopin.domain.model.Reminder
import com.booknerd.echopin.ui.theme.reminder.ReminderCreateObject
import com.booknerd.echopin.ui.theme.reminder.ReminderScreen
import kotlinx.coroutines.launch

@Composable
fun ReminderApp(snackbarHostState: SnackbarHostState) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    // This is where our actual Reminder objects live for now
    val reminders = remember { mutableStateListOf<Reminder>() }

    NavHost(
        navController = navController,
        startDestination = "list"
    ) {
        // Screen 1: The List
        composable("list") {
            ReminderScreen (
                reminders = reminders,
                onNavigateToCreate = { navController.navigate("create") }
            )
        }

        // Screen 2: The Creation Form
        composable("create") {
            ReminderCreateObject (
                onSave = { newReminder ->
                    reminders.add(newReminder)
                    navController.popBackStack()

                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Reminder '${newReminder.reminderName}' added")
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}