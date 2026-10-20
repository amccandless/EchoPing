package com.booknerd.echopin.controllers

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.booknerd.echopin.domain.model.Reminder
import com.booknerd.echopin.ui.reminder.ReminderCreateObject
import com.booknerd.echopin.ui.reminder.ReminderScreen
import com.booknerd.echopin.ui.reminder.ReminderViewModel
import kotlinx.coroutines.launch

@Composable
fun ReminderApp(snackbarHostState: SnackbarHostState) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    val reminderViewModel: ReminderViewModel = hiltViewModel()
    val reminders by reminderViewModel.reminders.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = "list"
    ) {
        // Screen 1: The List
        composable("list") {
            ReminderScreen (
                reminders = reminders,
                snackbarHostState = snackbarHostState,
                onNavigateToCreate = { navController.navigate("create")
                }
            )
        }

        // Screen 2: The Creation Form
        composable("create") {
            ReminderCreateObject(
                onSave = { newReminder ->
                    reminderViewModel.addReminder(newReminder)
                    navController.popBackStack()

                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Reminder '${newReminder.reminderName}' added")
                    }
                },
                onBack = { navController.popBackStack() },
                onSearch = { query -> reminderViewModel.searchAddress(query) }
            )
        }
    }
}