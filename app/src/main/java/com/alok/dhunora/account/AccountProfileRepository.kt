package com.alok.dhunora.account

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

data class AccountProfile(
    val name: String,
    val avatarUrl: String?
)

object AccountProfileRepository {
    private const val ORIGIN = "https://music.youtube.com"
    private const val API_KEY = "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8"
    private const val CLIENT_VERSION = "1.20260915.01.00"

    private val client =
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(18, TimeUnit.SECONDS)
            .build()

    suspend fun refresh(context: Context): AccountProfile? =
        withContext(Dispatchers.IO) {
            val cookie = AccountSession.cookie(context)
            if (cookie.isBlank()) return@withContext null

            val sapisid =
                cookieValue(cookie, "SAPISID")
                    ?: cookieValue(cookie, "__Secure-3PAPISID")
                    ?: cookieValue(cookie, "__Secure-1PAPISID")
                    ?: return@withContext null

            val timestamp = System.currentTimeMillis() / 1000L
            val hash = sha1("$timestamp $sapisid $ORIGIN")
            val auth = "SAPISIDHASH ${timestamp}_$hash"

            val payload =
                JSONObject()
                    .put(
                        "context",
                        JSONObject()
                            .put(
                                "client",
                                JSONObject()
                                    .put("clientName", "WEB_REMIX")
                                    .put("clientVersion", CLIENT_VERSION)
                                    .put("hl", "en")
                                    .put("gl", "IN")
                            )
                    )
                    .toString()

            val request =
                Request.Builder()
                    .url(
                        "$ORIGIN/youtubei/v1/account/account_menu" +
                            "?key=$API_KEY&prettyPrint=false"
                    )
                    .header("Cookie", cookie)
                    .header("Authorization", auth)
                    .header("Origin", ORIGIN)
                    .header("Referer", "$ORIGIN/")
                    .header("X-Origin", ORIGIN)
                    .header("X-Youtube-Client-Name", "67")
                    .header("X-Youtube-Client-Version", CLIENT_VERSION)
                    .header("X-Goog-AuthUser", "0")
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

            val profile =
                runCatching {
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) return@use null
                        val body = response.body?.string().orEmpty()
                        if (body.isBlank()) return@use null
                        parseProfile(JSONObject(body))
                    }
                }.getOrNull()

            profile?.let {
                AccountSession.saveProfile(
                    context,
                    name = it.name,
                    avatarUrl = it.avatarUrl
                )
            }

            profile
        }

    private fun parseProfile(root: JSONObject): AccountProfile? {
        val renderer = findObject(root, "activeAccountHeaderRenderer")
            ?: return null

        val name =
            firstText(renderer.optJSONObject("accountName"))
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: return null

        val avatar =
            renderer
                .optJSONObject("accountPhoto")
                ?.optJSONArray("thumbnails")
                ?.lastUrl()

        return AccountProfile(name = name, avatarUrl = avatar)
    }

    private fun firstText(obj: JSONObject?): String? {
        if (obj == null) return null
        obj.optString("simpleText")
            .takeIf { it.isNotBlank() }
            ?.let { return it }

        val runs = obj.optJSONArray("runs") ?: return null
        for (i in 0 until runs.length()) {
            val text = runs.optJSONObject(i)?.optString("text").orEmpty()
            if (text.isNotBlank()) return text
        }
        return null
    }

    private fun findObject(
        value: Any?,
        key: String
    ): JSONObject? {
        when (value) {
            is JSONObject -> {
                value.optJSONObject(key)?.let { return it }
                val keys = value.keys()
                while (keys.hasNext()) {
                    val child = value.opt(keys.next())
                    findObject(child, key)?.let { return it }
                }
            }

            is JSONArray -> {
                for (i in 0 until value.length()) {
                    findObject(value.opt(i), key)?.let { return it }
                }
            }
        }
        return null
    }

    private fun JSONArray.lastUrl(): String? {
        for (i in length() - 1 downTo 0) {
            val url = optJSONObject(i)?.optString("url").orEmpty()
            if (url.isNotBlank()) return url
        }
        return null
    }

    private fun cookieValue(
        cookie: String,
        key: String
    ): String? =
        cookie
            .split(';')
            .asSequence()
            .map { it.trim() }
            .firstOrNull { it.startsWith("$key=") }
            ?.substringAfter('=')
            ?.takeIf { it.isNotBlank() }

    private fun sha1(input: String): String =
        MessageDigest.getInstance("SHA-1")
            .digest(input.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
