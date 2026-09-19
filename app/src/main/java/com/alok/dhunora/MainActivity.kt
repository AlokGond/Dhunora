package com.alok.dhunora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import coil3.compose.AsyncImage
import com.alok.dhunora.data.MusicRepository
import com.alok.dhunora.model.Song
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

private enum class MainTab { HOME, LIBRARY, SEARCH }
private enum class PlayerPane { UP_NEXT, LYRICS }

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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun DhunoraApp() {
        var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }
        var searchText by rememberSaveable { mutableStateOf("") }
        var searchResults by remember { mutableStateOf<List<Song>>(emptyList()) }
        var quickPicks by remember { mutableStateOf<List<Song>>(emptyList()) }
        var madeForYou by remember { mutableStateOf<List<Song>>(emptyList()) }
        var trending by remember { mutableStateOf<List<Song>>(emptyList()) }
        var loadingSearch by remember { mutableStateOf(false) }
        var loadingHome by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }
        var currentSong by remember { mutableStateOf<Song?>(null) }
        var isPlaying by remember { mutableStateOf(false) }
        var buffering by remember { mutableStateOf(false) }
        var playerExpanded by rememberSaveable { mutableStateOf(false) }
        var playerPane by rememberSaveable { mutableStateOf(PlayerPane.UP_NEXT) }
        var favorites by remember { mutableStateOf(loadFavorites()) }
        var recentSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
        var playbackQueue by remember { mutableStateOf<List<Song>>(emptyList()) }
        var selectedMood by rememberSaveable { mutableStateOf("All") }
        var showSettings by remember { mutableStateOf(false) }
        var positionMs by remember { mutableLongStateOf(0L) }
        var durationMs by remember { mutableLongStateOf(1L) }

        val colors = darkColorScheme(
            primary = Color(0xFFB69CFF),
            onPrimary = Color(0xFF211044),
            background = Color(0xFF09090B),
            onBackground = Color(0xFFF4F1FA),
            surface = Color(0xFF111116),
            onSurface = Color(0xFFF4F1FA),
            surfaceVariant = Color(0xFF1B1A21),
            onSurfaceVariant = Color(0xFFC8C3D2)
        )

        fun isFavorite(song: Song): Boolean = favorites.any { it.sourceUrl == song.sourceUrl }

        fun toggleFavorite(song: Song) {
            favorites =
                if (isFavorite(song)) favorites.filterNot { it.sourceUrl == song.sourceUrl }
                else listOf(song) + favorites
            saveFavorites(favorites)
        }

        fun playSong(song: Song, queue: List<Song>? = null) {
            val fallbackQueue = (searchResults + quickPicks + madeForYou + trending + favorites)
                .distinctBy { it.sourceUrl }

            playbackQueue =
                when {
                    !queue.isNullOrEmpty() -> queue.distinctBy { it.sourceUrl }
                    playbackQueue.any { it.sourceUrl == song.sourceUrl } -> playbackQueue
                    else -> (listOf(song) + fallbackQueue).distinctBy { it.sourceUrl }
                }

            currentSong = song
            recentSongs = listOf(song) + recentSongs.filterNot { it.sourceUrl == song.sourceUrl }.take(19)
            buffering = true
            error = null

            lifecycleScope.launch {
                runCatching {
                    val url = MusicRepository.resolveAudioUrl(song)
                    player.setMediaItem(MediaItem.fromUri(url))
                    player.prepare()
                    player.play()
                }.onSuccess {
                    isPlaying = true
                }.onFailure {
                    error = it.message ?: "Playback failed"
                    isPlaying = false
                }
                buffering = false
            }
        }

        fun playNext() {
            val queue = playbackQueue.ifEmpty {
                (searchResults + quickPicks + madeForYou + trending + favorites).distinctBy { it.sourceUrl }
            }
            if (queue.isEmpty()) return

            val currentIndex = currentSong?.let { active ->
                queue.indexOfFirst { it.sourceUrl == active.sourceUrl }
            } ?: -1
            val nextIndex = if (currentIndex < 0) 0 else (currentIndex + 1) % queue.size
            playSong(queue[nextIndex], queue)
        }

        fun playPrevious() {
            val queue = playbackQueue.ifEmpty {
                (searchResults + quickPicks + madeForYou + trending + favorites).distinctBy { it.sourceUrl }
            }
            if (queue.isEmpty()) return

            val currentIndex = currentSong?.let { active ->
                queue.indexOfFirst { it.sourceUrl == active.sourceUrl }
            } ?: 0
            val previousIndex = if (currentIndex <= 0) queue.lastIndex else currentIndex - 1
            playSong(queue[previousIndex], queue)
        }

        fun runSearch(term: String = searchText) {
            if (term.isBlank()) return
            searchText = term
            selectedTab = MainTab.SEARCH
            loadingSearch = true
            error = null
            lifecycleScope.launch {
                runCatching { MusicRepository.searchSongs(term.trim()) }
                    .onSuccess { searchResults = it }
                    .onFailure { error = it.message ?: "Search failed" }
                loadingSearch = false
            }
        }

        LaunchedEffect(Unit) {
            loadingHome = true
            val songs = runCatching {
                MusicRepository.searchSongs("Top hits India Bollywood Punjabi 2026")
            }.getOrDefault(emptyList())

            quickPicks = songs.take(12)
            madeForYou = songs.drop(3).take(10)
            trending = songs.reversed().take(10)
            loadingHome = false
        }

        LaunchedEffect(selectedMood) {
            if (selectedMood != "All") {
                loadingHome = true
                runCatching { MusicRepository.searchSongs("$selectedMood music") }
                    .onSuccess {
                        quickPicks = it.take(12)
                        madeForYou = it.drop(2).take(10)
                    }
                loadingHome = false
            }
        }

        LaunchedEffect(currentSong) {
            while (currentSong != null) {
                positionMs = player.currentPosition.coerceAtLeast(0L)
                durationMs = player.duration.takeIf { it > 0 } ?: 1L
                isPlaying = player.isPlaying
                delay(1000)
            }
        }

        BackHandler(enabled = playerExpanded) { playerExpanded = false }

        MaterialTheme(colorScheme = colors) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (playerExpanded && currentSong != null) {
                    NowPlayingScreen(
                        song = currentSong!!,
                        isPlaying = isPlaying,
                        buffering = buffering,
                        positionMs = positionMs,
                        durationMs = durationMs,
                        favorite = isFavorite(currentSong!!),
                        pane = playerPane,
                        queue = (quickPicks + madeForYou).distinctBy { it.sourceUrl },
                        onCollapse = { playerExpanded = false },
                        onTogglePlay = {
                            if (player.isPlaying) player.pause() else player.play()
                            isPlaying = player.isPlaying
                        },
                        onSeek = { fraction ->
                            player.seekTo((durationMs * fraction.coerceIn(0f, 1f)).toLong())
                        },
                        onFavorite = { toggleFavorite(currentSong!!) },
                        onPrevious = { playPrevious() },
                        onNext = { playNext() },
                        onPane = { playerPane = it },
                        onPlaySong = { playSong(it) }
                    )
                } else {
                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.background,
                        bottomBar = {
                            PlayerAndNavigation(
                                selectedTab = selectedTab,
                                currentSong = currentSong,
                                isPlaying = isPlaying,
                                buffering = buffering,
                                progress = (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f),
                                onSelect = { selectedTab = it },
                                onOpenPlayer = { if (currentSong != null) playerExpanded = true },
                                onTogglePlay = {
                                    if (player.isPlaying) player.pause() else player.play()
                                    isPlaying = player.isPlaying
                                },
                                onNext = { playNext() }
                            )
                        }
                    ) { padding ->
                        when (selectedTab) {
                            MainTab.HOME -> HomeScreen(
                                modifier = Modifier.padding(padding),
                                quickPicks = quickPicks,
                                madeForYou = madeForYou,
                                trending = trending,
                                selectedMood = selectedMood,
                                loading = loadingHome,
                                onMood = { selectedMood = it },
                                onPlay = { playSong(it) },
                                onFavorite = { toggleFavorite(it) },
                                favoriteCheck = { isFavorite(it) },
                                onSearch = { selectedTab = MainTab.SEARCH },
                                onHistory = { selectedTab = MainTab.LIBRARY },
                                onSettings = { showSettings = true }
                            )
                            MainTab.SEARCH -> SearchScreen(
                                modifier = Modifier.padding(padding),
                                text = searchText,
                                results = searchResults,
                                loading = loadingSearch,
                                error = error,
                                onText = { searchText = it },
                                onSubmit = { runSearch(it) },
                                onPlay = { playSong(it) },
                                onFavorite = { toggleFavorite(it) },
                                favoriteCheck = { isFavorite(it) }
                            )
                            MainTab.LIBRARY -> LibraryScreen(
                                modifier = Modifier.padding(padding),
                                favorites = favorites,
                                recent = recentSongs,
                                onPlay = { playSong(it) },
                                onFavorite = { toggleFavorite(it) },
                                onSearch = { selectedTab = MainTab.SEARCH }
                            )
                        }
                    }
                }

                if (showSettings) {
                    ModalBottomSheet(
                        onDismissRequest = { showSettings = false },
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        SettingsSheet()
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
                    add(
                        Song(
                            title = o.optString("title"),
                            artist = o.optString("artist"),
                            sourceUrl = o.optString("url"),
                            durationSeconds = o.optLong("duration", 0L),
                            thumbnailUrl = o.optString("thumb").takeIf { it.isNotBlank() }
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun saveFavorites(list: List<Song>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().apply {
                put("title", it.title)
                put("artist", it.artist)
                put("url", it.sourceUrl)
                put("duration", it.durationSeconds)
                put("thumb", it.thumbnailUrl ?: "")
            })
        }
        getSharedPreferences("dhunora", 0).edit().putString("favorites", arr.toString()).apply()
    }
}

@Composable
private fun HomeScreen(
    modifier: Modifier,
    quickPicks: List<Song>,
    madeForYou: List<Song>,
    trending: List<Song>,
    selectedMood: String,
    loading: Boolean,
    onMood: (String) -> Unit,
    onPlay: (Song) -> Unit,
    onFavorite: (Song) -> Unit,
    favoriteCheck: (Song) -> Boolean,
    onSearch: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit
) {
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Late night listening"
        }
    }
    val moods = listOf("All", "Relax", "Energize", "Feel good", "Workout", "Commute")

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 18.dp)
    ) {
        item {
            Column(Modifier.statusBarsPadding().padding(top = 8.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(greeting, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        Text("Dhunora", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.8).sp)
                    }
                    IconButton(onClick = onHistory) { Icon(Icons.Rounded.History, "History") }
                    IconButton(onClick = onSettings) { Icon(Icons.Rounded.Settings, "Settings") }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(moods) { mood ->
                        FilterChip(
                            selected = selectedMood == mood,
                            onClick = { onMood(mood) },
                            label = { Text(mood) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = null
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))
                SectionHeader("Quick picks", "START RADIO", onSearch)
            }
        }

        if (loading && quickPicks.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(quickPicks.chunked(2)) { _, pair ->
                        Column(
                            Modifier.width(310.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { song ->
                                CompactSongRow(
                                    song = song,
                                    favorite = favoriteCheck(song),
                                    onPlay = { onPlay(song) },
                                    onFavorite = { onFavorite(song) }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader("Made for you", "MORE", onSearch)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(madeForYou) { song ->
                    SquareSongCard(song = song, onClick = { onPlay(song) })
                }
            }
        }

        item {
            Spacer(Modifier.height(28.dp))
            Text(
                "Moods & moments",
                Modifier.padding(horizontal = 18.dp),
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(12.dp))
            MoodGrid(onMood)
        }

        item {
            Spacer(Modifier.height(28.dp))
            SectionHeader("Trending now", "MORE", onSearch)
        }

        items(trending.take(8)) { song ->
            SongListRow(
                song = song,
                favorite = favoriteCheck(song),
                onPlay = { onPlay(song) },
                onFavorite = { onFavorite(song) }
            )
        }
    }
}

@Composable
private fun SearchScreen(
    modifier: Modifier,
    text: String,
    results: List<Song>,
    loading: Boolean,
    error: String?,
    onText: (String) -> Unit,
    onSubmit: (String) -> Unit,
    onPlay: (Song) -> Unit,
    onFavorite: (Song) -> Unit,
    favoriteCheck: (Song) -> Boolean
) {
    val focus = LocalFocusManager.current
    var filter by rememberSaveable { mutableStateOf("Songs") }
    val filters = listOf("Songs", "Albums", "Playlists", "Artists")
    val browse = listOf("Bollywood & Indian", "Punjabi", "Chill", "Workout", "Focus", "Romance", "Party", "Sleep")

    LazyColumn(
        modifier = modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Search", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Person, null, modifier = Modifier.size(22.dp))
                    }
                }
            }

            TextField(
                value = text,
                onValueChange = onText,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                shape = RoundedCornerShape(28.dp),
                singleLine = true,
                placeholder = { Text("Songs, artists, albums, playlists") },
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                trailingIcon = {
                    if (text.isNotEmpty()) {
                        IconButton(onClick = { onText("") }) { Icon(Icons.Rounded.Clear, null) }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focus.clearFocus()
                    onSubmit(text)
                }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach {
                    FilterChip(
                        selected = filter == it,
                        onClick = { filter = it },
                        label = { Text(it) },
                        border = null
                    )
                    Spacer(Modifier.width(2.dp))
                }
            }
        }

        if (loading) {
            item { LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 8.dp)) }
        }

        error?.let {
            item {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                )
            }
        }

        if (text.isBlank() && results.isEmpty()) {
            item {
                Text(
                    "Browse all",
                    Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                BrowseGrid(browse = browse, onClick = { onSubmit(it) })
            }
        } else if (!loading && results.isEmpty() && text.isNotBlank()) {
            item {
                Column(
                    Modifier.fillMaxWidth().padding(top = 70.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Rounded.Search, null, modifier = Modifier.size(54.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Text("No results found", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Try another song or artist", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            item {
                Text(
                    "Top results",
                    Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            items(results) { song ->
                SongListRow(
                    song = song,
                    favorite = favoriteCheck(song),
                    onPlay = { onPlay(song) },
                    onFavorite = { onFavorite(song) }
                )
            }
        }
    }
}

@Composable
private fun LibraryScreen(
    modifier: Modifier,
    favorites: List<Song>,
    recent: List<Song>,
    onPlay: (Song) -> Unit,
    onFavorite: (Song) -> Unit,
    onSearch: () -> Unit
) {
    var filter by rememberSaveable { mutableStateOf("Playlists") }
    val filters = listOf("Playlists", "Songs", "Albums", "Artists")

    LazyColumn(
        modifier = modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 18.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(Modifier.size(42.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("A", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text("Library", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                IconButton(onClick = onSearch) { Icon(Icons.Rounded.Search, null) }
            }

            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach {
                    FilterChip(
                        selected = filter == it,
                        onClick = { filter = it },
                        label = { Text(it) },
                        border = null
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            LibraryHeroCard(
                title = "Liked songs",
                subtitle = favorites.size.toString() + " songs",
                icon = Icons.Rounded.Favorite,
                onClick = { favorites.firstOrNull()?.let(onPlay) }
            )
            LibraryHeroCard(
                title = "Recently played",
                subtitle = recent.size.toString() + " items",
                icon = Icons.Rounded.History,
                onClick = { recent.firstOrNull()?.let(onPlay) }
            )
            LibraryHeroCard(
                title = "New playlist",
                subtitle = "Create your own mix",
                icon = Icons.Rounded.Add,
                onClick = {}
            )

            Text(
                "Recently played",
                Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                fontSize = 23.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        if (recent.isEmpty()) {
            item {
                Text(
                    "Songs you play will appear here.",
                    Modifier.padding(horizontal = 18.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(recent) { song ->
                SongListRow(
                    song = song,
                    favorite = favorites.any { it.sourceUrl == song.sourceUrl },
                    onPlay = { onPlay(song) },
                    onFavorite = { onFavorite(song) }
                )
            }
        }

        if (favorites.isNotEmpty()) {
            item {
                Text(
                    "Liked songs",
                    Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                    fontSize = 23.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            items(favorites.take(12)) { song ->
                SongListRow(song, true, { onPlay(song) }, { onFavorite(song) })
            }
        }
    }
}

@Composable
private fun PlayerAndNavigation(
    selectedTab: MainTab,
    currentSong: Song?,
    isPlaying: Boolean,
    buffering: Boolean,
    progress: Float,
    onSelect: (MainTab) -> Unit,
    onOpenPlayer: () -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, MaterialTheme.colorScheme.background.copy(alpha = 0.98f))
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        if (currentSong != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onOpenPlayer),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 8.dp
            ) {
                Column {
                    Row(
                        Modifier.fillMaxWidth().weight(1f).padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Artwork(currentSong, Modifier.size(50.dp), 12.dp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(currentSong.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(currentSong.artist, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        IconButton(onClick = onTogglePlay) {
                            if (buffering) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                            else Icon(if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null)
                        }
                        IconButton(onClick = onNext) { Icon(Icons.Rounded.SkipNext, "Next") }
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.Transparent
                    )
                }
            }
            Spacer(Modifier.height(7.dp))
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(34.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 6.dp
            ) {
                Row(
                    Modifier.height(64.dp).padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomTab(
                        icon = Icons.Rounded.Home,
                        label = "Home",
                        selected = selectedTab == MainTab.HOME,
                        onClick = { onSelect(MainTab.HOME) }
                    )
                    BottomTab(
                        icon = Icons.Rounded.LibraryMusic,
                        label = "Library",
                        selected = selectedTab == MainTab.LIBRARY,
                        onClick = { onSelect(MainTab.LIBRARY) }
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Surface(
                modifier = Modifier.size(56.dp).clickable { onSelect(MainTab.SEARCH) },
                shape = CircleShape,
                color = if (selectedTab == MainTab.SEARCH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.Search,
                        null,
                        tint = if (selectedTab == MainTab.SEARCH) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomTab(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.width(92.dp).fillMaxHeight().clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        color = if (selected) MaterialTheme.colorScheme.background.copy(alpha = 0.72f) else Color.Transparent
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                label,
                fontSize = 11.sp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NowPlayingScreen(
    song: Song,
    isPlaying: Boolean,
    buffering: Boolean,
    positionMs: Long,
    durationMs: Long,
    favorite: Boolean,
    pane: PlayerPane,
    queue: List<Song>,
    onCollapse: () -> Unit,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    onFavorite: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPane: (PlayerPane) -> Unit,
    onPlaySong: (Song) -> Unit
) {
    val fraction = (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    var dragging by remember { mutableFloatStateOf(fraction) }
    LaunchedEffect(fraction) { dragging = fraction }

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF3A285E), Color(0xFF15101E), Color(0xFF09090B)),
                    startY = 0f,
                    endY = 1500f
                )
            )
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCollapse) { Icon(Icons.Rounded.KeyboardArrowDown, "Collapse") }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("NOW PLAYING", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(song.artist, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
                IconButton(onClick = {}) { Icon(Icons.Rounded.MoreVert, null) }
            }

            Artwork(
                song,
                Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 18.dp).aspectRatio(1f),
                24.dp
            )

            Column(Modifier.padding(horizontal = 26.dp)) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(song.title, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(5.dp))
                        Text(song.artist, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                    IconButton(onClick = onFavorite) {
                        Icon(
                            if (favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            null,
                            tint = if (favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))
                Slider(
                    value = dragging,
                    onValueChange = { dragging = it },
                    onValueChangeFinished = { onSeek(dragging) },
                    valueRange = 0f..1f
                )
                Row(Modifier.fillMaxWidth()) {
                    Text(formatTime(positionMs), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.weight(1f))
                    Text(formatTime(durationMs), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(
                    Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {}) { Icon(Icons.Rounded.Shuffle, null) }
                    IconButton(onClick = onPrevious) { Icon(Icons.Rounded.SkipPrevious, "Previous", modifier = Modifier.size(36.dp)) }
                    FilledIconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier.size(72.dp)
                    ) {
                        if (buffering) CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 3.dp, color = MaterialTheme.colorScheme.onPrimary)
                        else Icon(if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, modifier = Modifier.size(38.dp))
                    }
                    IconButton(onClick = onNext) { Icon(Icons.Rounded.SkipNext, "Next", modifier = Modifier.size(36.dp)) }
                    IconButton(onClick = {}) { Icon(Icons.Rounded.Repeat, null) }
                }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PlayerPaneButton("UP NEXT", Icons.Rounded.QueueMusic, pane == PlayerPane.UP_NEXT) { onPane(PlayerPane.UP_NEXT) }
                    PlayerPaneButton("LYRICS", Icons.Rounded.MusicNote, pane == PlayerPane.LYRICS) { onPane(PlayerPane.LYRICS) }
                }
            }

            Spacer(Modifier.height(20.dp))
        }

        if (pane == PlayerPane.UP_NEXT) {
            item {
                Text("Up next", Modifier.padding(horizontal = 22.dp, vertical = 10.dp), fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
            }
            items(queue.filterNot { it.sourceUrl == song.sourceUrl }.take(8)) {
                SongListRow(it, false, { onPlaySong(it) }, {})
            }
        } else {
            item {
                Card(
                    Modifier.fillMaxWidth().padding(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Text("Lyrics", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Synced lyrics will appear here when a lyrics provider is connected.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 26.sp,
                            fontSize = 17.sp
                        )
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun PlayerPaneButton(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
        leadingIcon = { Icon(icon, null, modifier = Modifier.size(17.dp)) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Transparent,
            labelColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            leadingIconContentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = null
    )
}

@Composable
private fun CompactSongRow(song: Song, favorite: Boolean, onPlay: () -> Unit, onFavorite: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(70.dp).clickable(onClick = onPlay),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            Modifier.fillMaxSize().padding(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Artwork(song, Modifier.size(56.dp), 10.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(song.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artist, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onFavorite) {
                Icon(if (favorite) Icons.Rounded.Favorite else Icons.Rounded.MoreVert, null, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun SquareSongCard(song: Song, onClick: () -> Unit) {
    Column(Modifier.width(156.dp).clickable(onClick = onClick)) {
        Artwork(song, Modifier.size(156.dp), 18.dp)
        Spacer(Modifier.height(8.dp))
        Text(song.title, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 18.sp)
        Text(song.artist, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SongListRow(song: Song, favorite: Boolean, onPlay: () -> Unit, onFavorite: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onPlay).padding(horizontal = 18.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Artwork(song, Modifier.size(58.dp), 10.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(
                buildString {
                    append(song.artist)
                    if (song.durationSeconds > 0) append(" • " + formatDuration(song.durationSeconds))
                },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onFavorite) {
            Icon(if (favorite) Icons.Rounded.Favorite else Icons.Rounded.MoreVert, null)
        }
    }
}

@Composable
private fun Artwork(song: Song, modifier: Modifier, radius: Dp) {
    val url = MusicRepository.artworkFor(song)
    var failed by remember(url) { mutableStateOf(false) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(radius),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF34264A), Color(0xFF18151F))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!url.isNullOrBlank() && !failed) {
                AsyncImage(
                    model = url,
                    contentDescription = song.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onError = { failed = true }
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        song.title,
                        color = Color.White.copy(alpha = 0.78f),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, action: String, onAction: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
        Text(
            action,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onAction).padding(8.dp)
        )
    }
}

@Composable
private fun MoodGrid(onMood: (String) -> Unit) {
    val entries = listOf(
        "Energy Boosters" to Color(0xFFCA8CFF),
        "Chill" to Color(0xFF22D3A7),
        "Feel Good" to Color(0xFF53E276),
        "Focus" to Color(0xFF38A6FF),
        "Bollywood & Indian" to Color(0xFFFF6D77),
        "Romance" to Color(0xFFFF70A6)
    )
    Column(Modifier.padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        entries.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { entry ->
                    val title = entry.first
                    val color = entry.second
                    Surface(
                        modifier = Modifier.weight(1f).height(64.dp).clickable { onMood(title) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.width(7.dp).fillMaxHeight().background(color))
                            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                            }
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BrowseGrid(browse: List<String>, onClick: (String) -> Unit) {
    val colors = listOf(
        Color(0xFF7947D6), Color(0xFFE65C7B), Color(0xFF2C9E7B), Color(0xFFB77727),
        Color(0xFF286FB8), Color(0xFF9E3C70), Color(0xFF59853A), Color(0xFF6547A8)
    )
    Column(Modifier.padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        browse.chunked(2).forEachIndexed { rowIndex, row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEachIndexed { index, title ->
                    val color = colors[(rowIndex * 2 + index) % colors.size]
                    Surface(
                        modifier = Modifier.weight(1f).height(94.dp).clickable { onClick(title) },
                        shape = RoundedCornerShape(14.dp),
                        color = color
                    ) {
                        Box(Modifier.fillMaxSize().padding(12.dp)) {
                            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, modifier = Modifier.align(Alignment.TopStart))
                            Icon(Icons.Rounded.MusicNote, null, modifier = Modifier.align(Alignment.BottomEnd).size(30.dp), tint = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryHeroCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 5.dp).height(76.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(50.dp), shape = RoundedCornerShape(13.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) }
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsSheet() {
    Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 36.dp)) {
        Text("Dhunora", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Text("by Alok", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        SettingRow(Icons.Rounded.Tune, "Interface", "SimpMusic-inspired floating navigation")
        SettingRow(Icons.Rounded.MusicNote, "Playback", "YouTube / YouTube Music stream resolver")
        SettingRow(Icons.Rounded.Favorite, "Library", "Likes are saved locally on your phone")
        HorizontalDivider(Modifier.padding(vertical = 14.dp))
        Text(
            "Dhunora is an independent open-source client. It is not affiliated with Google, YouTube or SimpMusic.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, subtitle: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return m.toString() + ":" + s.toString().padStart(2, '0')
}

private fun formatTime(ms: Long): String {
    val total = (ms.coerceAtLeast(0L) / 1000)
    val m = total / 60
    val s = total % 60
    return m.toString() + ":" + s.toString().padStart(2, '0')
}
