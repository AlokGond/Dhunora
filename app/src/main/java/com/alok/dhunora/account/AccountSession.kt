package com.alok.dhunora.account

import android.content.Context

object AccountSession {
    private const val PREFS = "dhunora_account"
    private const val KEY_COOKIE = "youtube_music_cookie"

    fun saveCookie(context: Context, cookie: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_COOKIE, cookie)
            .apply()
    }

    fun cookie(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_COOKIE, "")
            .orEmpty()

    fun isLoggedIn(context: Context): Boolean {
        val value = cookie(context)
        return value.contains("SAPISID=") ||
            value.contains("__Secure-3PAPISID=") ||
            value.contains("SID=")
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
