package com.alok.dhunora.network

import android.content.Context
import com.alok.dhunora.account.AccountSession
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import java.util.concurrent.TimeUnit

class OkHttpDownloader(
    private val context: Context
) : Downloader() {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    override fun execute(request: Request): Response {
        val b = okhttp3.Request.Builder().url(request.url())

        request.headers().forEach { (name, values) ->
            values.forEach { b.addHeader(name, it) }
        }

        if (request.headers().keys.none { it.equals("User-Agent", true) }) {
            b.header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 Chrome/140.0 Mobile Safari/537.36"
            )
        }

        val host = runCatching {
            okhttp3.HttpUrl.get(request.url()).host
        }.getOrNull().orEmpty()

        if (
            host.endsWith("youtube.com") ||
            host.endsWith("googlevideo.com") ||
            host.endsWith("youtu.be")
        ) {
            val cookie = AccountSession.cookie(context)
            if (cookie.isNotBlank()) {
                b.header("Cookie", cookie)
            }
        }

        when (request.httpMethod().uppercase()) {
            "GET" -> b.get()
            "HEAD" -> b.head()
            "POST" -> b.post((request.dataToSend() ?: ByteArray(0)).toRequestBody(null))
            else -> b.method(
                request.httpMethod(),
                request.dataToSend()?.toRequestBody(null)
            )
        }

        client.newCall(b.build()).execute().use { r ->
            return Response(
                r.code,
                r.message,
                r.headers.toMultimap(),
                r.body?.string().orEmpty(),
                r.request.url.toString()
            )
        }
    }
}
