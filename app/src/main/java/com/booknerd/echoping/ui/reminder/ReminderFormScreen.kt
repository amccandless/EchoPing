package com.booknerd.echoping.ui.reminder

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.booknerd.echoping.domain.model.Reminder
import com.booknerd.echoping.domain.model.Schedule
import com.booknerd.echoping.domain.model.TriggerType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import com.booknerd.echoping.data.geocoding.GeoResult
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import java.time.LocalDate

private const val MIN_QUERY_LENGTH = 3
private const val SEARCH_DEBOUNCE_MS = 500L

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
    var radius by remember { mutableFloatStateOf(initial?.locationRadius ?: 150f) }
    var message by remember { mutableStateOf(initial?.notificationText ?: "") }
    var triggerType by remember { mutableStateOf(initial?.triggerType ?: TriggerType.ENTER) }
    var schedule by remember {
        mutableStateOf(initial?.schedule ?: Schedule(dayOfMonth = LocalDate.now().dayOfMonth))
    }

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

    val isValid = name.isNotBlank() && message.isNotBlank() && selected != null && schedule.isValid

    // Search as the user types. A new keystroke cancels the pending search, so it only
    // runs once they pause.
    LaunchedEffect(addressQuery) {
        if (selected != null || addressQuery.trim().length < MIN_QUERY_LENGTH) {
            results = emptyList()
            searching = false
            return@LaunchedEffect
        }
        delay(SEARCH_DEBOUNCE_MS)
        searching = true
        try {
            results = onSearch(addressQuery.trim())
            searched = true
        } finally {
            searching = false
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
                placeholder = { Text("Start typing an address or place") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                trailingIcon = {
                    if (addressQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            addressQuery = ""
                            selected = null
                            searched = false
                        }) {
                            Icon(Icons.Filled.Clear, "Clear address")
                        }
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

            val place = selected
            if (place != null) {
                RadiusPicker(
                    center = LatLng(place.latitude, place.longitude),
                    radius = radius,
                    onRadiusChange = { radius = it },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    "Pick an address to set the reminder area",
                    style = MaterialTheme.typography.bodySmall
                )
            }

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

            ScheduleSection(
                schedule = schedule,
                onChange = { schedule = it },
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
                    val reminder = initial?.copy(
                        reminderName = name.trim(),
                        notificationText = message.trim(),
                        address = place.label,
                        locationLatitude = place.latitude,
                        locationLongitude = place.longitude,
                        locationRadius = radius,
                        triggerType = triggerType,
                        schedule = schedule
                    ) ?: Reminder(
                        reminderName = name.trim(),
                        notificationText = message.trim(),
                        address = place.label,
                        locationLatitude = place.latitude,
                        locationLongitude = place.longitude,
                        locationRadius = radius,
                        triggerType = triggerType,
                        schedule = schedule
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