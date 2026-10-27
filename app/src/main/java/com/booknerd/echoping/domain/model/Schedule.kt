package com.booknerd.echoping.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

enum class RepeatType {
    ALWAYS, ONCE, DAILY, WEEKLY, MONTHLY, CUSTOM
}

enum class RepeatUnit {
    DAYS, WEEKS
}

/**
 * When a reminder is allowed to fire. Outside of ALWAYS, it fires at most once per window.
 * Window times are minutes from midnight; an end of 0 means midnight at the end of the day,
 * and a start after the end means the window runs overnight.
 */
data class Schedule(
    val repeat: RepeatType = RepeatType.ALWAYS,
    val interval: Int = 1,                          // CUSTOM: every N units
    val unit: RepeatUnit = RepeatUnit.WEEKS,        // CUSTOM
    val daysOfWeek: Set<DayOfWeek> = emptySet(),    // WEEKLY (one day) / CUSTOM weeks
    val dayOfMonth: Int = 1,                        // MONTHLY
    val startDate: LocalDate = LocalDate.now(),     // anchor for "every other"
    val windowStartMinute: Int? = null,
    val windowEndMinute: Int? = null,
) {
    val hasTimeWindow: Boolean get() = windowStartMinute != null && windowEndMinute != null

    /** Weekly and custom-weeks schedules need at least one day picked. */
    val isValid: Boolean
        get() = when {
            repeat == RepeatType.WEEKLY -> daysOfWeek.isNotEmpty()
            repeat == RepeatType.CUSTOM && unit == RepeatUnit.WEEKS -> daysOfWeek.isNotEmpty()
            else -> true
        }
}
