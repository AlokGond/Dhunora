package com.alok.dhunora.data

import android.content.Context
import com.alok.dhunora.model.Song
import org.json.JSONArray
import org.json.JSONObject

object ListeningProfileStore {
    private const val PREFS = "dhunora_listening_profile"
    private const val KEY_RECENT = "recent"
    private const val KEY_SEARCHES = "searches"

    fun recordPlay(context: Context, song: Song) {
        val existing = recent(context).toMutableList()
        existing.removeAll { it.sourceUrl == song.sourceUrl }
        existing.add(0, song.copy(localUri = null))
        save(context, existing.take(50))
    }

    fun recent(context: Context): List<Song> {
        val raw =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_RECENT, "[]") ?: "[]"

        return runCatching {
            val arr = JSONArray(raw)
            buildList {
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
        }.getOrDefault(emptyList())
    }


    fun recordSearch(context: Context, query: String) {
        val clean =
            query.trim()
                .replace(Regex("\\s+"), " ")
                .take(100)
        if (clean.length < 2) return

        val existing = recentSearches(context).toMutableList()
        existing.removeAll { it.equals(clean, ignoreCase = true) }
        existing.add(0, clean)

        val arr = JSONArray()
        existing.take(20).forEach(arr::put)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SEARCHES, arr.toString())
            .apply()
    }

    fun recentSearches(context: Context): List<String> {
        val raw =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_SEARCHES, "[]") ?: "[]"

        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    arr.optString(i)
                        .trim()
                        .takeIf { it.isNotBlank() }
                        ?.let(::add)
                }
            }
        }.getOrDefault(emptyList())
    }

    fun favoriteArtists(context: Context): List<String> {
        val counts = linkedMapOf<String, Int>()
        recent(context).forEach { song ->
            val artist = song.artist.trim()
            if (artist.isNotBlank() && !artist.equals("Unknown artist", true)) {
                counts[artist] = (counts[artist] ?: 0) + 1
            }
        }
        return counts.entries
            .sortedByDescending { it.value }
            .map { it.key }
            .take(8)
    }

    private fun save(context: Context, songs: List<Song>) {
        val arr = JSONArray()
        songs.forEach { song ->
            arr.put(
                JSONObject().apply {
                    put("title", song.title)
                    put("artist", song.artist)
                    put("url", song.sourceUrl)
                    put("duration", song.durationSeconds)
                    put("thumb", song.thumbnailUrl ?: "")
                }
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_RECENT, arr.toString())
            .apply()
    }
}
