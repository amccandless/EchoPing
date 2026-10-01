package com.booknerd.echopin.ui.theme.reminder

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.booknerd.echopin.domain.model.Reminder
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderCreateObject(
    onSave: (Reminder) -> Unit,
    onBack: () -> Unit
) {
    // State for each form field
    var name by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("New Reminder") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Reminder Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text("Latitude") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text("Longitude") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = radius,
                onValueChange = { radius = it },
                label = { Text("Radius (meters)") },
                modifier = Modifier.fillMaxWidth()
            )

            // The actual message the notification will show
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Notification Text") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = {
                    // Create the domain object
                    val newReminder = Reminder(
                        reminderId = UUID.randomUUID(), // Usually handled by Room/DB autoincrement
                        reminderName = name,
                        locationLatitude = latitude.toDoubleOrNull() ?: 0.0,
                        locationLongitude = longitude.toDoubleOrNull() ?: 0.0,
                        locationRadius = radius.toDoubleOrNull() ?: 100.0,
                        notificationText = message
                    )
                    onSave(newReminder)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && message.isNotBlank()
            ) {
                Text("Save Reminder")
            }
        }
    }
}