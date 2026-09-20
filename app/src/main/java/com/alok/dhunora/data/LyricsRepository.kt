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
