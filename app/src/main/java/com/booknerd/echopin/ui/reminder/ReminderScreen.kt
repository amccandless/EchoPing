package com.booknerd.echopin.ui.reminder

//item UI
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.booknerd.echopin.domain.model.Reminder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun ReminderScreen(
    reminders: List<Reminder>,
    snackbarHostState: SnackbarHostState,
    onNavigateToCreate: () -> Unit,
    onReminderClick: (Reminder) -> Unit,
    onToggleActive: (Reminder, Boolean) -> Unit,
    onDelete: (Reminder) -> Unit
) {
    var pendingDelete by remember { mutableStateOf<Reminder?>(null) }

    pendingDelete?.let { reminder ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete reminder?") },
            text = { Text("\"${reminder.reminderName}\" will be removed.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(reminder)
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = { AddNew_FAB { onNavigateToCreate() } }
    ) { paddingValues ->
        if (reminders.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No reminders yet. Tap + to add one.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items = reminders, key = { it.reminderId }) { reminder ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onReminderClick(reminder) }
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(reminder.reminderName, style = MaterialTheme.typography.titleMedium)
                                Text(reminder.notificationText, style = MaterialTheme.typography.bodyMedium)
                                if (reminder.address.isNotBlank()) {
                                    Text(
                                        reminder.address,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = reminder.isActive,
                                onCheckedChange = { onToggleActive(reminder, it) }
                            )
                            IconButton(onClick = { pendingDelete = reminder }) {
                                Icon(Icons.Filled.Delete, "Delete reminder")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddNew_FAB(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
    ) {
        Icon(Icons.Filled.Add, "Add new reminder")
    }
}