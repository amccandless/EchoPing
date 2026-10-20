package com.booknerd.echopin.controllers

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.booknerd.echopin.domain.model.Reminder
import com.booknerd.echopin.ui.reminder.ReminderFormScreen
import com.booknerd.echopin.ui.reminder.ReminderScreen
import com.booknerd.echopin.ui.reminder.ReminderViewModel
import com.booknerd.echopin.ui.reminder.ReminderDetailScreen
import kotlinx.coroutines.launch

@Composable
fun ReminderApp(snackbarHostState: SnackbarHostState) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    val viewModel: ReminderViewModel = hiltViewModel()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()

    fun showMessage(message: String) {
        coroutineScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    fun deleteWithUndo(reminder: Reminder) {
        viewModel.deleteReminder(reminder)
        coroutineScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = "Deleted '${reminder.reminderName}'",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.addReminder(reminder)
        }
    }

    NavHost(navController = navController, startDestination = "list") {

        composable("list") {
            ReminderScreen(
                reminders = reminders,
                snackbarHostState = snackbarHostState,
                onNavigateToCreate = { navController.navigate("create") },
                onReminderClick = { navController.navigate("detail/${it.reminderId}") },
                onToggleActive = { reminder, active -> viewModel.setActive(reminder, active) },
                onDelete = { deleteWithUndo(it) }
            )
        }

        composable("create") {
            ReminderFormScreen(
                title = "New Reminder",
                onSave = { newReminder ->
                    viewModel.addReminder(newReminder)
                    navController.popBackStack()
                    showMessage("Reminder '${newReminder.reminderName}' added")
                },
                onBack = { navController.popBackStack() },
                onSearch = { query -> viewModel.searchAddress(query) }
            )
        }

        composable(
            route = "detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString("id")
            val reminder = reminders.find { it.reminderId == id }

            ReminderDetailScreen(
                reminder = reminder,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate("edit/$id") },
                onDelete = {
                    reminder?.let { deleteWithUndo(it) }
                    navController.popBackStack()
                },
                onToggleActive = { active -> reminder?.let { viewModel.setActive(it, active) } }
            )
        }

        composable(
            route = "edit/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString("id")
            val reminder = reminders.find { it.reminderId == id }

            if (reminder != null) {
                ReminderFormScreen(
                    title = "Edit Reminder",
                    initial = reminder,
                    onSave = { updated ->
                        viewModel.updateReminder(updated)
                        navController.popBackStack()
                        showMessage("Reminder updated")
                    },
                    onBack = { navController.popBackStack() },
                    onSearch = { query -> viewModel.searchAddress(query) }
                )
            }
        }
    }
}