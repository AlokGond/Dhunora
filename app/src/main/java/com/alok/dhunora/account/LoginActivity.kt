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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)

        setContent {
            var progress by remember { mutableFloatStateOf(0f) }
            var title by remember { mutableStateOf("Sign in to YouTube Music") }

            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFFB69CFF),
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
                                title,
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
                                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.databaseEnabled = true
                                settings.userAgentString =
                                    "Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 " +
                                        "(KHTML, like Gecko) Chrome/140.0 Mobile Safari/537.36"
                                webChromeClient =
                                    object : WebChromeClient() {
                                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                            progress = newProgress / 100f
                                        }
                                    }
                                webViewClient =
                                    object : WebViewClient() {
                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                            if (url.isNullOrBlank()) return
                                            title =
                                                if (url.contains("accounts.google.com")) {
                                                    "Sign in with Google"
                                                } else {
                                                    "YouTube Music"
                                                }

                                            if (url.startsWith("https://music.youtube.com")) {
                                                CookieManager.getInstance().flush()
                                                val cookie =
                                                    CookieManager.getInstance()
                                                        .getCookie("https://music.youtube.com")
                                                        .orEmpty()

                                                if (
                                                    cookie.contains("SAPISID=") ||
                                                    cookie.contains("__Secure-3PAPISID=") ||
                                                    cookie.contains("SID=")
                                                ) {
                                                    AccountSession.saveCookie(this@LoginActivity, cookie)
                                                    lifecycleScope.launch {
                                                        AccountProfileRepository.refresh(this@LoginActivity)
                                                        Toast.makeText(
                                                            this@LoginActivity,
                                                            "YouTube Music account connected",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        finish()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                loadUrl(
                                    "https://accounts.google.com/ServiceLogin" +
                                        "?service=youtube" +
                                        "&continue=https%3A%2F%2Fmusic.youtube.com%2F"
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
