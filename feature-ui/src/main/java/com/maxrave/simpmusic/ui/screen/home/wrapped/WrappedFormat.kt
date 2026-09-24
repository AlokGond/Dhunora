package com.alok.dhunora.ui.ui.screen.home.wrapped

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.intl.Locale
import kotlin.math.roundToInt

/**
 * Formatting for the Wrapped reel, kept out of the cards.
 *
 * Thousands-grouped, the way the design prints every large figure ("18,430").
 * Grouping is done by hand because there is no `NumberFormat` in common Kotlin. The separator
 * follows the locale for the handful of languages the app ships where a comma would be wrong;
 * everything else gets the comma the design draws.
 */
@Composable
fun formatCount(value: Long): String = groupDigits(value, groupingSeparatorFor(Locale.current.language))

@Composable
fun formatCount(value: Int): String = formatCount(value.toLong())

/** A fraction 0..1 as whole percent — "41%". Rounded, never truncated: 0.999 is 100%, not 99%. */
fun formatPercent(fraction: Float): String = "${(fraction.coerceIn(0f, 1f) * 100).roundToInt()}%"

/** Seconds to whole minutes — the figure card 02 is built around. */
fun wholeMinutes(seconds: Long): Long = seconds / 60

/** Seconds to whole days, for card 02's "12 whole days". Floored: claiming a day that did not finish would be a lie. */
fun wholeDays(seconds: Long): Long = seconds / 86_400

private fun groupDigits(
    value: Long,
    separator: Char,
): String {
    val digits = value.toString()
    val negative = digits.startsWith('-')
    val body = if (negative) digits.drop(1) else digits
    if (body.length <= 4) return digits
    val grouped =
        body
            .reversed()
            .chunked(3)
            .joinToString(separator.toString())
            .reversed()
    return if (negative) "-$grouped" else grouped
}

/**
 * A comma everywhere the design's comma is right, a full stop where it would misread.
 *
 * Deliberately a short list rather than a general rule: these are the languages the app ships where
 * a comma marks the DECIMAL point, so "18,430" would read as eighteen-point-four-three-zero.
 */
private fun groupingSeparatorFor(language: String): Char =
    when (language.lowercase()) {
        "de", "es", "it", "nl", "pt", "id", "tr", "vi", "da", "ca", "ro", "el", "sr", "hr", "sl", "az", "uk", "ru", "bg", "cs", "sk", "pl", "hu", "fi", "sv", "nb", "no", "lv", "lt", "et" -> '.'
        else -> ','
    }
