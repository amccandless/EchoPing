package com.booknerd.echoping.ui.reminder

import android.text.format.DateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.booknerd.echoping.domain.model.MINUTES_PER_DAY
import com.booknerd.echoping.domain.model.RepeatType
import com.booknerd.echoping.domain.model.RepeatUnit
import com.booknerd.echoping.domain.model.Schedule
import com.booknerd.echoping.domain.model.describe
import com.booknerd.echoping.domain.model.formatStartDate
import com.booknerd.echoping.domain.model.formatMinute
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.TextStyle
import java.util.Locale

private const val DEFAULT_WINDOW_START = 17 * 60 // 5:00 PM
private const val DEFAULT_WINDOW_END = 0         // midnight

private val repeatLabels = listOf(
    RepeatType.ALWAYS to "Every time",
    RepeatType.ONCE to "Only once",
    RepeatType.DAILY to "Daily",
    RepeatType.WEEKLY to "Weekly",
    RepeatType.MONTHLY to "Monthly",
    RepeatType.CUSTOM to "Custom",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleSection(
    schedule: Schedule,
    onChange: (Schedule) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Repeat", style = MaterialTheme.typography.labelLarge)

        DropdownField(
            label = "How often",
            value = repeatLabels.first { it.first == schedule.repeat }.second,
            options = repeatLabels,
            optionLabel = { it.second },
            onSelect = { (type, _) ->
                // Week-based schedules need a day; start from today's.
                val days = when {
                    type == RepeatType.WEEKLY ->
                        setOf(schedule.daysOfWeek.minOrNull() ?: today.dayOfWeek)
                    type == RepeatType.CUSTOM && schedule.daysOfWeek.isEmpty() ->
                        setOf(today.dayOfWeek)
                    else -> schedule.daysOfWeek
                }
                // The start date only matters for Custom, so a newly chosen Custom starts today.
                val start = if (type == RepeatType.CUSTOM && schedule.repeat != RepeatType.CUSTOM) {
                    today
                } else schedule.startDate
                onChange(schedule.copy(repeat = type, daysOfWeek = days, startDate = start))
            }
        )

        when (schedule.repeat) {
            RepeatType.WEEKLY -> WeekdayPicker(
                selected = schedule.daysOfWeek,
                onToggle = { onChange(schedule.copy(daysOfWeek = setOf(it))) }
            )

            RepeatType.MONTHLY -> {
                DropdownField(
                    label = "Day of month",
                    value = schedule.dayOfMonth.toString(),
                    options = (1..31).toList(),
                    optionLabel = { it.toString() },
                    onSelect = { onChange(schedule.copy(dayOfMonth = it)) }
                )
                if (schedule.dayOfMonth > 28) {
                    Text(
                        "Uses the last day in shorter months",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            RepeatType.CUSTOM -> CustomRepeat(schedule, onChange)

            else -> Unit
        }

        TimeWindow(schedule, onChange)

        if (schedule.isValid) {
            Text(
                schedule.describe(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Text(
                "Pick at least one day",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomRepeat(schedule: Schedule, onChange: (Schedule) -> Unit) {
    var showDatePicker by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Every")
        FilledTonalIconButton(
            onClick = { onChange(schedule.copy(interval = (schedule.interval - 1).coerceAtLeast(1))) },
            enabled = schedule.interval > 1
        ) { Text("−") }
        Text(schedule.interval.toString(), style = MaterialTheme.typography.titleMedium)
        FilledTonalIconButton(
            onClick = { onChange(schedule.copy(interval = (schedule.interval + 1).coerceAtMost(52))) }
        ) { Text("+") }

        val units = listOf(RepeatUnit.DAYS to "Days", RepeatUnit.WEEKS to "Weeks")
        SingleChoiceSegmentedButtonRow(Modifier.weight(1f)) {
            units.forEachIndexed { index, (unit, label) ->
                SegmentedButton(
                    selected = schedule.unit == unit,
                    onClick = {
                        val days = if (unit == RepeatUnit.WEEKS && schedule.daysOfWeek.isEmpty()) {
                            setOf(schedule.startDate.dayOfWeek)
                        } else schedule.daysOfWeek
                        onChange(schedule.copy(unit = unit, daysOfWeek = days))
                    },
                    shape = SegmentedButtonDefaults.itemShape(index, units.size)
                ) { Text(label, maxLines = 1) }
            }
        }
    }

    if (schedule.unit == RepeatUnit.WEEKS) {
        WeekdayPicker(
            selected = schedule.daysOfWeek,
            onToggle = { day ->
                val days = if (day in schedule.daysOfWeek) schedule.daysOfWeek - day
                else schedule.daysOfWeek + day
                onChange(schedule.copy(daysOfWeek = days))
            }
        )
    }

    OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
        Text("Starting ${formatStartDate(schedule.startDate)}")
    }

    if (showDatePicker) {
        // DatePicker works in UTC millis.
        val state = rememberDatePickerState(
            initialSelectedDateMillis = schedule.startDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        onChange(schedule.copy(startDate = date))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state)
        }
    }
}

@Composable
private fun TimeWindow(schedule: Schedule, onChange: (Schedule) -> Unit) {
    var editing by remember { mutableStateOf<Boolean?>(null) } // true = start, false = end

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Only between certain times", style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = schedule.hasTimeWindow,
            onCheckedChange = { on ->
                onChange(
                    if (on) schedule.copy(
                        windowStartMinute = DEFAULT_WINDOW_START,
                        windowEndMinute = DEFAULT_WINDOW_END
                    ) else schedule.copy(windowStartMinute = null, windowEndMinute = null)
                )
            }
        )
    }

    val start = schedule.windowStartMinute
    val end = schedule.windowEndMinute
    if (start != null && end != null) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { editing = true }, modifier = Modifier.weight(1f)) {
                Text("From ${formatMinute(start)}")
            }
            OutlinedButton(onClick = { editing = false }, modifier = Modifier.weight(1f)) {
                Text("To ${formatMinute(end)}")
            }
        }

        editing?.let { isStart ->
            TimePickerDialog(
                initialMinute = if (isStart) start else end,
                onDismiss = { editing = null },
                onConfirm = { minute ->
                    onChange(
                        if (isStart) schedule.copy(windowStartMinute = minute)
                        else schedule.copy(windowEndMinute = minute)
                    )
                    editing = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(initialMinute: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    val context = LocalContext.current
    val state = rememberTimePickerState(
        initialHour = (initialMinute % MINUTES_PER_DAY) / 60,
        initialMinute = initialMinute % 60,
        is24Hour = DateFormat.is24HourFormat(context)
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour * 60 + state.minute) }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = { TimePicker(state) }
    )
}

@Composable
private fun WeekdayPicker(selected: Set<DayOfWeek>, onToggle: (DayOfWeek) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        DayOfWeek.entries.forEach { day ->
            val isSelected = day in selected
            Surface(
                onClick = { onToggle(day) },
                shape = CircleShape,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        day.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropdownField(
    label: String,
    value: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
