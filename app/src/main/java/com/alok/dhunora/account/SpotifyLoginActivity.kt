package com.alok.dhunora.account

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
            var showManual by remember { mutableStateOf(false) }
            var manualCookie by remember { mutableStateOf("") }

            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF1ED760),
                    background = Color(0xFF09090B),
                    surface = Color(0xFF111116),
                    onBackground = Color.White,
                    onSurface = Color.White
                )
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            WebView(context).apply {
                                setBackgroundColor(android.graphics.Color.WHITE)
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                cookieManager.setAcceptThirdPartyCookies(this, true)

                                // SimpMusic uses a plain WebView. Clearing stale WebView state fixes
                                // the blank/loop case that can persist across failed Spotify logins.
                                clearCache(true)
                                clearHistory()
                                clearFormData()
                                WebStorage.getInstance().deleteAllData()
                                cookieManager.removeAllCookies {
                                    cookieManager.flush()
                                    loadUrl(SPOTIFY_LOGIN_URL)
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

                                            val statusUrl =
                                                Regex(
                                                    "^https://accounts\\.spotify\\.com/" +
                                                        "(?:[^/]+/)?status(?:\\?.*)?$"
                                                )

                                            if (statusUrl.matches(currentUrl)) {
                                                saveSpotifyCookie(cookie)
                                            }
                                        }
                                    }
                            }
                        }
                    )

                    Surface(
                        color = Color(0xE609090B),
                        modifier =
                            Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .statusBarsPadding()
                    ) {
                        Row(
                            Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { finish() }) {
                                Icon(Icons.Rounded.ArrowBack, "Back")
                            }
                            Text(
                                "Log in to Spotify",
                                modifier = Modifier.weight(1f),
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                            IconButton(onClick = { showManual = true }) {
                                Icon(
                                    Icons.Rounded.Key,
                                    "Use sp_dc cookie",
                                    tint = Color(0xFF1ED760)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { showManual = true },
                        modifier =
                            Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(20.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1ED760),
                                contentColor = Color.Black
                            )
                    ) {
                        Icon(Icons.Rounded.Key, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Use sp_dc cookie")
                    }

                    if (showManual) {
                        AlertDialog(
                            onDismissRequest = { showManual = false },
                            title = { Text("Spotify cookie login") },
                            text = {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        "Paste only the sp_dc value, or paste the full cookie string containing sp_dc. This is the same fallback SimpMusic exposes when Spotify WebView login fails."
                                    )
                                    TextField(
                                        value = manualCookie,
                                        onValueChange = { manualCookie = it },
                                        singleLine = false,
                                        placeholder = { Text("sp_dc=...") }
                                    )
                                }
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        if (saveSpotifyCookie(manualCookie)) {
                                            showManual = false
                                        } else {
                                            Toast.makeText(
                                                this@SpotifyLoginActivity,
                                                "sp_dc not found",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                ) {
                                    Text("Connect")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showManual = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun saveSpotifyCookie(input: String): Boolean {
        val value =
            input
                .split(';')
                .asSequence()
                .map { it.trim() }
                .firstOrNull { it.startsWith("sp_dc=", ignoreCase = true) }
                ?.substringAfter('=')
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: input.trim()
                    .takeIf {
                        it.isNotBlank() &&
                            !it.contains(';') &&
                            !it.contains('=')
                    }

        if (value.isNullOrBlank()) return false

        SpotifySession.saveSpDc(this, value)
        CookieManager.getInstance().flush()
        Toast.makeText(this, "Spotify connected", Toast.LENGTH_SHORT).show()
        finish()
        return true
    }
}
