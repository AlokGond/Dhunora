package com.alok.dhunora.data

import android.content.Context
import com.alok.dhunora.account.SpotifySession
import com.alok.dhunora.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

data class LyricsResult(
    val lines: List<String>,
    val source: String,
    val canvasUrl: String? = null,
    val canvasThumbUrl: String? = null
)

private data class SpotifyBundle(
    val accessToken: String,
    val clientToken: String,
    val trackId: String
)

object LyricsRepository {
    private val client =
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .build()

    suspend fun load(context: Context, song: Song): LyricsResult? =
        withContext(Dispatchers.IO) {
            val bundle =
                if (SpotifySession.isLoggedIn(context)) {
                    runCatching { spotifyBundle(context, song) }.getOrNull()
                } else {
                    null
                }

            val spotifyLines =
                bundle?.let { runCatching { spotifyLyricsWithBundle(it) }.getOrNull() }

            val canvas =
                bundle?.let { runCatching { spotifyCanvas(it) }.getOrNull() }

            if (!spotifyLines.isNullOrEmpty()) {
                return@withContext LyricsResult(
                    lines = spotifyLines,
                    source = "Spotify",
                    canvasUrl = canvas?.first,
                    canvasThumbUrl = canvas?.second
                )
            }

            val fallback = runCatching { lrclibLyrics(song) }.getOrNull()
            if (fallback != null) {
                return@withContext fallback.copy(
                    canvasUrl = canvas?.first,
                    canvasThumbUrl = canvas?.second
                )
            }

            canvas?.first?.let {
                return@withContext LyricsResult(
                    lines = emptyList(),
                    source = "Spotify",
                    canvasUrl = it,
                    canvasThumbUrl = canvas.second
                )
            }

            null
        }

    private fun spotifyBundle(
        context: Context,
        song: Song
    ): SpotifyBundle? {
        val spDc = SpotifySession.spDc(context)
        if (spDc.isBlank()) return null

        val accessToken =
            spotifyPersonalToken(spDc)
                ?: spotifyTotpToken(spDc)
                ?: return null

        val clientToken = spotifyClientToken() ?: return null
        val trackId =
            spotifyTrackId(song, accessToken, clientToken)
                ?: return null

        return SpotifyBundle(
            accessToken = accessToken,
            clientToken = clientToken,
            trackId = trackId
        )
    }

