package com.alok.dhunora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.alok.dhunora.data.MusicRepository
import com.alok.dhunora.model.Song
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = ExoPlayer.Builder(this).build()
        setContent { DhunoraApp() }
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }

    @Composable
    private fun DhunoraApp() {
        var query by remember { mutableStateOf("") }
        var results by remember { mutableStateOf<List<Song>>(emptyList()) }
        var loading by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        var current by remember { mutableStateOf<Song?>(null) }
        var playing by remember { mutableStateOf(false) }
        var favorites by remember { mutableStateOf(loadFavorites()) }

        val colors = darkColorScheme(
            primary = androidx.compose.ui.graphics.Color(0xFF9DFF4F),
            background = androidx.compose.ui.graphics.Color(0xFF0B0F14),
            surface = androidx.compose.ui.graphics.Color(0xFF131A22)
        )

        MaterialTheme(colorScheme = colors) {
            Scaffold(
                bottomBar = {
                    current?.let { song ->
                        Surface(tonalElevation = 6.dp) {
                            Row(
                                Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.MusicNote, null, modifier = Modifier.size(34.dp))
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(song.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                                    Text(song.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                                }
                                FilledIconButton(onClick = {
                                    if (player.isPlaying) player.pause() else player.play()
                                    playing = player.isPlaying
                                }) {
                                    Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null)
                                }
                            }
                        }
                    }
                }
            ) { pad ->
                Column(Modifier.fillMaxSize().padding(pad).padding(horizontal = 16.dp)) {
                    Spacer(Modifier.height(16.dp))
                    Text("Dhunora", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                    Text("Music without the clutter", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Search songs, artists...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, null) },
                        trailingIcon = {
                            Button(
                                enabled = query.isNotBlank() && !loading,
                                onClick = {
                                    loading = true
                                    error = null
                                    lifecycleScope.launch {
                                        runCatching { MusicRepository.searchSongs(query) }
                                            .onSuccess { results = it }
                                            .onFailure { error = it.message ?: "Search failed" }
                                        loading = false
                                    }
                                }
                            ) { Text("Go") }
                        }
                    )

                    if (loading) {
                        LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 12.dp))
                    }
                    error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 10.dp))
                    }

                    if (results.isEmpty() && !loading) {
                        Column(
                            Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Rounded.MusicNote, null, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("Search and play music", fontWeight = FontWeight.Bold)
                            Text("No login required", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            Modifier.fillMaxSize().padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(results, key = { it.sourceUrl }) { song ->
                                val liked = favorites.any { it.sourceUrl == song.sourceUrl }
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    tonalElevation = 2.dp
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FilledIconButton(onClick = {
                                            lifecycleScope.launch {
                                                error = null
                                                runCatching {
                                                    val url = MusicRepository.resolveAudioUrl(song)
                                                    player.setMediaItem(MediaItem.fromUri(url))
                                                    player.prepare()
                                                    player.play()
                                                    current = song
                                                    playing = true
                                                }.onFailure { error = it.message ?: "Playback failed" }
                                            }
                                        }) {
                                            Icon(Icons.Rounded.PlayArrow, null)
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(song.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(song.artist, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        IconButton(onClick = {
                                            favorites = toggleFavorite(favorites, song)
                                        }) {
                                            Icon(if (liked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, null)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadFavorites(): List<Song> {
        val raw = getSharedPreferences("dhunora", 0).getString("favorites", "[]") ?: "[]"
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(Song(o.getString("title"), o.getString("artist"), o.getString("url")))
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun toggleFavorite(list: List<Song>, song: Song): List<Song> {
        val next = if (list.any { it.sourceUrl == song.sourceUrl }) {
            list.filterNot { it.sourceUrl == song.sourceUrl }
        } else listOf(song) + list

        val arr = JSONArray()
        next.forEach {
            arr.put(JSONObject().apply {
                put("title", it.title)
                put("artist", it.artist)
                put("url", it.sourceUrl)
            })
        }
        getSharedPreferences("dhunora", 0).edit().putString("favorites", arr.toString()).apply()
        return next
    }
}
