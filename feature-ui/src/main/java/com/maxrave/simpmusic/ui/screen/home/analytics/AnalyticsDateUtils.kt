package com.alok.dhunora.ui.ui.screen.home.analytics

import com.alok.dhunora.ui.R
import com.alok.dhunora.ui.viewModel.AnalyticsUiState
import kotlinx.datetime.Month

/**
 * Formatting helpers surviving from the pruned analytics screen.
 * Kept because the library's dynamic playlists need day-range labels and month names.
 */
fun AnalyticsUiState.DayRange.labelRes(): Int =
    when (this) {
        AnalyticsUiState.DayRange.LAST_7_DAYS -> R.string.last_7_days
        AnalyticsUiState.DayRange.LAST_30_DAYS -> R.string.last_30_days
        AnalyticsUiState.DayRange.LAST_90_DAYS -> R.string.last_90_days
        AnalyticsUiState.DayRange.THIS_YEAR -> R.string.this_year
    }

fun monthFullNameResource(month: Month): Int =
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
