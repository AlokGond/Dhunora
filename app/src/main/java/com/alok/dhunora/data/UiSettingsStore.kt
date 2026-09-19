package com.alok.dhunora.data

import android.content.Context

object UiSettingsStore {
    private const val PREFS = "dhunora_ui_settings"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun translucentNav(context: Context): Boolean =
        prefs(context).getBoolean("translucent_nav", true)

    fun setTranslucentNav(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean("translucent_nav", value).apply()
    }

    fun liquidGlass(context: Context): Boolean =
        prefs(context).getBoolean("liquid_glass", true)

    fun setLiquidGlass(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean("liquid_glass", value).apply()
    }

    fun romanizedLyrics(context: Context): Boolean =
        prefs(context).getBoolean("romanized_lyrics", false)

    fun setRomanizedLyrics(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean("romanized_lyrics", value).apply()
    }

    fun nowPlayingStyle(context: Context): String =
        prefs(context).getString("now_playing_style", "Classic") ?: "Classic"

    fun setNowPlayingStyle(context: Context, value: String) {
        prefs(context).edit().putString("now_playing_style", value).apply()
    }

    fun lyricsStyle(context: Context): String =
        prefs(context).getString("lyrics_style", "Classic") ?: "Classic"

    fun setLyricsStyle(context: Context, value: String) {
        prefs(context).edit().putString("lyrics_style", value).apply()
    }

    fun themeColor(context: Context): String =
        prefs(context).getString("theme_color", "Default") ?: "Default"

    fun setThemeColor(context: Context, value: String) {
        prefs(context).edit().putString("theme_color", value).apply()
    }
}
