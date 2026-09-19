package com.alok.dhunora.account

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebChromeClient
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

class SpotifyLoginActivity : ComponentActivity() {
    companion object {
        private const val SPOTIFY_LOGIN_URL =
            "https://accounts.spotify.com/en/login"
    }


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)

        setContent {
            var progress by remember { mutableFloatStateOf(0f) }

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

                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            WebView(context).apply {
                                cookieManager.setAcceptThirdPartyCookies(this, true)
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true

                                webChromeClient =
                                    object : WebChromeClient() {
                                        override fun onProgressChanged(
                                            view: WebView?,
                                            newProgress: Int
                                        ) {
                                            progress = newProgress / 100f
                                        }
                                    }

                                webViewClient =
                                    object : WebViewClient() {
                                        override fun onPageFinished(
                                            view: WebView?,
                                            url: String?
                                        ) {
                                            super.onPageFinished(view, url)

                                            val currentUrl = url.orEmpty()
                                            val cookie =
                                                CookieManager.getInstance()
                                                    .getCookie(currentUrl)
                                                    .orEmpty()

                                            val isStatusPage =
                                                Regex(
                                                    "^https://accounts\\.spotify\\.com/" +
                                                        "(?:[^/]+/)?status(?:\\?.*)?$"
                                                ).matches(currentUrl)

                                            if (isStatusPage && cookie.isNotBlank()) {
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

                                                if (spDc.isNotBlank()) {
                                                    SpotifySession.saveSpDc(
                                                        this@SpotifyLoginActivity,
                                                        spDc
                                                    )
                                                    CookieManager.getInstance().flush()
                                                    Toast.makeText(
                                                        this@SpotifyLoginActivity,
                                                        "Spotify connected",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    finish()
                                                }
                                            }
                                        }
                                    }

                                loadUrl(SPOTIFY_LOGIN_URL)
                            }
                        }
                    )
                }
            }
        }
    }
}
