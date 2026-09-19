package com.alok.dhunora.data

import com.alok.dhunora.model.MusicSearchItem
import com.alok.dhunora.model.SearchKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object YouTubeMusicApi {
    private const val ORIGIN = "https://music.youtube.com"
    private const val API_KEY = "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8"
    private const val CLIENT_VERSION = "1.20260915.01.00"
    private const val SONG_FILTER = "EgWKAQIIAWoKEAkQBRAKEAMQBA%3D%3D"

    private val client =
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .build()

    suspend fun searchSongs(query: String): List<MusicSearchItem> =
        withContext(Dispatchers.IO) {
            val payload =
                JSONObject()
                    .put(
                        "context",
                        JSONObject().put(
                            "client",
                            JSONObject()
                                .put("clientName", "WEB_REMIX")
                                .put("clientVersion", CLIENT_VERSION)
                                .put("hl", "en")
                                .put("gl", "IN")
                        )
                    )
                    .put("query", query)
                    .put("params", SONG_FILTER)
                    .toString()

            val request =
                Request.Builder()
                    .url("$ORIGIN/youtubei/v1/search?key=$API_KEY&prettyPrint=false")
                    .header("Origin", ORIGIN)
                    .header("Referer", "$ORIGIN/")
                    .header("X-Youtube-Client-Name", "67")
                    .header("X-Youtube-Client-Version", CLIENT_VERSION)
                    .header(
                        "User-Agent",
                        "Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 " +
                            "(KHTML, like Gecko) Chrome/140.0 Mobile Safari/537.36"
                    )
                    .post(payload.toRequestBody("application/json; charset=utf-8".toMediaType()))
                    .build()

            runCatching {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use emptyList()
                    val body = response.body?.string().orEmpty()
                    if (body.isBlank()) return@use emptyList()
                    parseSongs(JSONObject(body))
                }
            }.getOrDefault(emptyList())
        }

    private fun parseSongs(root: JSONObject): List<MusicSearchItem> {
        val renderers = mutableListOf<JSONObject>()
        collectObjects(root, "musicResponsiveListItemRenderer", renderers)

        return renderers
            .mapNotNull(::rendererToSong)
            .distinctBy { it.sourceUrl }
            .take(35)
    }

    private fun rendererToSong(renderer: JSONObject): MusicSearchItem? {
        val videoId = findString(renderer, "videoId") ?: return null

        val flexColumns = renderer.optJSONArray("flexColumns")
        val title =
            flexColumns
                ?.optJSONObject(0)
                ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                ?.optJSONObject("text")
                ?.let(::firstText)
                ?.takeIf { it.isNotBlank() }
                ?: return null

        val secondRuns =
            flexColumns
                ?.optJSONObject(1)
                ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                ?.optJSONObject("text")
                ?.optJSONArray("runs")

        val metadata = mutableListOf<String>()
        if (secondRuns != null) {
            for (i in 0 until secondRuns.length()) {
                val text = secondRuns.optJSONObject(i)?.optString("text").orEmpty().trim()
                if (
                    text.isNotBlank() &&
                    text != "•" &&
                    text != "Song" &&
                    text != "Video" &&
                    !DURATION.matches(text)
                ) {
                    metadata += text
                }
            }
        }

        val artist =
            metadata.firstOrNull()
                ?.takeIf { it.isNotBlank() }
                ?: "Unknown artist"

        val durationText =
            findAllStrings(renderer)
                .firstOrNull { DURATION.matches(it) }
        val duration = parseDuration(durationText)

        val thumb =
            findThumbnailUrl(renderer)
                ?.replace(Regex("w\\d+-h\\d+"), "w800-h800")
                ?.replace(Regex("=s\\d+(-c)?"), "=s800-c")

        return MusicSearchItem(
            kind = SearchKind.SONG,
            title = title,
            subtitle = artist,
            sourceUrl = "https://www.youtube.com/watch?v=$videoId",
            thumbnailUrl = thumb,
            durationSeconds = duration
        )
    }

    private fun firstText(obj: JSONObject): String? {
        obj.optString("simpleText").takeIf { it.isNotBlank() }?.let { return it }
        val runs = obj.optJSONArray("runs") ?: return null
        for (i in 0 until runs.length()) {
            val text = runs.optJSONObject(i)?.optString("text").orEmpty()
            if (text.isNotBlank()) return text
        }
        return null
    }

    private fun findThumbnailUrl(value: Any?): String? {
        when (value) {
            is JSONObject -> {
                val thumbnails = value.optJSONArray("thumbnails")
                if (thumbnails != null) {
                    for (i in thumbnails.length() - 1 downTo 0) {
                        val url = thumbnails.optJSONObject(i)?.optString("url").orEmpty()
                        if (url.isNotBlank()) return url
                    }
                }

                val keys = value.keys()
                while (keys.hasNext()) {
                    findThumbnailUrl(value.opt(keys.next()))?.let { return it }
                }
            }

            is JSONArray -> {
                for (i in 0 until value.length()) {
                    findThumbnailUrl(value.opt(i))?.let { return it }
                }
            }
        }
        return null
    }

    private fun collectObjects(
        value: Any?,
        key: String,
        output: MutableList<JSONObject>
    ) {
        when (value) {
            is JSONObject -> {
                value.optJSONObject(key)?.let(output::add)
                val keys = value.keys()
                while (keys.hasNext()) {
                    collectObjects(value.opt(keys.next()), key, output)
                }
            }

            is JSONArray -> {
                for (i in 0 until value.length()) {
                    collectObjects(value.opt(i), key, output)
                }
            }
        }
    }

    private fun findString(
        value: Any?,
        key: String
    ): String? {
        when (value) {
            is JSONObject -> {
                value.optString(key).takeIf { it.isNotBlank() }?.let { return it }
                val keys = value.keys()
                while (keys.hasNext()) {
                    findString(value.opt(keys.next()), key)?.let { return it }
                }
            }

            is JSONArray -> {
                for (i in 0 until value.length()) {
                    findString(value.opt(i), key)?.let { return it }
                }
            }
        }
        return null
    }

    private fun findAllStrings(value: Any?): List<String> {
        val result = mutableListOf<String>()
        fun walk(node: Any?) {
            when (node) {
                is JSONObject -> {
                    val keys = node.keys()
                    while (keys.hasNext()) walk(node.opt(keys.next()))
                }
                is JSONArray -> {
                    for (i in 0 until node.length()) walk(node.opt(i))
                }
                is String -> result += node.trim()
            }
        }
        walk(value)
        return result
    }

    private fun parseDuration(value: String?): Long {
        if (value.isNullOrBlank()) return 0L
        val parts = value.split(":").mapNotNull { it.toLongOrNull() }
        return when (parts.size) {
            2 -> parts[0] * 60 + parts[1]
            3 -> parts[0] * 3600 + parts[1] * 60 + parts[2]
            else -> 0L
        }
    }

    private val DURATION = Regex("""\d{1,2}:\d{2}(?::\d{2})?""")
}
