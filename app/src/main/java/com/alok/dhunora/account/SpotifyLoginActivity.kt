package com.alok.dhunora.account

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

class SpotifyLoginActivity : ComponentActivity() {
    companion object {
        private const val SPOTIFY_LOGIN_URL =
            "https://accounts.spotify.com/login" +
                "?continue=https%3A%2F%2Faccounts.spotify.com%2Fen%2Fstatus"
    }


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)

        setContent {
            var progress by remember { mutableFloatStateOf(0f) }
            var loadError by remember { mutableStateOf<String?>(null) }
            var webViewRef by remember { mutableStateOf<WebView?>(null) }

            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF1ED760),
                    background = Color(0xFF09090B),
                    surface = Color(0xFF111116)
                )
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .statusBarsPadding()
                ) {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        Box(
                            Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            IconButton(onClick = { finish() }) {
                                Icon(Icons.Rounded.ArrowBack, "Back")
                            }
                            Text(
                                "Connect Spotify",
                                modifier = Modifier.align(Alignment.Center),
                                color = Color.White
                            )
                        }
                    }

                    if (progress < 1f) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    Box(Modifier.fillMaxSize()) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { context ->
                                WebView(context).apply {
                                    webViewRef = this
                                    setBackgroundColor(android.graphics.Color.BLACK)
                                    cookieManager.setAcceptThirdPartyCookies(this, true)

                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.databaseEnabled = true
                                    settings.loadsImagesAutomatically = true
                                    settings.javaScriptCanOpenWindowsAutomatically = true
                                    settings.setSupportMultipleWindows(false)
                                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                                    settings.mixedContentMode =
                                        WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

                                    webChromeClient =
                                        object : WebChromeClient() {
                                            override fun onProgressChanged(
                                                view: WebView?,
                                                newProgress: Int
                                            ) {
                                                progress = newProgress / 100f
                                                if (newProgress > 5) loadError = null
                                            }
                                        }

                                    webViewClient =
                                        object : WebViewClient() {
                                            override fun shouldOverrideUrlLoading(
                                                view: WebView?,
                                                request: WebResourceRequest?
                                            ): Boolean {
                                                val target = request?.url?.toString().orEmpty()
                                                if (target.startsWith("https://")) {
                                                    view?.loadUrl(target)
                                                    return true
                                                }
                                                return false
                                            }

                                            override fun onReceivedError(
                                                view: WebView?,
                                                request: WebResourceRequest?,
                                                error: WebResourceError?
                                            ) {
                                                super.onReceivedError(view, request, error)
                                                if (request?.isForMainFrame == true) {
                                                    loadError =
                                                        error?.description?.toString()
                                                            ?: "Spotify login page could not load"
                                                }
                                            }

                                            override fun onPageFinished(
                                                view: WebView?,
                                                url: String?
                                            ) {
                                                super.onPageFinished(view, url)
                                                progress = 1f
                                                CookieManager.getInstance().flush()

                                                val cookie =
                                                    CookieManager.getInstance()
                                                        .getCookie(
                                                            url
                                                                ?: "https://accounts.spotify.com"
                                                        )
                                                        .orEmpty() +
                                                        "; " +
                                                        CookieManager.getInstance()
                                                            .getCookie(
                                                                "https://accounts.spotify.com"
                                                            )
                                                            .orEmpty()

                                                val spDc =
                                                    cookie
                                                        .split(';')
                                                        .asSequence()
                                                        .map { it.trim() }
                                                        .firstOrNull {
                                                            it.startsWith("sp_dc=")
                                                        }
                                                        ?.substringAfter('=')
                                                        .orEmpty()

                                                val isStatusPage =
                                                    url?.matches(
                                                        Regex(
                                                            "^https://accounts\\.spotify\\.com/" +
                                                                "(?:[^/]+/)?status(?:\\?.*)?$"
                                                        )
                                                    ) == true

                                                if (spDc.isNotBlank() && isStatusPage) {
                                                    SpotifySession.saveSpDc(
                                                        this@SpotifyLoginActivity,
                                                        spDc
                                                    )
                                                    Toast.makeText(
                                                        this@SpotifyLoginActivity,
                                                        "Spotify connected",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    finish()
                                                }
                                            }
                                        }

                                    loadUrl(SPOTIFY_LOGIN_URL)
                                }
                            }
                        )

                        if (loadError != null) {
                            Column(
                                Modifier
                                    .align(Alignment.Center)
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Spotify login could not load",
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    loadError.orEmpty(),
                                    color = Color.White.copy(alpha = 0.65f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        loadError = null
                                        progress = 0f
                                        webViewRef?.loadUrl(SPOTIFY_LOGIN_URL)
                                    }
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
