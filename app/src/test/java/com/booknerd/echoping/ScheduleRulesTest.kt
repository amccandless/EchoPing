package com.booknerd.echoping

import com.booknerd.echoping.domain.model.RepeatType
import com.booknerd.echoping.domain.model.RepeatUnit
import com.booknerd.echoping.domain.model.Schedule
import com.booknerd.echoping.domain.model.shouldNotify
import com.booknerd.echoping.domain.model.windowStart
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

class ScheduleRulesTest {

    // 2026-10-27 is a Tuesday.
    private val tuesday = LocalDate.of(2026, 10, 27)

    private val everyOtherTuesdayAfter5 = Schedule(
        repeat = RepeatType.CUSTOM,
        interval = 2,
        unit = RepeatUnit.WEEKS,
        daysOfWeek = setOf(DayOfWeek.TUESDAY),
        startDate = tuesday,
        windowStartMinute = 17 * 60,
        windowEndMinute = 0
    )

    @Test
    fun everyOtherTuesdayAfter5_firesOnAlternateWeeksOnly() {
        assertTrue(everyOtherTuesdayAfter5.shouldNotify(null, tuesday.atTime(18, 0)))
        assertFalse(everyOtherTuesdayAfter5.shouldNotify(null, tuesday.plusWeeks(1).atTime(18, 0)))
        assertTrue(everyOtherTuesdayAfter5.shouldNotify(null, tuesday.plusWeeks(2).atTime(18, 0)))
    }

    @Test
    fun everyOtherTuesdayAfter5_quietBefore5AndOnOtherDays() {
        assertFalse(everyOtherTuesdayAfter5.shouldNotify(null, tuesday.atTime(16, 59)))
        assertFalse(everyOtherTuesdayAfter5.shouldNotify(null, tuesday.plusDays(1).atTime(18, 0)))
        assertTrue(everyOtherTuesdayAfter5.shouldNotify(null, tuesday.atTime(23, 59)))
    }

    @Test
    fun datesBeforeStartNeverMatch() {
        assertFalse(everyOtherTuesdayAfter5.shouldNotify(null, tuesday.minusWeeks(2).atTime(18, 0)))
    }

    @Test
    fun firesAtMostOncePerWindow() {
        val daily = Schedule(repeat = RepeatType.DAILY)
        val first = tuesday.atTime(9, 0)

        assertTrue(daily.shouldNotify(null, first))
        assertFalse(daily.shouldNotify(first, tuesday.atTime(20, 0)))
        assertTrue(daily.shouldNotify(first, tuesday.plusDays(1).atTime(8, 0)))
    }

    @Test
    fun overnightWindow_hoursAfterMidnightBelongToPreviousDay() {
        val fridayNights = Schedule(
            repeat = RepeatType.WEEKLY,
            daysOfWeek = setOf(DayOfWeek.FRIDAY),
            windowStartMinute = 22 * 60,
            windowEndMinute = 2 * 60
        )
        val friday = LocalDate.of(2026, 10, 30)

        assertEquals(friday.atTime(22, 0), fridayNights.windowStart(friday.atTime(23, 0)))
        assertEquals(friday.atTime(22, 0), fridayNights.windowStart(friday.plusDays(1).atTime(1, 30)))
        assertNull(fridayNights.windowStart(friday.plusDays(1).atTime(2, 0)))
        assertNull(fridayNights.windowStart(friday.atTime(1, 0)))
        // Already fired at 11pm, so 1:30am Saturday is the same window.
        assertFalse(fridayNights.shouldNotify(friday.atTime(23, 0), friday.plusDays(1).atTime(1, 30)))
    }

    @Test
    fun monthlyOn31st_usesLastDayOfShortMonths() {
        val monthly = Schedule(repeat = RepeatType.MONTHLY, dayOfMonth = 31)

        assertTrue(monthly.shouldNotify(null, LocalDateTime.of(2027, 2, 28, 12, 0)))
        assertFalse(monthly.shouldNotify(null, LocalDateTime.of(2027, 2, 27, 12, 0)))
        assertTrue(monthly.shouldNotify(null, LocalDateTime.of(2026, 10, 31, 12, 0)))
        assertFalse(monthly.shouldNotify(null, LocalDateTime.of(2026, 10, 30, 12, 0)))
    }

    @Test
    fun everyThreeDays() {
        val schedule = Schedule(repeat = RepeatType.CUSTOM, interval = 3, unit = RepeatUnit.DAYS, startDate = tuesday)

        assertTrue(schedule.shouldNotify(null, tuesday.atTime(12, 0)))
        assertFalse(schedule.shouldNotify(null, tuesday.plusDays(1).atTime(12, 0)))
        assertTrue(schedule.shouldNotify(null, tuesday.plusDays(3).atTime(12, 0)))
    }

    @Test
    fun onlyOnce_firesOnlyFirstTime() {
        val once = Schedule(repeat = RepeatType.ONCE)

        assertTrue(once.shouldNotify(null, tuesday.atTime(12, 0)))
        assertFalse(once.shouldNotify(tuesday.atTime(12, 0), tuesday.plusDays(5).atTime(12, 0)))
    }

    @Test
    fun always_firesEveryTimeButRespectsTimeWindow() {
        val always = Schedule()
        val evenings = Schedule(windowStartMinute = 17 * 60, windowEndMinute = 21 * 60)

        assertTrue(always.shouldNotify(tuesday.atTime(11, 59), tuesday.atTime(12, 0)))
        assertTrue(evenings.shouldNotify(tuesday.atTime(18, 0), tuesday.atTime(19, 0)))
        assertFalse(evenings.shouldNotify(null, tuesday.atTime(21, 0)))
    }
}
