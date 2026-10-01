package com.booknerd.echopin.ui.theme.reminder

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.booknerd.echopin.domain.model.Reminder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderCreateObject(
    onSave: (Reminder) -> Unit,
    onBack: () -> Unit
) {
    // 1. State (what you already have)
    var name by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    // 2. Validation (new): parsed values, null if invalid
    val lat = latitude.toDoubleOrNull()?.takeIf { it in -90.0..90.0 }
    val lon = longitude.toDoubleOrNull()?.takeIf { it in -180.0..180.0 }
    val rad = radius.toFloatOrNull()?.takeIf { it >= 100f }
    val isValid = name.isNotBlank() && message.isNotBlank() &&
            lat != null && lon != null && rad != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Reminder") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
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
                    isError = latitude.isNotEmpty() && lat == null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text("Longitude") },
                    isError = longitude.isNotEmpty() && lon == null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = radius,
                onValueChange = { radius = it },
                label = { Text("Radius (meters, min 100)") },
                isError = radius.isNotEmpty() && rad == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Notification Text") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = {
                    onSave(
                        Reminder(
                            reminderName = name,
                            notificationText = message,
                            locationLatitude = lat!!,
                            locationLongitude = lon!!,
                            locationRadius = rad!!
                        )
                    )
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Reminder")
            }
        }
    }
}