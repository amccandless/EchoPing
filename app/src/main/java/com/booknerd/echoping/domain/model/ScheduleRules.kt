package com.booknerd.echoping.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

const val MINUTES_PER_DAY = 24 * 60

/** True if this schedule can fire on [date], ignoring the time window. */
fun Schedule.matchesDay(date: LocalDate): Boolean = when (repeat) {
    RepeatType.ALWAYS, RepeatType.ONCE, RepeatType.DAILY -> true
    RepeatType.WEEKLY -> date.dayOfWeek in daysOfWeek
    RepeatType.MONTHLY -> date.dayOfMonth == minOf(dayOfMonth, date.lengthOfMonth())
    RepeatType.CUSTOM -> {
        val every = interval.coerceAtLeast(1).toLong()
        !date.isBefore(startDate) && when (unit) {
            RepeatUnit.DAYS -> ChronoUnit.DAYS.between(startDate, date) % every == 0L
            RepeatUnit.WEEKS -> date.dayOfWeek in daysOfWeek &&
                ChronoUnit.WEEKS.between(weekOf(startDate), weekOf(date)) % every == 0L
        }
    }
}

/**
 * Start of the window that [now] falls in, or null when [now] is outside every window.
 * Without a time range the window is the whole matching day. In an overnight window,
 * the hours after midnight belong to the previous day's window.
 */
fun Schedule.windowStart(now: LocalDateTime): LocalDateTime? {
    val date = now.toLocalDate()
    val start = windowStartMinute
    val rawEnd = windowEndMinute
    if (start == null || rawEnd == null) {
        return if (matchesDay(date)) date.atStartOfDay() else null
    }

    val end = if (rawEnd == 0) MINUTES_PER_DAY else rawEnd
    val minute = now.hour * 60 + now.minute

    if (start < end) {
        return if (minute in start until end && matchesDay(date)) date.atMinute(start) else null
    }
    if (minute >= start && matchesDay(date)) return date.atMinute(start)
    val yesterday = date.minusDays(1)
    if (minute < end && matchesDay(yesterday)) return yesterday.atMinute(start)
    return null
}

/** Whether an arrive/leave at [now] should notify, given when the reminder last fired. */
fun Schedule.shouldNotify(lastTriggeredAt: LocalDateTime?, now: LocalDateTime): Boolean {
    val windowStart = windowStart(now) ?: return false
    return when (repeat) {
        RepeatType.ALWAYS -> true
        RepeatType.ONCE -> lastTriggeredAt == null
        else -> lastTriggeredAt == null || lastTriggeredAt.isBefore(windowStart)
    }
}

/** Human-readable summary, e.g. "Every 2 weeks on Tue · 5:00 PM – 12:00 AM". */
fun Schedule.describe(): String {
    val days = DayOfWeek.entries.filter { it in daysOfWeek }
    val starting = "starting ${formatStartDate(startDate)}"

    val base = when (repeat) {
        RepeatType.ALWAYS -> "Every time"
        RepeatType.ONCE -> "Only once"
        RepeatType.DAILY -> "Every day"
        RepeatType.WEEKLY -> "Every ${days.joinToString { it.getDisplayName(TextStyle.FULL, Locale.getDefault()) }}"
        RepeatType.MONTHLY -> "Monthly on the ${ordinal(dayOfMonth)}"
        RepeatType.CUSTOM -> when (unit) {
            RepeatUnit.DAYS ->
                if (interval <= 1) "Every day, $starting" else "Every $interval days, $starting"
            RepeatUnit.WEEKS -> {
                val dayList = days.joinToString { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
                val every = if (interval <= 1) "Every week" else "Every $interval weeks"
                "$every on $dayList, $starting"
            }
        }
    }

    if (!hasTimeWindow) return base
    return "$base · ${formatMinute(windowStartMinute!!)} – ${formatMinute(windowEndMinute!!)}"
}

/** "today", or a medium date like "Nov 3, 2026". */
fun formatStartDate(date: LocalDate, today: LocalDate = LocalDate.now()): String =
    if (date == today) "today" else date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

fun formatMinute(minute: Int): String {
    val m = minute % MINUTES_PER_DAY
    return LocalTime.of(m / 60, m % 60).format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
}

private fun weekOf(date: LocalDate): LocalDate =
    date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

private fun LocalDate.atMinute(minute: Int): LocalDateTime = atStartOfDay().plusMinutes(minute.toLong())

private fun ordinal(n: Int): String {
    val suffix = if (n % 100 in 11..13) "th" else when (n % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
    return "$n$suffix"
}