    private fun spotifyPersonalToken(spDc: String): String? {
        val request =
            Request.Builder()
                .url(
                    "https://open.spotify.com/get_access_token" +
                        "?reason=transport&productType=web_player"
                )
                .header("Cookie", "sp_dc=$spDc")
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "Chrome/135.0.0.0 Safari/537.36"
                )
                .build()

        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            JSONObject(response.body?.string().orEmpty())
                .optString("accessToken")
                .takeIf { it.isNotBlank() }
        }
    }

    private fun spotifyClientToken(): String? {
        val body =
            """
            {
              "client_data":{
                "client_version":"1.2.62.476.g2ad6e7f3",
                "client_id":"d8a5ed958d274c2e8ee717e6a4b0971d",
                "js_sdk_data":{
                  "device_brand":"Apple",
                  "device_model":"unknown",
                  "os":"macos",
                  "os_version":"10.15.7",
                  "device_id":"4fd0c748-b282-4927-9658-6d51a24e58b7",
                  "device_type":"computer"
                }
              }
            }
            """.trimIndent()

        val request =
            Request.Builder()
                .url("https://clienttoken.spotify.com/v1/clienttoken")
                .header("Accept", "application/json")
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "Chrome/135.0.0.0 Safari/537.36"
                )
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()

        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            JSONObject(response.body?.string().orEmpty())
                .optJSONObject("granted_token")
                ?.optString("token")
                ?.takeIf { it.isNotBlank() }
        }
    }

    private fun spotifyTrackId(
        song: Song,
        accessToken: String,
        clientToken: String
    ): String? {
        val variables =
            JSONObject()
                .put("searchTerm", (song.title + " " + song.artist).trim())
                .put("offset", 0)
                .put("limit", 3)
                .put("numberOfTopResults", 3)
                .put("includeAudiobooks", true)
                .put("includePreReleases", false)
                .toString()

        val extensions =
            """{"persistedQuery":{"version":1,"sha256Hash":"bc1ca2fcd0ba1013a0fc88e6cc4f190af501851e3dafd3e1ef85840297694428"}}"""

        val url =
            "https://api-partner.spotify.com/pathfinder/v1/query".toHttpUrl()
                .newBuilder()
                .addQueryParameter("operationName", "searchTracks")
                .addQueryParameter("variables", variables)
                .addQueryParameter("extensions", extensions)
                .build()

        val request =
            Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $accessToken")
                .header("Client-Token", clientToken)
                .build()

        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            JSONObject(response.body?.string().orEmpty())
                .optJSONObject("data")
                ?.optJSONObject("searchV2")
                ?.optJSONObject("tracksV2")
                ?.optJSONArray("items")
                ?.optJSONObject(0)
                ?.optJSONObject("item")
                ?.optJSONObject("data")
                ?.optString("id")
                ?.takeIf { it.isNotBlank() }
        }
    }

    private fun spotifyLyricsWithBundle(
        bundle: SpotifyBundle
    ): List<String>? {
        val request =
            Request.Builder()
                .url(
                    "https://spclient.wg.spotify.com/color-lyrics/v2/track/" +
                        bundle.trackId +
                        "?format=json&vocalRemoval=false&market=from_token"
                )
                .header("Authorization", "Bearer " + bundle.accessToken)
                .header("Client-Token", bundle.clientToken)
                .header("App-platform", "WebPlayer")
                .build()

        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val root = JSONObject(response.body?.string().orEmpty())
            val array =
                root.optJSONObject("lyrics")
                    ?.optJSONArray("lines")
                    ?: return null

            buildList {
                for (i in 0 until array.length()) {
                    array.optJSONObject(i)
                        ?.optString("words")
                        ?.trim()
                        ?.takeIf { it.isNotBlank() && it != "♪" }
                        ?.let(::add)
                }
            }.takeIf { it.isNotEmpty() }
        }
    }

    private fun spotifyCanvas(
        bundle: SpotifyBundle
    ): Pair<String, String?>? {
        val payload = encodeCanvasRequest(bundle.trackId)
        val request =
            Request.Builder()
                .url("https://spclient.wg.spotify.com/canvaz-cache/v0/canvases")
                .header("Accept", "application/protobuf")
                .header("Authorization", "Bearer " + bundle.accessToken)
                .header("Client-Token", bundle.clientToken)
                .header(
                    "User-Agent",
                    "Spotify/9.0.34.593 iOS/18.4 (iPhone15,3)"
                )
                .post(
                    payload.toRequestBody(
                        "application/protobuf".toMediaType()
                    )
                )
                .build()

        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            parseCanvasResponse(response.body?.bytes().orEmpty())
        }
    }

    private fun encodeCanvasRequest(trackId: String): ByteArray {
        val uri =
            ("spotify:track:" + trackId)
                .toByteArray(Charsets.UTF_8)

        val track =
            ByteArrayOutputStream().apply {
                writeVarint((1 shl 3) or 2)
                writeVarint(uri.size)
                write(uri)
            }.toByteArray()

        return ByteArrayOutputStream().apply {
            writeVarint((1 shl 3) or 2)
            writeVarint(track.size)
            write(track)
        }.toByteArray()
    }

    private fun ByteArrayOutputStream.writeVarint(value: Int) {
        var remaining = value
        while (true) {
            if ((remaining and 0x7F.inv()) == 0) {
                write(remaining)
                return
            }
            write((remaining and 0x7F) or 0x80)
            remaining = remaining ushr 7
        }
    }

    private fun parseCanvasResponse(
        bytes: ByteArray
    ): Pair<String, String?>? {
        var index = 0

        while (index < bytes.size) {
            val key = readVarint(bytes, index) ?: return null
            index = key.second

            val field = key.first ushr 3
            val wire = key.first and 7

            if (field == 1 && wire == 2) {
                val length = readVarint(bytes, index) ?: return null
                index = length.second
                val end = index + length.first
                if (end > bytes.size) return null

                parseCanvasMessage(
                    bytes.copyOfRange(index, end)
                )?.let { return it }

                index = end
            } else {
                index = skipProtoField(bytes, index, wire) ?: return null
            }
        }

        return null
    }

    private fun parseCanvasMessage(
        bytes: ByteArray
    ): Pair<String, String?>? {
        var index = 0
        var canvasUrl: String? = null
        var thumbUrl: String? = null

        while (index < bytes.size) {
            val key = readVarint(bytes, index) ?: break
            index = key.second

            val field = key.first ushr 3
            val wire = key.first and 7

            if (wire == 2) {
                val length = readVarint(bytes, index) ?: break
                index = length.second
                val end = index + length.first
                if (end > bytes.size) break

                val data = bytes.copyOfRange(index, end)

                when (field) {
                    2 -> canvasUrl = data.toString(Charsets.UTF_8)
                    13 -> parseCanvasThumb(data)?.let { thumbUrl = it }
                }

                index = end
            } else {
                index = skipProtoField(bytes, index, wire) ?: break
            }
        }

        return canvasUrl
            ?.takeIf { it.isNotBlank() }
            ?.let { it to thumbUrl }
    }

    private fun parseCanvasThumb(bytes: ByteArray): String? {
        var index = 0

        while (index < bytes.size) {
            val key = readVarint(bytes, index) ?: return null
            index = key.second

            val field = key.first ushr 3
            val wire = key.first and 7

            if (field == 3 && wire == 2) {
                val length = readVarint(bytes, index) ?: return null
                index = length.second
                val end = index + length.first
                if (end > bytes.size) return null

                return bytes.copyOfRange(index, end)
                    .toString(Charsets.UTF_8)
                    .takeIf { it.isNotBlank() }
            }

            index = skipProtoField(bytes, index, wire) ?: return null
        }

        return null
    }

    private fun readVarint(
        bytes: ByteArray,
        start: Int
    ): Pair<Int, Int>? {
        var index = start
        var shift = 0
        var result = 0

        while (index < bytes.size && shift < 32) {
            val value = bytes[index].toInt() and 0xFF
            index++
            result =
                result or
                    ((value and 0x7F) shl shift)

            if ((value and 0x80) == 0) {
                return result to index
            }

            shift += 7
        }

        return null
    }

    private fun skipProtoField(
        bytes: ByteArray,
        start: Int,
        wire: Int
    ): Int? =
        when (wire) {
            0 -> readVarint(bytes, start)?.second
            1 -> (start + 8).takeIf { it <= bytes.size }
            2 -> {
                val length = readVarint(bytes, start) ?: return null
                (length.second + length.first)
                    .takeIf { it <= bytes.size }
            }
            5 -> (start + 4).takeIf { it <= bytes.size }
            else -> null
        }

    private fun legacySpotifyLyrics(
        context: Context,
        song: Song
    ): LyricsResult? {
        val spDc = SpotifySession.spDc(context)
        if (spDc.isBlank()) return null

        val tokenRequest =
            Request.Builder()
                .url(
                    "https://open.spotify.com/get_access_token" +
                        "?reason=transport&productType=web_player"
                )
                .header("Cookie", "sp_dc=$spDc")
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/140.0 Mobile Safari/537.36"
                )
                .build()

        val token =
            client.newCall(tokenRequest).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) return null
                JSONObject(body).optString("accessToken")
            }.takeIf { it.isNotBlank() } ?: return null

        val q =
            URLEncoder.encode(
                "track:${song.title} artist:${song.artist}",
                StandardCharsets.UTF_8.toString()
            )

        val searchRequest =
            Request.Builder()
                .url("https://api.spotify.com/v1/search?q=$q&type=track&limit=1")
                .header("Authorization", "Bearer $token")
                .build()

        val trackId =
            client.newCall(searchRequest).execute().use { response ->
                if (!response.isSuccessful) return null
                val root = JSONObject(response.body?.string().orEmpty())
                root.optJSONObject("tracks")
                    ?.optJSONArray("items")
                    ?.optJSONObject(0)
                    ?.optString("id")
            }.orEmpty()

        if (trackId.isBlank()) return null

        val lyricsRequest =
            Request.Builder()
                .url(
                    "https://spclient.wg.spotify.com/color-lyrics/v2/track/$trackId" +
                        "?format=json&market=from_token"
                )
                .header("Authorization", "Bearer $token")
                .header("app-platform", "WebPlayer")
                .build()

        return client.newCall(lyricsRequest).execute().use { response ->
            if (!response.isSuccessful) return null
            val root = JSONObject(response.body?.string().orEmpty())
            val linesArray =
                root.optJSONObject("lyrics")
                    ?.optJSONArray("lines")
                    ?: return null

            val lines = mutableListOf<String>()
            for (i in 0 until linesArray.length()) {
                val words =
                    linesArray.optJSONObject(i)
                        ?.optString("words")
                        .orEmpty()
                        .trim()
                if (words.isNotBlank() && words != "♪") lines += words
            }

            lines.takeIf { it.isNotEmpty() }
                ?.let { LyricsResult(it, "Spotify") }
        }
    }

    private fun lrclibLyrics(song: Song): LyricsResult? {
        val track =
            URLEncoder.encode(
                song.title,
                StandardCharsets.UTF_8.toString()
            )
        val artist =
            URLEncoder.encode(
                song.artist,
                StandardCharsets.UTF_8.toString()
            )

        val request =
            Request.Builder()
                .url(
                    "https://lrclib.net/api/search" +
                        "?track_name=$track&artist_name=$artist"
                )
                .header("User-Agent", "Dhunora/1.0")
                .build()

        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val body = response.body?.string().orEmpty()
            if (body.isBlank()) return null

            val array = JSONArray(body)
            if (array.length() == 0) return null

            var selected: JSONObject? = null
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                if (
                    item.optString("syncedLyrics").isNotBlank() ||
                    item.optString("plainLyrics").isNotBlank()
                ) {
                    selected = item
                    break
                }
            }

            val item = selected ?: return null
            val synced = item.optString("syncedLyrics")
            val plain = item.optString("plainLyrics")
            val raw = synced.ifBlank { plain }
            if (raw.isBlank()) return null

            val lines =
                raw.lineSequence()
                    .map {
                        it.replace(Regex("""^\[[^]]+\]\s*"""), "")
                            .trim()
                    }
                    .filter { it.isNotBlank() }
                    .toList()

            lines.takeIf { it.isNotEmpty() }
                ?.let { LyricsResult(it, "LRCLIB") }
        }
    }
}
