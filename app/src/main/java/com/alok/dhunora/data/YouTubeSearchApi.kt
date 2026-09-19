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

object YouTubeSearchApi {
    private const val ORIGIN = "https://www.youtube.com"
    private const val API_KEY = "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8"
    private const val CLIENT_VERSION = "2.20260915.01.00"

    private val client =
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .build()

    suspend fun searchVideos(query: String): List<MusicSearchItem> =
        search(query, SearchKind.SONG)

    suspend fun searchArtists(query: String): List<MusicSearchItem> =
        search(query, SearchKind.ARTIST)

    private suspend fun search(
        query: String,
        kind: SearchKind
    ): List<MusicSearchItem> =
        withContext(Dispatchers.IO) {
            val payload =
                JSONObject()
                    .put(
                        "context",
                        JSONObject().put(
                            "client",
                            JSONObject()
                                .put("clientName", "WEB")
                                .put("clientVersion", CLIENT_VERSION)
                                .put("hl", "en")
                                .put("gl", "IN")
                        )
                    )
                    .put("query", query)
                    .toString()

            val request =
                Request.Builder()
                    .url("$ORIGIN/youtubei/v1/search?key=$API_KEY&prettyPrint=false")
                    .header("Origin", ORIGIN)
                    .header("Referer", "$ORIGIN/")
                    .header("X-Youtube-Client-Name", "1")
                    .header("X-Youtube-Client-Version", CLIENT_VERSION)
                    .header(
                        "User-Agent",
                        "Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 " +
                            "(KHTML, like Gecko) Chrome/140.0 Mobile Safari/537.36"
                    )
                    .post(
                        payload.toRequestBody(
                            "application/json; charset=utf-8".toMediaType()
                        )
                    )
                    .build()

            runCatching {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use emptyList()
                    val body = response.body?.string().orEmpty()
                    if (body.isBlank()) return@use emptyList()

                    val root = JSONObject(body)
                    when (kind) {
                        SearchKind.SONG -> parseVideos(root)
                        SearchKind.ARTIST -> parseChannels(root)
                        else -> emptyList()
                    }
                }
            }.getOrDefault(emptyList())
        }

    private fun parseVideos(root: JSONObject): List<MusicSearchItem> {
        val renderers = mutableListOf<JSONObject>()
        collectObjects(root, "videoRenderer", renderers)

        return renderers
            .mapNotNull { renderer ->
                val videoId = renderer.optString("videoId").takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null
                val title =
                    text(renderer.optJSONObject("title"))
                        ?.takeIf { it.isNotBlank() }
                        ?: return@mapNotNull null
                val artist =
                    text(renderer.optJSONObject("ownerText"))
                        ?.takeIf { it.isNotBlank() }
                        ?: text(renderer.optJSONObject("longBylineText"))
                        ?: "YouTube"
                val duration =
                    parseDuration(
                        text(renderer.optJSONObject("lengthText"))
                    )
                val thumb =
                    bestThumbnail(renderer.optJSONObject("thumbnail"))
                        ?.let(::upgradeThumbnail)

                MusicSearchItem(
                    kind = SearchKind.SONG,
                    title = title,
                    subtitle = artist,
                    sourceUrl = "https://www.youtube.com/watch?v=$videoId",
                    thumbnailUrl = thumb,
                    durationSeconds = duration
                )
            }
            .distinctBy { it.sourceUrl }
            .take(40)
    }

    private fun parseChannels(root: JSONObject): List<MusicSearchItem> {
        val renderers = mutableListOf<JSONObject>()
        collectObjects(root, "channelRenderer", renderers)

        return renderers
            .mapNotNull { renderer ->
                val channelId =
                    renderer.optString("channelId")
                        .takeIf { it.isNotBlank() }
                        ?: return@mapNotNull null
                val title =
                    text(renderer.optJSONObject("title"))
                        ?.takeIf { it.isNotBlank() }
                        ?: return@mapNotNull null
                val subtitle =
                    text(renderer.optJSONObject("subscriberCountText"))
                        ?.takeIf { it.isNotBlank() }
                        ?: "Artist"
                val thumb =
                    bestThumbnail(renderer.optJSONObject("thumbnail"))
                        ?.let(::upgradeThumbnail)

                MusicSearchItem(
                    kind = SearchKind.ARTIST,
                    title = title,
                    subtitle = subtitle,
                    sourceUrl = "https://www.youtube.com/channel/$channelId",
                    thumbnailUrl = thumb
                )
            }
            .distinctBy { it.sourceUrl }
            .take(25)
    }

    private fun text(obj: JSONObject?): String? {
        if (obj == null) return null

        obj.optString("simpleText")
            .takeIf { it.isNotBlank() }
            ?.let { return it }

        val runs = obj.optJSONArray("runs") ?: return null
        val builder = StringBuilder()
        for (i in 0 until runs.length()) {
            val value = runs.optJSONObject(i)?.optString("text").orEmpty()
            if (value.isNotBlank()) builder.append(value)
        }
        return builder.toString().takeIf { it.isNotBlank() }
    }

    private fun bestThumbnail(obj: JSONObject?): String? {
        val thumbnails = obj?.optJSONArray("thumbnails") ?: return null
        for (i in thumbnails.length() - 1 downTo 0) {
            val url = thumbnails.optJSONObject(i)?.optString("url").orEmpty()
            if (url.isNotBlank()) return url
        }
        return null
    }

    private fun upgradeThumbnail(url: String): String {
        return when {
            url.contains("i.ytimg.com/vi/") ->
                url.replace(
                    Regex("/(default|mqdefault|hqdefault|sddefault|maxresdefault)\\.jpg.*$"),
                    "/sddefault.jpg"
                )

            Regex("=s\\d+(-c)?").containsMatchIn(url) ->
                url.replace(Regex("=s\\d+(-c)?"), "=s1200-c")

            Regex("=w\\d+-h\\d+").containsMatchIn(url) ->
                url.replace(
                    Regex("=w\\d+-h\\d+[^?]*$"),
                    "=w1200-h1200-l90-rj"
                )

            else -> url
        }
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
}
