package com.booknerd.echopin.ui.reminder

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.booknerd.echopin.domain.model.Reminder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import com.booknerd.echopin.data.geocoding.GeoResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderCreateObject(
    onSave: (Reminder) -> Unit,
    onBack: () -> Unit,
    onSearch: suspend (String) -> List<GeoResult>
) {
    var name by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf("150") }
    var message by remember { mutableStateOf("") }

    // Address search state
    var addressQuery by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<GeoResult>>(emptyList()) }
    var selected by remember { mutableStateOf<GeoResult?>(null) }
    var searching by remember { mutableStateOf(false) }
    var searched by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Validation
    val rad = radius.toFloatOrNull()?.takeIf { it >= 100f }
    val isValid = name.isNotBlank() && message.isNotBlank() &&
            selected != null && rad != null

    fun runSearch() {
        scope.launch {
            searching = true
            results = onSearch(addressQuery)
            searching = false
            searched = true
        }
    }

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

            // Address search
            OutlinedTextField(
                value = addressQuery,
                onValueChange = {
                    addressQuery = it
                    selected = null          // editing the text invalidates the earlier pick
                    searched = false
                },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { runSearch() }),
                trailingIcon = {
                    IconButton(onClick = { runSearch() }) {
                        Icon(Icons.Filled.Search, "Search")
                    }
                }
            )

            if (searching) LinearProgressIndicator(Modifier.fillMaxWidth())

            if (selected == null) {
                results.forEach { result ->
                    TextButton(
                        onClick = {
                            selected = result
                            addressQuery = result.label
                            results = emptyList()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            result.label,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }
                }
                if (searched && !searching && results.isEmpty()) {
                    Text(
                        "No matches. Try adding the city or ZIP code.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
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
                    val place = selected ?: return@Button
                    onSave(
                        Reminder(
                            reminderName = name,
                            notificationText = message,
                            locationLatitude = place.latitude,
                            locationLongitude = place.longitude,
                            locationRadius = rad ?: return@Button
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