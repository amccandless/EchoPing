package com.booknerd.echoping.ui.reminder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.booknerd.echoping.domain.model.Reminder
import com.booknerd.echoping.domain.model.TriggerType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailScreen(
    reminder: Reminder?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: (Boolean) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog && reminder != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete reminder?") },
            text = { Text("\"${reminder.reminderName}\" will be removed.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminder") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (reminder != null) {
                        IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, "Edit") }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (reminder == null) {
            Box(Modifier.fillMaxSize().padding(padding))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(reminder.reminderName, style = MaterialTheme.typography.headlineSmall)

            DetailRow("Notification text", reminder.notificationText)
            DetailRow(
                "Location",
                reminder.address.ifBlank { "${reminder.locationLatitude}, ${reminder.locationLongitude}" }
            )
            DetailRow("Radius", "${reminder.locationRadius.toInt()} meters")
            DetailRow(
                "Triggers",
                when (reminder.triggerType) {
                    TriggerType.ENTER -> "When I arrive"
                    TriggerType.EXIT -> "When I leave"
                    TriggerType.BOTH -> "When I arrive or leave"
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Active", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = reminder.isActive, onCheckedChange = onToggleActive)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}