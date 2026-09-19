package com.alok.dhunora.data

import android.app.DownloadManager
import android.content.Context
import com.alok.dhunora.model.Song
import org.json.JSONArray
import org.json.JSONObject

data class DownloadRecord(
    val id: Long,
    val song: Song,
    val status: Int,
    val progress: Int,
    val localUri: String?
) {
    val isComplete: Boolean
        get() = status == DownloadManager.STATUS_SUCCESSFUL && !localUri.isNullOrBlank()
}

object DownloadedSongStore {
    private const val PREFS = "dhunora_downloads"
    private const val KEY_ITEMS = "items"

    fun add(context: Context, id: Long, song: Song) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = runCatching {
            JSONArray(prefs.getString(KEY_ITEMS, "[]") ?: "[]")
        }.getOrElse { JSONArray() }

        val next = JSONArray()
        next.put(toJson(id, song))
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            if (obj.optString("url") != song.sourceUrl) {
                next.put(obj)
            }
        }
        prefs.edit().putString(KEY_ITEMS, next.toString()).apply()
    }

    fun records(context: Context): List<DownloadRecord> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = runCatching {
            JSONArray(prefs.getString(KEY_ITEMS, "[]") ?: "[]")
        }.getOrElse { JSONArray() }

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val output = mutableListOf<DownloadRecord>()

        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val id = obj.optLong("id", -1L)
            if (id <= 0L) continue

            val song = Song(
                title = obj.optString("title"),
                artist = obj.optString("artist"),
                sourceUrl = obj.optString("url"),
                durationSeconds = obj.optLong("duration"),
                thumbnailUrl = obj.optString("thumb").takeIf { it.isNotBlank() }
            )

            var status = DownloadManager.STATUS_PENDING
            var progress = 0
            var localUri: String? = null

            runCatching {
                manager.query(DownloadManager.Query().setFilterById(id))?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        val downloadedIndex =
                            cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        val totalIndex =
                            cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                        val uriIndex =
                            cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)

                        if (statusIndex >= 0) status = cursor.getInt(statusIndex)
                        val downloaded =
                            if (downloadedIndex >= 0) cursor.getLong(downloadedIndex) else 0L
                        val total =
                            if (totalIndex >= 0) cursor.getLong(totalIndex) else 0L
                        progress =
                            if (total > 0) ((downloaded * 100L) / total).toInt().coerceIn(0, 100)
                            else if (status == DownloadManager.STATUS_SUCCESSFUL) 100 else 0
                        if (uriIndex >= 0) {
                            localUri = cursor.getString(uriIndex)
                        }
                    }
                }
            }

            output +=
                DownloadRecord(
                    id = id,
                    song = song.copy(localUri = localUri),
                    status = status,
                    progress = progress,
                    localUri = localUri
                )
        }

        return output
    }

    private fun toJson(id: Long, song: Song): JSONObject =
        JSONObject().apply {
            put("id", id)
            put("title", song.title)
            put("artist", song.artist)
            put("url", song.sourceUrl)
            put("duration", song.durationSeconds)
            put("thumb", song.thumbnailUrl ?: "")
        }
}
