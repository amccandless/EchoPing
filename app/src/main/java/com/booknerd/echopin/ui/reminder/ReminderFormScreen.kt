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
import com.booknerd.echopin.domain.model.TriggerType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import com.booknerd.echopin.data.geocoding.GeoResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderFormScreen(
    title: String,
    initial: Reminder? = null,
    onSave: (Reminder) -> Unit,
    onBack: () -> Unit,
    onSearch: suspend (String) -> List<GeoResult>
) {
    var name by remember { mutableStateOf(initial?.reminderName ?: "") }
    var radius by remember {
        mutableStateOf(initial?.locationRadius?.toInt()?.toString() ?: "150")
    }
    var message by remember { mutableStateOf(initial?.notificationText ?: "") }
    var triggerType by remember { mutableStateOf(initial?.triggerType ?: TriggerType.ENTER) }

    var selected by remember {
        mutableStateOf(
            initial?.let {
                GeoResult(
                    label = it.address.ifBlank { "${it.locationLatitude}, ${it.locationLongitude}" },
                    latitude = it.locationLatitude,
                    longitude = it.locationLongitude
                )
            }
        )
    }
    var addressQuery by remember { mutableStateOf(selected?.label ?: "") }
    var results by remember { mutableStateOf<List<GeoResult>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }
    var searched by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val rad = radius.toFloatOrNull()?.takeIf { it >= 100f }
    val isValid = name.isNotBlank() && message.isNotBlank() && selected != null && rad != null

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
                title = { Text(title) },
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

            OutlinedTextField(
                value = addressQuery,
                onValueChange = {
                    addressQuery = it
                    selected = null
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
                        Text(result.label, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
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

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Remind me", style = MaterialTheme.typography.labelLarge)
                val options = listOf(
                    TriggerType.ENTER to "When I arrive",
                    TriggerType.EXIT to "When I leave",
                    TriggerType.BOTH to "Both"
                )
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    options.forEachIndexed { index, (type, label) ->
                        SegmentedButton(
                            selected = triggerType == type,
                            onClick = { triggerType = type },
                            shape = SegmentedButtonDefaults.itemShape(index, options.size)
                        ) {
                            Text(label, maxLines = 1)
                        }
                    }
                }
            }

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
                    val r = rad ?: return@Button
                    val reminder = initial?.copy(
                        reminderName = name.trim(),
                        notificationText = message.trim(),
                        address = place.label,
                        locationLatitude = place.latitude,
                        locationLongitude = place.longitude,
                        locationRadius = r,
                        triggerType = triggerType
                    ) ?: Reminder(
                        reminderName = name.trim(),
                        notificationText = message.trim(),
                        address = place.label,
                        locationLatitude = place.latitude,
                        locationLongitude = place.longitude,
                        locationRadius = r,
                        triggerType = triggerType
                    )
                    onSave(reminder)
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (initial == null) "Save Reminder" else "Save Changes")
            }
        }
    }
}