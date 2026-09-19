package com.alok.dhunora.data

import android.content.Context
import com.alok.dhunora.model.Song
import org.json.JSONArray
import org.json.JSONObject

object LocalPlaylistStore {
    private const val PREFS = "dhunora_playlists"
    private const val DEFAULT_PLAYLIST = "My Playlist"

    fun addToMyPlaylist(context: Context, song: Song) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(DEFAULT_PLAYLIST, "[]") ?: "[]"
        val arr = runCatching { JSONArray(raw) }.getOrElse { JSONArray() }

        val exists = (0 until arr.length()).any { index ->
            arr.optJSONObject(index)?.optString("url") == song.sourceUrl
        }
        if (exists) return

        arr.put(
            JSONObject().apply {
                put("title", song.title)
                put("artist", song.artist)
                put("url", song.sourceUrl)
                put("duration", song.durationSeconds)
                put("thumb", song.thumbnailUrl ?: "")
            }
        )
        prefs.edit().putString(DEFAULT_PLAYLIST, arr.toString()).apply()
    }

    fun myPlaylist(context: Context): List<Song> {
        val raw =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(DEFAULT_PLAYLIST, "[]") ?: "[]"
        val arr = runCatching { JSONArray(raw) }.getOrElse { JSONArray() }

        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                add(
                    Song(
                        title = o.optString("title"),
                        artist = o.optString("artist"),
                        sourceUrl = o.optString("url"),
                        durationSeconds = o.optLong("duration"),
                        thumbnailUrl = o.optString("thumb").takeIf { it.isNotBlank() }
                    )
                )
            }
        }
    }
}
