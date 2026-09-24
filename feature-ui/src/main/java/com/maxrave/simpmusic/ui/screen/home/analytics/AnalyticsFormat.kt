package com.alok.dhunora.ui.ui.screen.home.analytics

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.alok.dhunora.ui.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

/**
 * The full month name's string resource id, resolved by whoever needs it.
 */
fun monthFullNameRes(month: Month): Int =
    when (month) {
        Month.JANUARY -> R.string.month_full_jan
        Month.FEBRUARY -> R.string.month_full_feb
        Month.MARCH -> R.string.month_full_mar
        Month.APRIL -> R.string.month_full_apr
        Month.MAY -> R.string.month_full_may
        Month.JUNE -> R.string.month_full_jun
        Month.JULY -> R.string.month_full_jul
        Month.AUGUST -> R.string.month_full_aug
        Month.SEPTEMBER -> R.string.month_full_sep
        Month.OCTOBER -> R.string.month_full_oct
        Month.NOVEMBER -> R.string.month_full_nov
        Month.DECEMBER -> R.string.month_full_dec
        else -> R.string.month_full_jan
    }

/**
 * `January` — the register a playlist title needs.
 */
@Composable
fun monthFullName(month: Month): String = stringResource(monthFullNameRes(month))

/**
 * A span written in numbers, day first: `1/9-1/10/2026`.
 *
 * The year is written once, at the end, when both days share it. A span that crosses New Year
 * carries it on both sides — `15/12/2025-13/1/2026` — because `15/12-13/1/2026` reads as December
 * of the later year.
 */
fun formatNumericSpan(
    start: LocalDate,
    end: LocalDate,
): String {
    val from = if (start.year == end.year) "${start.day}/${start.month.ordinal + 1}" else "${start.day}/${start.month.ordinal + 1}/${start.year}"
    return "$from-${end.day}/${end.month.ordinal + 1}/${end.year}"
}
