package com.alok.dhunora

import android.Manifest
import android.app.DownloadManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Groups
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
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil3.compose.AsyncImage
import com.google.common.util.concurrent.ListenableFuture
import com.alok.dhunora.account.AccountSession
import com.alok.dhunora.account.AccountProfileRepository
import com.alok.dhunora.account.LoginActivity
import com.alok.dhunora.account.SpotifyLoginActivity
import com.alok.dhunora.account.SpotifySession
import com.alok.dhunora.data.DownloadRecord
import com.alok.dhunora.data.DownloadedSongStore
import com.alok.dhunora.data.ListeningProfileStore
import com.alok.dhunora.data.LocalPlaylistStore
import com.alok.dhunora.data.LyricsRepository
import com.alok.dhunora.data.LyricsResult
import com.alok.dhunora.data.MusicRepository
import com.alok.dhunora.data.UiSettingsStore
import com.alok.dhunora.model.MusicSearchItem
import com.alok.dhunora.model.SearchKind
import com.alok.dhunora.model.Song
import com.alok.dhunora.player.PlaybackService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.util.Calendar

private enum class MainTab { HOME, MIX, LIBRARY, SEARCH }
private enum class PlayerPane { UP_NEXT, LYRICS }

private data class LibraryCollection(
    val title: String,
    val subtitle: String,
    val songs: List<Song> = emptyList(),
    val downloads: List<DownloadRecord> = emptyList()
)

class MainActivity : ComponentActivity() {
    private lateinit var player: MediaController
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private var sleepTimerJob: Job? = null
    private var searchJob: Job? = null
    private var recommendationJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }

        lifecycleScope.launch {
            controllerFuture =
                MediaController.Builder(
                    this@MainActivity,
                    SessionToken(
                        this@MainActivity,
                        ComponentName(this@MainActivity, PlaybackService::class.java)
                    )
                ).buildAsync()

            player = controllerFuture.await()
            setContent { DhunoraApp() }
        }
    }

    override fun onDestroy() {
        searchJob?.cancel()
        recommendationJob?.cancel()
        sleepTimerJob?.cancel()
        if (::controllerFuture.isInitialized) {
            MediaController.releaseFuture(controllerFuture)
        }
        super.onDestroy()
    }


    private fun downloadSong(song: Song) {
        lifecycleScope.launch {
            val url =
                runCatching { MusicRepository.resolveAudioUrl(song) }
                    .getOrElse {
                        Toast.makeText(this@MainActivity, "Download failed", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

            val safeName =
                song.title
                    .replace(Regex("[^A-Za-z0-9 _.-]"), "")
                    .trim()
                    .take(80)
                    .ifBlank { "Dhunora song" }

            val request =
                DownloadManager.Request(Uri.parse(url))
                    .setTitle(song.title)
                    .setDescription(song.artist)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_MUSIC,
                        "Dhunora/" + safeName + ".m4a"
                    )

            val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = manager.enqueue(request)
            DownloadedSongStore.add(this@MainActivity, downloadId, song)
            Toast.makeText(
                this@MainActivity,
                "Download added to Dhunora",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun shareSong(song: Song) {
        val intent =
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, song.title)
                putExtra(
                    Intent.EXTRA_TEXT,
                    song.title + " — " + song.artist + "\n" + song.sourceUrl
                )
            }
        startActivity(Intent.createChooser(intent, "Share song"))
    }

    private fun addToMyPlaylist(song: Song) {
        LocalPlaylistStore.addToMyPlaylist(this, song)
        Toast.makeText(this, "Added to My Playlist", Toast.LENGTH_SHORT).show()
    }

    private fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        sleepTimerJob =
            lifecycleScope.launch {
                delay(minutes * 60_000L)
                player.pause()
            }
        Toast.makeText(this, "Sleep timer: " + minutes + " min", Toast.LENGTH_SHORT).show()
    }

    private fun setPlayback(speed: Float, pitch: Float) {
        player.setPlaybackParameters(PlaybackParameters(speed, pitch))
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun DhunoraApp() {
        var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }
        var searchText by rememberSaveable { mutableStateOf("") }
        var searchResults by remember { mutableStateOf<List<MusicSearchItem>>(emptyList()) }
        var liveSearchResults by remember { mutableStateOf<List<MusicSearchItem>>(emptyList()) }
        var searchCommitted by rememberSaveable { mutableStateOf(false) }
        var selectedSearchKind by rememberSaveable { mutableStateOf(SearchKind.ALL) }
        var openedSearchItem by remember { mutableStateOf<MusicSearchItem?>(null) }
        var openedSearchSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
        var loadingSearchDetail by remember { mutableStateOf(false) }
        var quickPicks by remember { mutableStateOf<List<Song>>(emptyList()) }
        var madeForYou by remember { mutableStateOf<List<Song>>(emptyList()) }
        var trending by remember { mutableStateOf<List<Song>>(emptyList()) }
        var loadingSearch by remember { mutableStateOf(false) }
        var searchRequestId by remember { mutableLongStateOf(0L) }
        var loadingHome by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }
        var currentSong by remember { mutableStateOf<Song?>(null) }
        var isPlaying by remember { mutableStateOf(false) }
        var buffering by remember { mutableStateOf(false) }
        var playerExpanded by rememberSaveable { mutableStateOf(false) }
        var playerPane by rememberSaveable { mutableStateOf(PlayerPane.LYRICS) }
        var favorites by remember { mutableStateOf(loadFavorites()) }
        var recentSongs by remember { mutableStateOf(ListeningProfileStore.recent(this@MainActivity)) }
        var searchSignals by remember {
            mutableStateOf(ListeningProfileStore.recentSearches(this@MainActivity))
        }
        var playbackQueue by remember { mutableStateOf<List<Song>>(emptyList()) }
        var selectedMood by rememberSaveable { mutableStateOf("All") }
        var showSettings by remember { mutableStateOf(false) }
        var showSongMenu by remember { mutableStateOf(false) }
        var showAccountSheet by remember { mutableStateOf(false) }
        var youtubeLoggedIn by remember { mutableStateOf(AccountSession.isLoggedIn(this@MainActivity)) }
        var spotifyLoggedIn by remember {
            mutableStateOf(SpotifySession.isLoggedIn(this@MainActivity))
        }
        var lyricsResult by remember { mutableStateOf<LyricsResult?>(null) }
        var lyricsLoading by remember { mutableStateOf(false) }
        var profileName by remember {
            mutableStateOf(AccountSession.profileName(this@MainActivity))
        }
        var profileAvatar by remember {
            mutableStateOf(AccountSession.profileAvatar(this@MainActivity))
        }
        var youtubeLikedSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
        var youtubeLikedLoading by remember { mutableStateOf(false) }
        var downloads by remember {
            mutableStateOf(DownloadedSongStore.records(this@MainActivity))
        }
        var libraryCollection by remember { mutableStateOf<LibraryCollection?>(null) }
        var translucentNav by rememberSaveable {
            mutableStateOf(UiSettingsStore.translucentNav(this@MainActivity))
        }
        var liquidGlass by rememberSaveable {
            mutableStateOf(UiSettingsStore.liquidGlass(this@MainActivity))
        }
        var romanizedLyrics by rememberSaveable {
            mutableStateOf(UiSettingsStore.romanizedLyrics(this@MainActivity))
        }
        var nowPlayingStyle by rememberSaveable {
            mutableStateOf(UiSettingsStore.nowPlayingStyle(this@MainActivity))
        }
        var lyricsStyle by rememberSaveable {
            mutableStateOf(UiSettingsStore.lyricsStyle(this@MainActivity))
        }
        var themeColor by rememberSaveable {
            mutableStateOf(UiSettingsStore.themeColor(this@MainActivity))
        }
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

        val dhunoraFont =
            FontFamily(
                Font(
                    resId = R.font.manrope_variable,
                    weight = FontWeight.Normal
                )
            )

        val appTypography = Typography(
            displayLarge = TextStyle(
                fontFamily = dhunoraFont,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                letterSpacing = (-0.5).sp
            ),
            headlineLarge = TextStyle(
                fontFamily = dhunoraFont,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                letterSpacing = (-0.35).sp
            ),
            headlineMedium = TextStyle(
                fontFamily = dhunoraFont,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
                letterSpacing = (-0.25).sp
            ),
            titleLarge = TextStyle(
                fontFamily = dhunoraFont,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                letterSpacing = (-0.15).sp
            ),
            titleMedium = TextStyle(
                fontFamily = dhunoraFont,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            ),
            titleSmall = TextStyle(
                fontFamily = dhunoraFont,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            ),
            bodyLarge = TextStyle(
                fontFamily = dhunoraFont,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = dhunoraFont,
                fontWeight = FontWeight.Normal,
                fontSize = 12.5.sp,
                lineHeight = 18.sp
            ),
            bodySmall = TextStyle(
                fontFamily = dhunoraFont,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp
            ),
            labelLarge = TextStyle(
                fontFamily = dhunoraFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            ),
            labelMedium = TextStyle(
                fontFamily = dhunoraFont,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
        )

        val loginLauncher =
            rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {
                youtubeLoggedIn = AccountSession.isLoggedIn(this@MainActivity)
                profileName = AccountSession.profileName(this@MainActivity)
                profileAvatar = AccountSession.profileAvatar(this@MainActivity)
                if (youtubeLoggedIn) {
                    lifecycleScope.launch {
                        AccountProfileRepository.refresh(this@MainActivity)
                        profileName = AccountSession.profileName(this@MainActivity)
                        profileAvatar = AccountSession.profileAvatar(this@MainActivity)
                    }
                }
            }

        val spotifyLoginLauncher =
            rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {
                spotifyLoggedIn = SpotifySession.isLoggedIn(this@MainActivity)
            }

        fun openSpotify() {
            if (spotifyLoggedIn) {
                SpotifySession.clear(this@MainActivity)
                spotifyLoggedIn = false
                Toast.makeText(
                    this@MainActivity,
                    "Spotify disconnected",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                spotifyLoginLauncher.launch(
                    Intent(this@MainActivity, SpotifyLoginActivity::class.java)
                )
            }
        }

        fun openAccount() {
            if (youtubeLoggedIn) {
                showAccountSheet = true
            } else {
                loginLauncher.launch(
                    Intent(this@MainActivity, LoginActivity::class.java)
                )
            }
        }

        fun isFavorite(song: Song): Boolean = favorites.any { it.sourceUrl == song.sourceUrl }

        fun toggleFavorite(song: Song) {
            favorites =
                if (isFavorite(song)) favorites.filterNot { it.sourceUrl == song.sourceUrl }
                else listOf(song) + favorites
            saveFavorites(favorites)
        }

        fun fallbackQueue(): List<Song> =
            (
                searchResults.mapNotNull { it.toSongOrNull() } +
                    quickPicks +
                    madeForYou +
                    trending +
                    favorites
            ).distinctBy { it.sourceUrl }

        fun prefetchFollowing(song: Song, queue: List<Song>) {
            if (queue.size <= 1) return
            val index = queue.indexOfFirst { it.sourceUrl == song.sourceUrl }
            if (index < 0) return

            val upcoming = buildList {
                for (step in 1..2) {
                    add(queue[(index + step) % queue.size])
                }
            }.distinctBy { it.sourceUrl }

            lifecycleScope.launch {
                MusicRepository.prefetchAudioUrls(upcoming)
            }
        }

        fun playSong(
            song: Song,
            queue: List<Song>? = null,
            skipAttempts: Int = 0
        ) {
            val resolvedQueue =
                when {
                    !queue.isNullOrEmpty() -> queue.distinctBy { it.sourceUrl }
                    playbackQueue.any { it.sourceUrl == song.sourceUrl } -> playbackQueue
                    else -> (listOf(song) + fallbackQueue()).distinctBy { it.sourceUrl }
                }

            playbackQueue = resolvedQueue
            buffering = true
            error = null

            lifecycleScope.launch {
                val resolved =
                    if (!song.localUri.isNullOrBlank()) {
                        Result.success(song.localUri)
                    } else {
                        runCatching { MusicRepository.resolveAudioUrl(song) }
                    }

                resolved.onSuccess { url ->
                    currentSong = song
                    recentSongs =
                        listOf(song) +
                            recentSongs
                                .filterNot { it.sourceUrl == song.sourceUrl }
                                .take(49)
                    ListeningProfileStore.recordPlay(this@MainActivity, song)

                    val metadata =
                        MediaMetadata.Builder()
                            .setTitle(song.title)
                            .setArtist(song.artist)
                            .setAlbumTitle("Dhunora")
                            .apply {
                                MusicRepository.artworkFor(song)
                                    ?.takeIf { it.isNotBlank() }
                                    ?.let { setArtworkUri(Uri.parse(it)) }
                            }
                            .build()

                    val mediaItem =
                        MediaItem.Builder()
                            .setMediaId(song.sourceUrl)
                            .setUri(url)
                            .setMediaMetadata(metadata)
                            .build()

                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.play()
                    buffering = true

                    if (song.localUri.isNullOrBlank()) {
                        prefetchFollowing(song, resolvedQueue)
                    }
                }.onFailure {
                    if (song.localUri.isNullOrBlank()) {
                        MusicRepository.invalidateAudioUrl(song)
                    }

                    if (resolvedQueue.size > 1 && skipAttempts < resolvedQueue.size - 1) {
                        val failedIndex =
                            resolvedQueue.indexOfFirst { it.sourceUrl == song.sourceUrl }
                                .takeIf { it >= 0 } ?: 0
                        val nextIndex = (failedIndex + 1) % resolvedQueue.size

                        playSong(
                            song = resolvedQueue[nextIndex],
                            queue = resolvedQueue,
                            skipAttempts = skipAttempts + 1
                        )
                    } else {
                        buffering = false
                        isPlaying = false
                        error = "No playable track found in this queue"
                    }
                }
            }
        }

        fun startRecommendationRadio(song: Song) {
            recommendationJob?.cancel()

            val seedQueue = listOf(song)
            playbackQueue = seedQueue
            playSong(song, seedQueue)

            recommendationJob =
                lifecycleScope.launch {
                    val related =
                        runCatching { MusicRepository.relatedSongs(song) }
                            .getOrDefault(emptyList())

                    if (currentSong?.sourceUrl == song.sourceUrl) {
                        playbackQueue =
                            (listOf(song) + related)
                                .distinctBy { it.sourceUrl }

                        prefetchFollowing(song, playbackQueue)
                    }
                }
        }

        fun playNext() {
            val queue = playbackQueue.ifEmpty { fallbackQueue() }
            if (queue.isEmpty()) return

            val active = currentSong ?: queue.first()
            val currentIndex =
                queue.indexOfFirst { it.sourceUrl == active.sourceUrl }
                    .takeIf { it >= 0 } ?: 0

            if (queue.size <= 1 || currentIndex >= queue.lastIndex) {
                buffering = true
                recommendationJob?.cancel()
                recommendationJob =
                    lifecycleScope.launch {
                        val related =
                            runCatching { MusicRepository.relatedSongs(active) }
                                .getOrDefault(emptyList())

                        val expanded =
                            (queue + related)
                                .distinctBy { it.sourceUrl }

                        playbackQueue = expanded

                        val activeIndex =
                            expanded.indexOfFirst {
                                it.sourceUrl == active.sourceUrl
                            }.takeIf { it >= 0 } ?: 0

                        val next =
                            expanded.getOrNull(activeIndex + 1)
                                ?: expanded.firstOrNull {
                                    it.sourceUrl != active.sourceUrl
                                }

                        if (next != null) {
                            playSong(
                                song = next,
                                queue = expanded,
                                skipAttempts = 0
                            )
                        } else {
                            buffering = false
                        }
                    }
                return
            }

            playSong(
                song = queue[currentIndex + 1],
                queue = queue,
                skipAttempts = 0
            )
        }

        fun playPrevious() {
            val queue = playbackQueue.ifEmpty { fallbackQueue() }
            if (queue.isEmpty()) return

            val currentIndex =
                currentSong?.let { active ->
                    queue.indexOfFirst { it.sourceUrl == active.sourceUrl }
                } ?: 0

            val previousIndex =
                if (currentIndex <= 0) queue.lastIndex
                else currentIndex - 1

            playSong(
                song = queue[previousIndex],
                queue = queue,
                skipAttempts = 0
            )
        }

        fun runSearch(term: String = searchText, kind: SearchKind = selectedSearchKind) {
            val clean = term.trim()
            if (clean.isBlank()) return

            searchText = term
            selectedSearchKind = kind
            selectedTab = MainTab.SEARCH
            searchCommitted = true
            error = null

            searchRequestId += 1L
            val requestId = searchRequestId

            searchJob?.cancel()
            loadingSearch = true

            searchJob =
                lifecycleScope.launch {
                    try {
                        val results = MusicRepository.search(clean, kind)

                        if (requestId == searchRequestId) {
                            searchResults = results
                            error = null

                            if (
                                kind == SearchKind.ALL ||
                                kind == SearchKind.SONG ||
                                kind == SearchKind.VIDEO
                            ) {
                                val likelyNext =
                                    results
                                        .mapNotNull { result -> result.toSongOrNull() }
                                        .take(3)

                                lifecycleScope.launch {
                                    MusicRepository.prefetchAudioUrls(likelyNext)
                                }
                            }
                        }
                    } catch (_: CancellationException) {
                        // Realtime typing intentionally cancels the previous request.
                    } catch (t: Throwable) {
                        if (requestId == searchRequestId) {
                            val message = t.message.orEmpty()
                            error =
                                if (
                                    message.contains("cancel", ignoreCase = true) ||
                                    message.contains("StandaloneCoroutine", ignoreCase = true)
                                ) {
                                    null
                                } else {
                                    message.ifBlank { "Search failed" }
                                }
                        }
                    } finally {
                        if (requestId == searchRequestId) {
                            loadingSearch = false
                        }
                    }
                }
        }

        fun openSearchResult(item: MusicSearchItem) {
            val signal =
                searchText.trim().takeIf { it.length >= 2 }
                    ?: (item.title + " " + item.subtitle).trim()
            ListeningProfileStore.recordSearch(this@MainActivity, signal)
            searchSignals =
                ListeningProfileStore.recentSearches(this@MainActivity)

            if (
                item.kind == SearchKind.SONG ||
                item.kind == SearchKind.VIDEO
            ) {
                val song = item.toSongOrNull() ?: return
                startRecommendationRadio(song)
                return
            }

            openedSearchItem = item
            openedSearchSongs = emptyList()
            loadingSearchDetail = true
            lifecycleScope.launch {
                runCatching { MusicRepository.loadCollection(item) }
                    .onSuccess { openedSearchSongs = it }
                    .onFailure { error = it.message ?: "Could not load " + item.kind.label.lowercase() }
                loadingSearchDetail = false
            }
        }

        LaunchedEffect(searchText, selectedTab, searchCommitted) {
            val clean = searchText.trim()

            if (
                selectedTab != MainTab.SEARCH ||
                searchCommitted ||
                clean.length < 2
            ) {
                if (clean.length < 2) liveSearchResults = emptyList()
                return@LaunchedEffect
            }

            delay(220)

            try {
                val mixed =
                    MusicRepository.search(clean, SearchKind.ALL)
                        .take(8)

                if (
                    selectedTab == MainTab.SEARCH &&
                    !searchCommitted &&
                    searchText.trim().equals(clean, ignoreCase = true)
                ) {
                    liveSearchResults = mixed
                }
            } catch (_: CancellationException) {
                // Expected while the user keeps typing.
            }
        }

        LaunchedEffect(searchText, selectedTab) {
            if (
                selectedTab == MainTab.SEARCH &&
                searchText.trim().length >= 3
            ) {
                delay(1400)
                ListeningProfileStore.recordSearch(
                    this@MainActivity,
                    searchText.trim()
                )
                searchSignals =
                    ListeningProfileStore.recentSearches(this@MainActivity)
            }
        }

        LaunchedEffect(selectedTab) {
            if (selectedTab == MainTab.LIBRARY) {
                while (true) {
                    downloads = DownloadedSongStore.records(this@MainActivity)
                    delay(1500)
                }
            }
        }

        LaunchedEffect(youtubeLoggedIn) {
            if (youtubeLoggedIn) {
                runCatching {
                    AccountProfileRepository.refresh(this@MainActivity)
                }
                profileName = AccountSession.profileName(this@MainActivity)
                profileAvatar = AccountSession.profileAvatar(this@MainActivity)
                youtubeLikedLoading = true
                youtubeLikedSongs =
                    runCatching { MusicRepository.loadYouTubeLikedMusic() }
                        .getOrDefault(emptyList())
                youtubeLikedLoading = false
            } else {
                youtubeLikedSongs = emptyList()
                youtubeLikedLoading = false
            }
        }

        LaunchedEffect(currentSong?.sourceUrl, spotifyLoggedIn) {
            val active = currentSong
            if (active == null) {
                lyricsResult = null
                lyricsLoading = false
            } else {
                lyricsLoading = true
                lyricsResult =
                    runCatching {
                        LyricsRepository.load(this@MainActivity, active)
                    }.getOrNull()
                lyricsLoading = false
            }
        }

        val homeProfileKey =
            buildString {
                append(
                    (
                        recentSongs.take(10) +
                            favorites.take(6) +
                            youtubeLikedSongs.take(6)
                    ).joinToString("|") { it.sourceUrl }
                )
                append("::")
                append(searchSignals.take(8).joinToString("|"))
            }

        LaunchedEffect(homeProfileKey, selectedMood, selectedTab) {
            if (selectedTab != MainTab.HOME) return@LaunchedEffect
            loadingHome = true

            val songs =
                if (selectedMood == "All") {
                    runCatching {
                        MusicRepository.personalizedHome(
                            recent = recentSongs,
                            favorites = favorites,
                            accountLikes = youtubeLikedSongs,
                            searchTerms = searchSignals
                        )
                    }.getOrDefault(emptyList())
                } else {
                    runCatching {
                        MusicRepository.searchSongs(selectedMood + " music")
                    }.getOrDefault(emptyList())
                }

            val fallback =
                if (songs.isEmpty()) {
                    runCatching {
                        MusicRepository.searchSongs("Top music India")
                    }.getOrDefault(emptyList())
                } else {
                    songs
                }

            quickPicks = fallback.take(12)
            madeForYou =
                fallback.drop(12).take(10)
                    .ifEmpty { fallback.drop(4).take(10) }
            trending =
                fallback.drop(22).take(12)
                    .ifEmpty { fallback.drop(8).take(12) }

            lifecycleScope.launch {
                MusicRepository.prefetchAudioUrls(fallback.take(2))
            }

            loadingHome = false
        }

        LaunchedEffect(currentSong, playerExpanded) {
            while (currentSong != null) {
                positionMs = player.currentPosition.coerceAtLeast(0L)
                durationMs = player.duration.takeIf { it > 0 } ?: 1L
                delay(if (playerExpanded) 500 else 1500)
            }
        }

        DisposableEffect(currentSong) {
            val failedSong = currentSong
            val listener =
                object : Player.Listener {
                    override fun onIsPlayingChanged(value: Boolean) {
                        isPlaying = value
                    }

                    override fun onPlaybackStateChanged(state: Int) {
                        buffering = state == Player.STATE_BUFFERING
                    }

                    override fun onPlayerError(errorValue: PlaybackException) {
                        failedSong?.let { MusicRepository.invalidateAudioUrl(it) }
                        buffering = true
                        isPlaying = false

                        lifecycleScope.launch {
                            delay(120)
                            playNext()
                        }
                    }
                }

            player.addListener(listener)

            onDispose {
                player.removeListener(listener)
            }
        }

        BackHandler(enabled = playerExpanded) { playerExpanded = false }
        BackHandler(enabled = !playerExpanded && openedSearchItem != null) {
            openedSearchItem = null
            openedSearchSongs = emptyList()
        }
        BackHandler(
            enabled =
                !playerExpanded &&
                    openedSearchItem == null &&
                    libraryCollection != null
        ) {
            libraryCollection = null
        }
        BackHandler(
            enabled =
                !playerExpanded &&
                    openedSearchItem == null &&
                    libraryCollection == null &&
                    showSettings
        ) {
            showSettings = false
        }

        MaterialTheme(colorScheme = colors, typography = appTypography) {
            ProvideTextStyle(value = appTypography.bodyLarge) {
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
                        queue = playbackQueue.ifEmpty { (quickPicks + madeForYou).distinctBy { it.sourceUrl } },
                        onCollapse = { playerExpanded = false },
                        onTogglePlay = {
                            if (player.isPlaying) player.pause() else player.play()
                            isPlaying = player.isPlaying
                        },
                        onSeek = { fraction ->
                            player.seekTo((durationMs * fraction.coerceIn(0f, 1f)).toLong())
                        },
                        onFavorite = { toggleFavorite(currentSong!!) },
                        onMore = { showSongMenu = true },
                        onPrevious = { playPrevious() },
                        onNext = { playNext() },
                        onPane = { playerPane = it },
                        onPlaySong = { playSong(it) },
                        onAdd = { addToMyPlaylist(currentSong!!) },
                        onRadio = { startRecommendationRadio(currentSong!!) },
                        style = nowPlayingStyle,
                        liquidGlass = liquidGlass,
                        romanizedLyrics = romanizedLyrics,
                        lyricsLines = lyricsResult?.lines.orEmpty(),
                        lyricsSource = lyricsResult?.source,
                        lyricsLoading = lyricsLoading,
                        canvasUrl = lyricsResult?.canvasUrl
                    )
                } else if (openedSearchItem != null) {
                    CollectionDetailScreen(
                        item = openedSearchItem!!,
                        songs = openedSearchSongs,
                        loading = loadingSearchDetail,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        onBack = {
                            openedSearchItem = null
                            openedSearchSongs = emptyList()
                        },
                        onPlayAll = {
                            openedSearchSongs.firstOrNull()?.let { first ->
                                playSong(first, openedSearchSongs)
                            }
                        },
                        onPlaySong = { song -> playSong(song, openedSearchSongs) },
                        onFavorite = { toggleFavorite(it) },
                        favoriteCheck = { isFavorite(it) }
                    )
                } else if (libraryCollection != null) {
                    LibraryCollectionScreen(
                        collection = libraryCollection!!,
                        favorites = favorites,
                        onBack = { libraryCollection = null },
                        onPlay = { song ->
                            val songs = libraryCollection?.songs.orEmpty()
                            if (songs.isNotEmpty()) {
                                playSong(song, songs)
                            } else {
                                playSong(song)
                            }
                        },
                        onFavorite = { toggleFavorite(it) }
                    )
                } else if (showSettings) {
                    SettingsScreen(
                        youtubeLoggedIn = youtubeLoggedIn,
                        spotifyLoggedIn = spotifyLoggedIn,
                        translucentNav = translucentNav,
                        liquidGlass = liquidGlass,
                        romanizedLyrics = romanizedLyrics,
                        nowPlayingStyle = nowPlayingStyle,
                        lyricsStyle = lyricsStyle,
                        themeColor = themeColor,
                        onBack = { showSettings = false },
                        onAccount = {
                            showSettings = false
                            openAccount()
                        },
                        onSpotify = {
                            openSpotify()
                        },
                        onTranslucentNav = {
                            translucentNav = it
                            UiSettingsStore.setTranslucentNav(this@MainActivity, it)
                        },
                        onLiquidGlass = {
                            liquidGlass = it
                            UiSettingsStore.setLiquidGlass(this@MainActivity, it)
                        },
                        onRomanizedLyrics = {
                            romanizedLyrics = it
                            UiSettingsStore.setRomanizedLyrics(this@MainActivity, it)
                        },
                        onNowPlayingStyle = {
                            nowPlayingStyle = it
                            UiSettingsStore.setNowPlayingStyle(this@MainActivity, it)
                        },
                        onLyricsStyle = {
                            lyricsStyle = it
                            UiSettingsStore.setLyricsStyle(this@MainActivity, it)
                        },
                        onThemeColor = {
                            themeColor = it
                            UiSettingsStore.setThemeColor(this@MainActivity, it)
                        }
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
                                onNext = { playNext() },
                                glassEnabled = translucentNav || liquidGlass
                            )
                        }
                    ) { padding ->
                        Crossfade(
                            targetState = selectedTab,
                            animationSpec = tween(durationMillis = 220),
                            label = "main_tab_crossfade"
                        ) { activeTab ->
                        when (activeTab) {
                            MainTab.HOME -> HomeScreen(
                                modifier = Modifier.padding(padding),
                                quickPicks = quickPicks,
                                madeForYou = madeForYou,
                                trending = trending,
                                selectedMood = selectedMood,
                                loading = loadingHome,
                                onMood = { selectedMood = it },
                                onPlay = { startRecommendationRadio(it) },
                                onFavorite = { toggleFavorite(it) },
                                favoriteCheck = { isFavorite(it) },
                                youtubeLoggedIn = youtubeLoggedIn,
                                profileName = profileName,
                                profileAvatar = profileAvatar,
                                onAccount = { openAccount() },
                                onSearch = { selectedTab = MainTab.SEARCH },
                                onHistory = { selectedTab = MainTab.LIBRARY },
                                onNotifications = {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "No new music notifications",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onSettings = { showSettings = true }
                            )
                            MainTab.MIX -> MixScreen(
                                modifier = Modifier.padding(padding),
                                songs = (quickPicks + madeForYou + trending)
                                    .distinctBy { it.sourceUrl },
                                onPlay = { startRecommendationRadio(it) },
                                onFavorite = { toggleFavorite(it) },
                                favoriteCheck = { isFavorite(it) }
                            )
                            MainTab.SEARCH -> SearchScreen(
                                modifier = Modifier.padding(padding),
                                text = searchText,
                                results = searchResults,
                                liveResults = liveSearchResults,
                                committed = searchCommitted,
                                selectedKind = selectedSearchKind,
                                loading = loadingSearch,
                                error = error,
                                onText = { value ->
                                    searchText = value
                                    searchCommitted = false
                                    error = null

                                    if (value.isBlank()) {
                                        searchResults = emptyList()
                                        liveSearchResults = emptyList()
                                    }
                                },
                                onSubmit = { query, kind -> runSearch(query, kind) },
                                onSuggestion = { suggestion ->
                                    searchText = suggestion
                                    searchCommitted = false
                                },
                                onKindChange = { kind ->
                                    selectedSearchKind = kind
                                    if (searchCommitted && searchText.isNotBlank()) {
                                        runSearch(searchText, kind)
                                    }
                                },
                                onOpen = { openSearchResult(it) },
                                onFavorite = { song -> toggleFavorite(song) },
                                favoriteCheck = { song -> isFavorite(song) },
                                accountConnected = youtubeLoggedIn,
                                onAccount = { openAccount() }
                            )
                            MainTab.LIBRARY -> LibraryScreen(
                                modifier = Modifier.padding(padding),
                                favorites = favorites,
                                recent = recentSongs,
                                youtubeLiked = youtubeLikedSongs,
                                youtubeLoggedIn = youtubeLoggedIn,
                                youtubeLoading = youtubeLikedLoading,
                                myPlaylist = LocalPlaylistStore.myPlaylist(this@MainActivity),
                                downloads = downloads,
                                onPlay = { playSong(it) },
                                onFavorite = { toggleFavorite(it) },
                                onSearch = { selectedTab = MainTab.SEARCH },
                                onAccount = { openAccount() },
                                onOpenCollection = { title, subtitle, songs, downloadItems ->
                                    libraryCollection =
                                        LibraryCollection(
                                            title = title,
                                            subtitle = subtitle,
                                            songs = songs,
                                            downloads = downloadItems
                                        )
                                }
                            )
                        }
                        }
                    }
                }

                if (showSongMenu && currentSong != null) {
                    ModalBottomSheet(
                        onDismissRequest = { showSongMenu = false },
                        containerColor = Color(0xFF242424),
                        dragHandle = {
                            Surface(
                                Modifier
                                    .padding(top = 10.dp, bottom = 8.dp)
                                    .width(72.dp)
                                    .height(5.dp),
                                shape = RoundedCornerShape(100.dp),
                                color = Color(0xFF5D5D5D)
                            ) {}
                        }
                    ) {
                        SongActionSheet(
                            song = currentSong!!,
                            favorite = isFavorite(currentSong!!),
                            onLike = { toggleFavorite(currentSong!!) },
                            onDownload = {
                                showSongMenu = false
                                downloadSong(currentSong!!)
                            },
                            onAddPlaylist = {
                                addToMyPlaylist(currentSong!!)
                                showSongMenu = false
                            },
                            onPlayNext = {
                                val song = currentSong!!
                                val queue = playbackQueue.ifEmpty { fallbackQueue() }.toMutableList()
                                val index =
                                    queue.indexOfFirst { it.sourceUrl == song.sourceUrl }
                                        .takeIf { it >= 0 } ?: 0
                                queue.add((index + 1).coerceAtMost(queue.size), song)
                                playbackQueue = queue
                                Toast.makeText(this@MainActivity, "Added to play next", Toast.LENGTH_SHORT).show()
                                showSongMenu = false
                            },
                            onAddQueue = {
                                val song = currentSong!!
                                playbackQueue =
                                    (playbackQueue.ifEmpty { fallbackQueue() } + song)
                                        .distinctBy { it.sourceUrl }
                                Toast.makeText(this@MainActivity, "Added to queue", Toast.LENGTH_SHORT).show()
                                showSongMenu = false
                            },
                            onArtist = {
                                val artist = currentSong!!.artist
                                showSongMenu = false
                                playerExpanded = false
                                runSearch(artist, SearchKind.ARTIST)
                            },
                            onAlbum = {
                                val song = currentSong!!
                                showSongMenu = false
                                playerExpanded = false
                                runSearch(song.title + " " + song.artist, SearchKind.ALBUM)
                            },
                            onRadio = {
                                val song = currentSong!!
                                showSongMenu = false
                                startRecommendationRadio(song)
                            },
                            onLyrics = {
                                playerPane = PlayerPane.LYRICS
                                showSongMenu = false
                            },
                            onSleep = { minutes ->
                                setSleepTimer(minutes)
                                showSongMenu = false
                            },
                            onPlayback = { speed, pitch ->
                                setPlayback(speed, pitch)
                            },
                            onShare = {
                                shareSong(currentSong!!)
                                showSongMenu = false
                            }
                        )
                    }
                }

                if (showAccountSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showAccountSheet = false },
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        AccountSheet(
                            profileName = profileName,
                            profileAvatar = profileAvatar,
                            likedCount = youtubeLikedSongs.size,
                            onOpenLiked = {
                                showAccountSheet = false
                                selectedTab = MainTab.LIBRARY
                            },
                            onLogout = {
                                AccountSession.clear(this@MainActivity)
                                youtubeLoggedIn = false
                                profileName = ""
                                profileAvatar = ""
                                youtubeLikedSongs = emptyList()
                                showAccountSheet = false
                                Toast.makeText(this@MainActivity, "Signed out", Toast.LENGTH_SHORT).show()
                            }
                        )
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
    youtubeLoggedIn: Boolean,
    profileName: String,
    profileAvatar: String,
    onAccount: () -> Unit,
    onSearch: () -> Unit,
    onHistory: () -> Unit,
    onNotifications: () -> Unit,
    onSettings: () -> Unit
) {
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Good Night"
        }
    }
    val moods = listOf(
        "All", "Relax", "Sleep", "Energize", "Sad",
        "Romance", "Feel good", "Workout", "Party", "Commute", "Focus"
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Column(
                Modifier
                    .statusBarsPadding()
                    .padding(top = 8.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_dhunora_launcher),
                            contentDescription = "Dhunora",
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Dhunora",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.2).sp
                            )
                            Text(
                                greeting,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.5.sp
                            )
                        }
                    }

                    IconButton(onClick = onNotifications) {
                        Icon(Icons.Rounded.Notifications, "Notifications")
                    }
                    IconButton(onClick = onHistory) {
                        Icon(Icons.Rounded.History, "History")
                    }
                    IconButton(onClick = onAccount) {
                        Icon(Icons.Rounded.Groups, "Account")
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Rounded.Settings, "Settings")
                    }
                }

                Spacer(Modifier.height(16.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(moods) { mood ->
                        FilterChip(
                            selected = selectedMood == mood,
                            onClick = { onMood(mood) },
                            label = {
                                Text(
                                    mood,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            },
                            shape = RoundedCornerShape(22.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = Color.Transparent
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (selectedMood == mood)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                            )
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))

                if (youtubeLoggedIn) {
                    Column(
                        Modifier.padding(horizontal = 20.dp)
                    ) {
                        Text(
                            "Welcome back,",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            Modifier.clickable(onClick = onAccount),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                Modifier.size(50.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (profileAvatar.isNotBlank()) {
                                        AsyncImage(
                                            model = profileAvatar,
                                            contentDescription = profileName,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            Icons.Rounded.Person,
                                            null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Text(
                                profileName.ifBlank { "YouTube Music" },
                                fontSize = 19.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.height(22.dp))
                    }
                }

                Text(
                    "LET'S START WITH A RADIO",
                    Modifier.padding(horizontal = 20.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp
                )
                Text(
                    "Quick picks",
                    Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (loading && quickPicks.isEmpty()) {
            items(4) {
                SearchResultShimmer(SearchKind.SONG)
            }
        } else {
            items(
                items = quickPicks.take(5),
                key = { "homequick:" + it.sourceUrl }
            ) { song ->
                HomeQuickPickRow(
                    song = song,
                    favorite = favoriteCheck(song),
                    onPlay = { onPlay(song) },
                    onFavorite = { onFavorite(song) }
                )
            }
        }

        if (madeForYou.isNotEmpty()) {
            item {
                Spacer(Modifier.height(22.dp))
                Text(
                    "Forgotten favorites",
                    Modifier.padding(horizontal = 20.dp),
                    fontSize = 21.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(14.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(
                        items = madeForYou,
                        key = { "forgotten:" + it.sourceUrl }
                    ) { song ->
                        LargeHomeCard(
                            song = song,
                            onClick = { onPlay(song) }
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "More for you",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "Explore",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable(onClick = onSearch)
                        .padding(8.dp)
                )
            }
        }

        items(
            items = trending.take(10),
            key = { "more:" + it.sourceUrl }
        ) { song ->
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
private fun HomeQuickPickRow(
    song: Song,
    favorite: Boolean,
    onPlay: () -> Unit,
    onFavorite: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay)
            .padding(horizontal = 22.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Artwork(song, Modifier.size(52.dp), 10.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                song.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                song.artist,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onFavorite) {
            Icon(
                if (favorite) Icons.Rounded.Favorite else Icons.Rounded.MoreVert,
                contentDescription = null,
                tint =
                    if (favorite) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LargeHomeCard(
    song: Song,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .width(226.dp)
            .clickable(onClick = onClick)
    ) {
        Box {
            Artwork(
                song,
                Modifier
                    .fillMaxWidth()
                    .height(146.dp),
                16.dp
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = Color.Black.copy(alpha = 0.55f)
            ) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
                    Text(
                        song.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        song.artist,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun MixScreen(
    modifier: Modifier,
    songs: List<Song>,
    onPlay: (Song) -> Unit,
    onFavorite: (Song) -> Unit,
    favoriteCheck: (Song) -> Boolean
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(
                    "Mix",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "An endless radio shaped by what you listen to",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(20.dp))
            }
        }

        items(
            items = songs.take(30),
            key = { "mix:" + it.sourceUrl }
        ) { song ->
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
    results: List<MusicSearchItem>,
    liveResults: List<MusicSearchItem>,
    committed: Boolean,
    selectedKind: SearchKind,
    loading: Boolean,
    error: String?,
    onText: (String) -> Unit,
    onSubmit: (String, SearchKind) -> Unit,
    onSuggestion: (String) -> Unit,
    onKindChange: (SearchKind) -> Unit,
    onOpen: (MusicSearchItem) -> Unit,
    onFavorite: (Song) -> Unit,
    favoriteCheck: (Song) -> Boolean,
    accountConnected: Boolean,
    onAccount: () -> Unit
) {
    val focus = LocalFocusManager.current
    val browse = listOf(
        "Bollywood & Indian",
        "Punjabi",
        "Chill",
        "Workout",
        "Focus",
        "Romance",
        "Party",
        "Sleep"
    )

    LazyColumn(
        modifier = modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            Spacer(Modifier.height(10.dp))

            TextField(
                value = text,
                onValueChange = onText,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(58.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                placeholder = {
                    Text(
                        when (selectedKind) {
                            SearchKind.ALL -> "Search songs, artists, albums..."
                            SearchKind.SONG -> "Search songs..."
                            SearchKind.VIDEO -> "Search videos..."
                            SearchKind.ALBUM -> "Search albums..."
                            SearchKind.ARTIST -> "Search artists..."
                            SearchKind.PLAYLIST -> "Search playlists..."
                        }
                    )
                },
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                trailingIcon = {
                    if (text.isNotEmpty()) {
                        IconButton(onClick = { onText("") }) {
                            Icon(Icons.Rounded.Clear, null)
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focus.clearFocus()
                    onSubmit(text, selectedKind)
                }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            if (committed) {
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SearchKind.entries.forEach { kind ->
                        FilterChip(
                            selected = selectedKind == kind,
                            onClick = { onKindChange(kind) },
                            label = {
                                Text(
                                    kind.label,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = Color.Transparent
                            ),
                            border =
                                BorderStroke(
                                    1.dp,
                                    if (selectedKind == kind)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                )
                        )
                    }
                }
            }
        }

        if (committed && loading && results.isEmpty()) {
            items(4) {
                SearchResultShimmer(selectedKind)
            }
        }

        error
            ?.takeUnless {
                it.contains("cancel", ignoreCase = true) ||
                    it.contains("StandaloneCoroutine", ignoreCase = true)
            }
            ?.let {
                item {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                    )
                }
            }

        if (!committed && text.isNotBlank()) {
            if (liveResults.isNotEmpty()) {
                items(
                    items = liveResults.take(4),
                    key = { "live:" + it.kind.name + ":" + it.sourceUrl }
                ) { item ->
                    SearchResultRow(
                        item = item,
                        favorite =
                            item.toSongOrNull()?.let(favoriteCheck) ?: false,
                        onClick = { onOpen(item) },
                        onFavorite = {
                            item.toSongOrNull()?.let(onFavorite)
                        }
                    )
                }

                val suggestions =
                    buildList {
                        add(text.trim())
                        liveResults.forEach { item ->
                            add(item.title.trim())
                            if (
                                item.kind == SearchKind.ARTIST &&
                                item.title.isNotBlank()
                            ) {
                                add(item.title.trim() + " songs")
                            }
                        }
                    }
                        .filter { it.length >= 2 }
                        .distinctBy { it.lowercase() }
                        .take(5)

                items(
                    items = suggestions,
                    key = { "live-suggestion:" + it.lowercase() }
                ) { suggestion ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSuggestion(suggestion) }
                            .padding(horizontal = 26.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            suggestion.lowercase(),
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Icon(
                            Icons.Rounded.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier
                                .size(22.dp)
                                .graphicsLayer { rotationZ = 135f },
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        if (!committed && text.isBlank()) {
            item {
                Text(
                    "Everything you need",
                    Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                BrowseGrid(
                    browse = browse,
                    onClick = { onSubmit(it, SearchKind.ALL) }
                )
            }
        } else if (committed && !loading && results.isEmpty() && text.isNotBlank()) {
            item {
                Column(
                    Modifier.fillMaxWidth().padding(top = 70.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Rounded.Search,
                        null,
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No " + selectedKind.label.lowercase() + " found",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        "Try another search",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (committed && results.isNotEmpty()) {
            items(
                items = results,
                key = { "all:" + it.kind.name + ":" + it.sourceUrl }
            ) { item ->
                SearchResultRow(
                    item = item,
                    favorite = item.toSongOrNull()?.let(favoriteCheck) ?: false,
                    onClick = { onOpen(item) },
                    onFavorite = { item.toSongOrNull()?.let(onFavorite) }
                )
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    item: MusicSearchItem,
    favorite: Boolean,
    onClick: () -> Unit,
    onFavorite: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchArtwork(
            item = item,
            modifier = Modifier.size(52.dp)
        )

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                item.title,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(3.dp))

            val meta =
                when (item.kind) {
                    SearchKind.ALL ->
                        item.subtitle

                    SearchKind.SONG ->
                        item.subtitle +
                            if (item.durationSeconds > 0) " • " + formatDuration(item.durationSeconds) else ""

                    SearchKind.VIDEO ->
                        "Video • " + item.subtitle +
                            if (item.durationSeconds > 0) " • " + formatDuration(item.durationSeconds) else ""

                    SearchKind.ALBUM ->
                        "Album" +
                            if (item.subtitle.isNotBlank()) " • " + item.subtitle else ""

                    SearchKind.PLAYLIST ->
                        "Playlist" +
                            if (item.subtitle.isNotBlank()) " • " + item.subtitle else "" +
                            if (item.itemCount > 0) " • " + item.itemCount + " songs" else ""

                    SearchKind.ARTIST ->
                        item.subtitle
                }

            Text(
                meta,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (
            item.kind == SearchKind.SONG ||
            item.kind == SearchKind.VIDEO
        ) {
            IconButton(onClick = onFavorite) {
                Icon(
                    if (favorite) Icons.Rounded.Favorite else Icons.Rounded.MoreVert,
                    contentDescription = null,
                    tint =
                        if (favorite) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SearchArtwork(
    item: MusicSearchItem,
    modifier: Modifier
) {
    val shape =
        if (item.kind == SearchKind.ARTIST) CircleShape
        else RoundedCornerShape(6.dp)

    Surface(
        modifier = modifier,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        if (!item.thumbnailUrl.isNullOrBlank()) {
            AsyncImage(
                model = MusicRepository.highQualityThumbnail(item.thumbnailUrl),
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (item.kind == SearchKind.ARTIST) Icons.Rounded.Person
                    else Icons.Rounded.MusicNote,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchResultShimmer(kind: SearchKind) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            Modifier.size(52.dp),
            shape = if (kind == SearchKind.ARTIST) CircleShape else RoundedCornerShape(5.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {}
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Surface(
                Modifier.fillMaxWidth(0.72f).height(16.dp),
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {}
            Spacer(Modifier.height(8.dp))
            Surface(
                Modifier.fillMaxWidth(0.42f).height(11.dp),
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
            ) {}
        }
    }
}

@Composable
private fun CollectionDetailScreen(
    item: MusicSearchItem,
    songs: List<Song>,
    loading: Boolean,
    currentSong: Song?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onPlayAll: () -> Unit,
    onPlaySong: (Song) -> Unit,
    onFavorite: (Song) -> Unit,
    favoriteCheck: (Song) -> Boolean
) {
    CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colorScheme.onBackground
    ) {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, "Back", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {}) {
                    Icon(Icons.Rounded.MoreVert, null, tint = Color.White)
                }
            }

            Column(
                Modifier.fillMaxWidth().padding(horizontal = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SearchArtwork(
                    item = item,
                    modifier =
                        if (item.kind == SearchKind.ARTIST) {
                            Modifier.size(190.dp)
                        } else {
                            Modifier.size(220.dp)
                        }
                )

                Spacer(Modifier.height(18.dp))

                Text(
                    item.title,
                    color = Color.White,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    when (item.kind) {
                        SearchKind.ARTIST -> item.subtitle
                        SearchKind.ALBUM -> "Album • " + item.subtitle
                        SearchKind.PLAYLIST -> "Playlist • " + item.subtitle
                        SearchKind.SONG -> item.subtitle
                    },
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(18.dp))

                FilledIconButton(
                    onClick = onPlayAll,
                    enabled = songs.isNotEmpty(),
                    modifier = Modifier.size(62.dp)
                ) {
                    Icon(
                        Icons.Rounded.PlayArrow,
                        contentDescription = "Play all",
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            HorizontalDivider()
        }

        if (loading) {
            item {
                Box(
                    Modifier.fillMaxWidth().height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (songs.isEmpty()) {
            item {
                Column(
                    Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No tracks available",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        "This item may not expose tracks through the current backend.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            item {
                Text(
                    if (item.kind == SearchKind.ARTIST) "Popular" else "Tracks",
                    Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    color = Color.White,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(
                items = songs,
                key = { it.sourceUrl }
            ) { song ->
                SongListRow(
                    song = song,
                    favorite = favoriteCheck(song),
                    onPlay = { onPlaySong(song) },
                    onFavorite = { onFavorite(song) }
                )
            }
        }
    }

    }
}

@Composable
private fun LibraryScreen(
    modifier: Modifier,
    favorites: List<Song>,
    recent: List<Song>,
    youtubeLiked: List<Song>,
    youtubeLoggedIn: Boolean,
    youtubeLoading: Boolean,
    myPlaylist: List<Song>,
    downloads: List<DownloadRecord>,
    onPlay: (Song) -> Unit,
    onFavorite: (Song) -> Unit,
    onSearch: () -> Unit,
    onAccount: () -> Unit,
    onOpenCollection: (
        title: String,
        subtitle: String,
        songs: List<Song>,
        downloads: List<DownloadRecord>
    ) -> Unit
) {
    var filter by rememberSaveable { mutableStateOf("Your library") }
    val filters =
        buildList {
            add("Your library")
            add("Dhunora Charts")
            if (youtubeLoggedIn) add("Your YouTube Music")
        }

    val completedDownloads = downloads.filter { it.isComplete }.map { it.song }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(46.dp)
                        .clickable(onClick = onAccount),
                    shape = CircleShape,
                    color =
                        if (youtubeLoggedIn)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                    border =
                        if (youtubeLoggedIn)
                            BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                        else null
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.Person,
                            "Account",
                            tint =
                                if (youtubeLoggedIn)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                Text(
                    "Library",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onAccount) {
                    Icon(Icons.Rounded.Groups, "Account and friends")
                }

                IconButton(onClick = onSearch) {
                    Icon(Icons.Rounded.Search, "Search")
                }
            }

            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                filters.forEach { item ->
                    FilterChip(
                        selected = filter == item,
                        onClick = { filter = item },
                        label = {
                            Text(
                                item,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = Color.Transparent
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (filter == item)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                        )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            when (filter) {
                "Your YouTube Music" -> {
                    LibraryWideTile(
                        title = "YouTube Liked Music",
                        subtitle =
                            if (youtubeLoading) "Syncing your account..."
                            else youtubeLiked.size.toString() + " songs",
                        icon = Icons.Rounded.Favorite,
                        container = Color(0xFF9B5DE5),
                        onClick = {
                            onOpenCollection(
                                "YouTube Liked Music",
                                youtubeLiked.size.toString() + " songs",
                                youtubeLiked,
                                emptyList()
                            )
                        }
                    )
                }

                "Dhunora Charts" -> {
                    LibraryWideTile(
                        title = "Your Mix",
                        subtitle = "Based on your recent listening",
                        icon = Icons.Rounded.TrendingUp,
                        container = Color(0xFF1FA2FF),
                        onClick = {
                            onOpenCollection(
                                "Your Mix",
                                "Personalized from your listening",
                                recent.distinctBy { it.sourceUrl },
                                emptyList()
                            )
                        }
                    )
                }

                else -> {
                    LibraryTileGrid(
                        favorites = favorites,
                        recent = recent,
                        myPlaylist = myPlaylist,
                        downloads = downloads,
                        onOpenCollection = onOpenCollection
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                if (filter == "Your YouTube Music") "Your YouTube Music"
                else "Recently Added",
                Modifier.padding(horizontal = 20.dp),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(10.dp))
        }

        val list =
            when (filter) {
                "Your YouTube Music" -> youtubeLiked
                "Dhunora Charts" ->
                    (recent + favorites)
                        .distinctBy { it.sourceUrl }
                else ->
                    (youtubeLiked.take(1) + recent + favorites + completedDownloads)
                        .distinctBy { it.sourceUrl }
            }

        if (list.isEmpty()) {
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 38.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Rounded.LibraryMusic,
                        null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Your library is getting ready",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Play, like or download music and it will appear here.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(
                items = list.take(30),
                key = { "libraryrecent:" + it.sourceUrl }
            ) { song ->
                SongListRow(
                    song = song,
                    favorite = favorites.any { it.sourceUrl == song.sourceUrl },
                    onPlay = { onPlay(song) },
                    onFavorite = { onFavorite(song) }
                )
            }
        }
    }
}

@Composable
private fun LibraryTileGrid(
    favorites: List<Song>,
    recent: List<Song>,
    myPlaylist: List<Song>,
    downloads: List<DownloadRecord>,
    onOpenCollection: (
        title: String,
        subtitle: String,
        songs: List<Song>,
        downloads: List<DownloadRecord>
    ) -> Unit
) {
    val completed = downloads.filter { it.isComplete }.map { it.song }

    Column(
        Modifier.padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LibraryColorTile(
                modifier = Modifier.weight(1f),
                title = "Favorite",
                icon = Icons.Rounded.Favorite,
                container = Color(0xFFFF8FB3),
                onClick = {
                    onOpenCollection(
                        "Favorite",
                        favorites.size.toString() + " liked songs",
                        favorites,
                        emptyList()
                    )
                }
            )
            LibraryColorTile(
                modifier = Modifier.weight(1f),
                title = "My Playlist",
                icon = Icons.Rounded.PlaylistAdd,
                container = Color(0xFFFFE300),
                onClick = {
                    onOpenCollection(
                        "My Playlist",
                        myPlaylist.size.toString() + " songs",
                        myPlaylist,
                        emptyList()
                    )
                }
            )
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LibraryColorTile(
                modifier = Modifier.weight(1f),
                title = "Most Played",
                icon = Icons.Rounded.TrendingUp,
                container = Color(0xFF1BBED1),
                onClick = {
                    onOpenCollection(
                        "Most Played",
                        recent.size.toString() + " recent tracks",
                        recent,
                        emptyList()
                    )
                }
            )
            LibraryColorTile(
                modifier = Modifier.weight(1f),
                title = "Downloaded",
                icon = Icons.Rounded.Download,
                container = Color(0xFF13C45B),
                onClick = {
                    onOpenCollection(
                        "Downloaded",
                        completed.size.toString() + " ready",
                        completed,
                        downloads
                    )
                }
            )
        }
    }
}

@Composable
private fun LibraryColorTile(
    modifier: Modifier,
    title: String,
    icon: ImageVector,
    container: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(94.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = container
    ) {
        Row(
            Modifier.padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                null,
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(14.dp))
            Text(
                title,
                color = Color.Black,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun LibraryWideTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    container: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = container.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, container.copy(alpha = 0.55f))
    ) {
        Row(
            Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                Modifier.size(58.dp),
                shape = RoundedCornerShape(16.dp),
                color = container.copy(alpha = 0.35f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = container)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
            Icon(Icons.Rounded.ChevronRight, null)
        }
    }
}

@Composable
private fun LibraryCollectionScreen(
    collection: LibraryCollection,
    favorites: List<Song>,
    onBack: () -> Unit,
    onPlay: (Song) -> Unit,
    onFavorite: (Song) -> Unit
) {
    CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colorScheme.onBackground
    ) {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Rounded.ArrowBack,
                        "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        collection.title,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        collection.subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (collection.songs.isNotEmpty()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        collection.songs.size.toString() + " tracks",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    FilledIconButton(
                        onClick = { collection.songs.firstOrNull()?.let(onPlay) },
                        modifier = Modifier.size(54.dp)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, "Play all")
                    }
                }
            }

            HorizontalDivider()
        }

        if (collection.downloads.isNotEmpty()) {
            items(
                items = collection.downloads,
                key = { "collectiondownload:" + it.id }
            ) { record ->
                DownloadRow(
                    record = record,
                    onPlay = {
                        if (record.isComplete) onPlay(record.song)
                    }
                )
            }
        } else if (collection.songs.isEmpty()) {
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Rounded.MusicNote,
                        null,
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Nothing here yet",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            items(
                items = collection.songs,
                key = { "collection:" + it.sourceUrl }
            ) { song ->
                SongListRow(
                    song = song,
                    favorite = favorites.any { it.sourceUrl == song.sourceUrl },
                    onPlay = { onPlay(song) },
                    onFavorite = { onFavorite(song) }
                )
            }
        }
    }

    }
}

@Composable
private fun DownloadRow(
    record: DownloadRecord,
    onPlay: () -> Unit
) {
    val statusText =
        when (record.status) {
            DownloadManager.STATUS_SUCCESSFUL -> "Downloaded"
            DownloadManager.STATUS_RUNNING -> "Downloading • " + record.progress + "%"
            DownloadManager.STATUS_PAUSED -> "Paused • " + record.progress + "%"
            DownloadManager.STATUS_FAILED -> "Download failed"
            else -> "Waiting to download"
        }

    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = record.isComplete, onClick = onPlay)
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Artwork(record.song, Modifier.size(58.dp), 10.dp)
        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                record.song.title,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                record.song.artist + " • " + statusText,
                fontSize = 12.sp,
                color =
                    if (record.status == DownloadManager.STATUS_FAILED)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!record.isComplete && record.status != DownloadManager.STATUS_FAILED) {
                Spacer(Modifier.height(5.dp))
                LinearProgressIndicator(
                    progress = { record.progress / 100f },
                    modifier = Modifier.fillMaxWidth().height(2.dp)
                )
            }
        }

        Icon(
            if (record.isComplete) Icons.Rounded.PlayArrow else Icons.Rounded.Download,
            contentDescription = null,
            tint =
                if (record.isComplete) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
        )
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
    onNext: () -> Unit,
    glassEnabled: Boolean
) {
    val glass =
        if (glassEnabled) Color(0xC71B1B20)
        else MaterialTheme.colorScheme.surfaceVariant

    Column(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.background.copy(alpha = 0.86f)
                    )
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        if (currentSong != null) {
            Surface(
                onClick = onOpenPlayer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                shape = RoundedCornerShape(18.dp),
                color =
                    if (glassEnabled)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                border =
                    if (glassEnabled)
                        BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                    else null,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Column {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Artwork(currentSong, Modifier.size(52.dp), 9.dp)
                        Spacer(Modifier.width(10.dp))

                        Column(Modifier.weight(1f)) {
                            Text(
                                currentSong.title,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                currentSong.artist,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(onClick = onTogglePlay) {
                            if (buffering) {
                                CircularProgressIndicator(
                                    Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    if (isPlaying)
                                        Icons.Rounded.Pause
                                    else
                                        Icons.Rounded.PlayArrow,
                                    null
                                )
                            }
                        }

                        IconButton(onClick = onNext) {
                            Icon(Icons.Rounded.SkipNext, "Next")
                        }
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp),
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
                color = glass,
                border =
                    if (glassEnabled)
                        BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
                    else null,
                tonalElevation = if (glassEnabled) 0.dp else 6.dp
            ) {
                Row(
                    Modifier
                        .height(66.dp)
                        .padding(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomTab(
                        icon = Icons.Rounded.Home,
                        label = "Home",
                        selected = selectedTab == MainTab.HOME,
                        onClick = { onSelect(MainTab.HOME) }
                    )
                    BottomTab(
                        icon = Icons.Rounded.Radio,
                        label = "Mix",
                        selected = selectedTab == MainTab.MIX,
                        onClick = { onSelect(MainTab.MIX) }
                    )
                    BottomTab(
                        icon = Icons.Rounded.LibraryMusic,
                        label = "Library",
                        selected = selectedTab == MainTab.LIBRARY,
                        onClick = { onSelect(MainTab.LIBRARY) }
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            Surface(
                onClick = { onSelect(MainTab.SEARCH) },
                modifier = Modifier.size(58.dp),
                shape = CircleShape,
                color =
                    if (selectedTab == MainTab.SEARCH)
                        MaterialTheme.colorScheme.primary
                    else glass,
                border =
                    if (glassEnabled && selectedTab != MainTab.SEARCH)
                        BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
                    else null,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.Search,
                        null,
                        tint =
                            if (selectedTab == MainTab.SEARCH)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
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
        onClick = onClick,
        modifier = Modifier
            .width(84.dp)
            .fillMaxHeight(),
        shape = RoundedCornerShape(28.dp),
        color =
            if (selected)
                MaterialTheme.colorScheme.background.copy(alpha = 0.72f)
            else
                Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
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
    onMore: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPane: (PlayerPane) -> Unit,
    onPlaySong: (Song) -> Unit,
    onAdd: () -> Unit,
    onRadio: () -> Unit,
    style: String,
    liquidGlass: Boolean,
    romanizedLyrics: Boolean,
    lyricsLines: List<String>,
    lyricsSource: String?,
    lyricsLoading: Boolean,
    canvasUrl: String?
) {
    val fraction =
        (positionMs.toFloat() / durationMs.toFloat())
            .coerceIn(0f, 1f)
    var dragging by remember { mutableFloatStateOf(fraction) }
    LaunchedEffect(fraction) { dragging = fraction }

    var artworkTone by remember(song.sourceUrl) {
        mutableStateOf(Color(0xFF8F132B))
    }

    LaunchedEffect(song.sourceUrl, song.thumbnailUrl) {
        val url = MusicRepository.artworkFor(song)
        if (!url.isNullOrBlank()) {
            val extracted =
                withContext(Dispatchers.IO) {
                    runCatching {
                        URL(url).openStream().use { stream ->
                            BitmapFactory.decodeStream(stream)?.let { bitmap ->
                                Palette.from(bitmap).generate().let { palette ->
                                    palette.darkMutedSwatch?.rgb
                                        ?: palette.mutedSwatch?.rgb
                                        ?: palette.darkVibrantSwatch?.rgb
                                        ?: palette.dominantSwatch?.rgb
                                }
                            }
                        }
                    }.getOrNull()
                }

            if (extracted != null) {
                artworkTone = Color(extracted)
            }
        }
    }

    val topColor =
        if (style == "Immersive") artworkTone
        else lerp(artworkTone, Color.Black, 0.16f)
    val midColor =
        lerp(
            artworkTone,
            Color.Black,
            if (liquidGlass) 0.62f else 0.72f
        )

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            topColor,
                            midColor,
                            Color(0xFF09090B),
                            Color(0xFF09090B)
                        ),
                        startY = 0f,
                        endY = 1750f
                    )
                )
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 36.dp)
        ) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onCollapse) {
                        Icon(Icons.Rounded.KeyboardArrowDown, "Collapse")
                    }

                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "NOW PLAYING",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            song.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = onMore) {
                        Icon(Icons.Rounded.MoreVert, "More")
                    }
                }

                Spacer(Modifier.height(34.dp))

                if (!canvasUrl.isNullOrBlank()) {
                    SpotifyCanvasPreview(
                        canvasUrl = canvasUrl,
                        fallbackSong = song,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 42.dp)
                                .aspectRatio(9f / 14f)
                    )
                } else {
                    Artwork(
                        song,
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 40.dp)
                            .aspectRatio(1f),
                        12.dp
                    )
                }

                Spacer(Modifier.height(38.dp))

                Column(Modifier.padding(horizontal = 24.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                song.title,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 24.sp
                            )
                            Spacer(Modifier.height(5.dp))
                            Text(
                                song.artist,
                                fontSize = 13.5.sp,
                                color = Color.White.copy(alpha = 0.68f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(onClick = onAdd) {
                            Surface(
                                Modifier.size(36.dp),
                                shape = CircleShape,
                                color = Color.Transparent,
                                border = BorderStroke(
                                    2.dp,
                                    Color.White.copy(alpha = 0.88f)
                                )
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Rounded.Add,
                                        "Add",
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        IconButton(onClick = onFavorite) {
                            Icon(
                                if (favorite)
                                    Icons.Rounded.Favorite
                                else
                                    Icons.Rounded.FavoriteBorder,
                                null,
                                modifier = Modifier.size(28.dp),
                                tint =
                                    if (favorite)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Slider(
                        value = dragging,
                        onValueChange = { dragging = it },
                        onValueChangeFinished = { onSeek(dragging) },
                        valueRange = 0f..1f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(22.dp)
                            .graphicsLayer { scaleY = 0.48f }
                    )

                    Row(Modifier.fillMaxWidth()) {
                        Text(
                            formatTime(positionMs),
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            formatTime(durationMs),
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    }

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 22.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Rounded.Shuffle,
                                null,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        IconButton(onClick = onPrevious) {
                            Icon(
                                Icons.Rounded.SkipPrevious,
                                "Previous",
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        FilledIconButton(
                            onClick = onTogglePlay,
                            modifier = Modifier.size(76.dp),
                            colors =
                                androidx.compose.material3.IconButtonDefaults
                                    .filledIconButtonColors(
                                        containerColor = Color.White,
                                        contentColor = Color.Black
                                    )
                        ) {
                            if (buffering) {
                                CircularProgressIndicator(
                                    Modifier.size(30.dp),
                                    strokeWidth = 3.dp,
                                    color = Color.Black
                                )
                            } else {
                                Icon(
                                    if (isPlaying)
                                        Icons.Rounded.Pause
                                    else
                                        Icons.Rounded.PlayArrow,
                                    null,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        IconButton(onClick = onNext) {
                            Icon(
                                Icons.Rounded.SkipNext,
                                "Next",
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Rounded.Repeat,
                                null,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PlayerActionIcon(
                            icon = Icons.Rounded.Info,
                            label = "Info",
                            onClick = onMore
                        )
                        PlayerActionIcon(
                            icon = Icons.Rounded.Radio,
                            label = "Radio",
                            onClick = onRadio
                        )
                        PlayerActionIcon(
                            icon = Icons.Rounded.PlaylistAdd,
                            label = "Queue",
                            onClick = { onPane(PlayerPane.UP_NEXT) }
                        )
                        PlayerActionIcon(
                            icon = Icons.Rounded.QueueMusic,
                            label = "Lyrics",
                            onClick = { onPane(PlayerPane.LYRICS) }
                        )
                    }
                }

                Spacer(Modifier.height(26.dp))

                if (pane == PlayerPane.LYRICS) {
                    LyricsPreviewCard(
                        romanized = romanizedLyrics,
                        lines = lyricsLines,
                        source = lyricsSource,
                        loading = lyricsLoading,
                        onShowQueue = { onPane(PlayerPane.UP_NEXT) }
                    )
                } else {
                    UpNextPreview(
                        current = song,
                        queue = queue,
                        onPlaySong = onPlaySong,
                        onShowLyrics = { onPane(PlayerPane.LYRICS) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SpotifyCanvasPreview(
    canvasUrl: String,
    fallbackSong: Song,
    modifier: Modifier = Modifier
) {
    var failed by remember(canvasUrl) {
        mutableStateOf(false)
    }
    var videoView by remember {
        mutableStateOf<VideoView?>(null)
    }

    DisposableEffect(canvasUrl) {
        onDispose {
            videoView?.stopPlayback()
            videoView = null
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Artwork(
            fallbackSong,
            Modifier.fillMaxSize(),
            16.dp
        )

        if (!failed) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    VideoView(context).apply {
                        videoView = this
                        setOnPreparedListener { mediaPlayer ->
                            mediaPlayer.isLooping = true
                            mediaPlayer.setVolume(0f, 0f)
                            start()
                        }
                        setOnErrorListener { _, _, _ ->
                            failed = true
                            true
                        }
                        setVideoURI(Uri.parse(canvasUrl))
                    }
                },
                update = { view ->
                    if (view.tag != canvasUrl) {
                        view.tag = canvasUrl
                        view.setVideoURI(Uri.parse(canvasUrl))
                        view.start()
                    }
                }
            )
        }

        Surface(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color.Black.copy(alpha = 0.55f)
        ) {
            Text(
                "Spotify Canvas",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PlayerActionIcon(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size(29.dp),
            tint = Color.White
        )
    }
}

@Composable
private fun LyricsPreviewCard(
    romanized: Boolean,
    lines: List<String>,
    source: String?,
    loading: Boolean,
    onShowQueue: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFCB1D42)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(22.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Lyrics",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "Queue",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable(onClick = onShowQueue)
                        .padding(8.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(26.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Text(
                    when {
                        lines.isNotEmpty() -> lines.take(6).joinToString("\n\n")
                        romanized -> "Lyrics will appear here when a matching source is available."
                        else -> "Lyrics are not available for this track yet."
                    },
                    color = Color.White.copy(
                        alpha = if (lines.isNotEmpty()) 0.72f else 0.56f
                    ),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 24.sp,
                    maxLines = 12,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(34.dp))

            Text(
                source?.let { "Lyrics provided by $it" } ?: "Lyrics",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun UpNextPreview(
    current: Song,
    queue: List<Song>,
    onPlaySong: (Song) -> Unit,
    onShowLyrics: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.06f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(vertical = 12.dp)) {
            Row(
                Modifier.padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Up next",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "Lyrics",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable(onClick = onShowLyrics)
                        .padding(8.dp)
                )
            }

            queue
                .filterNot { it.sourceUrl == current.sourceUrl }
                .take(4)
                .forEach { next ->
                    SongListRow(
                        song = next,
                        favorite = false,
                        onPlay = { onPlaySong(next) },
                        onFavorite = {}
                    )
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
            Text(
                song.title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
            Icon(
                if (favorite) Icons.Rounded.Favorite else Icons.Rounded.MoreVert,
                null,
                tint =
                    if (favorite) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
            )
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
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
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
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.align(Alignment.TopStart))
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
private fun SongActionSheet(
    song: Song,
    favorite: Boolean,
    onLike: () -> Unit,
    onDownload: () -> Unit,
    onAddPlaylist: () -> Unit,
    onPlayNext: () -> Unit,
    onAddQueue: () -> Unit,
    onArtist: () -> Unit,
    onAlbum: () -> Unit,
    onRadio: () -> Unit,
    onLyrics: () -> Unit,
    onSleep: (Int) -> Unit,
    onPlayback: (Float, Float) -> Unit,
    onShare: () -> Unit
) {
    var page by rememberSaveable { mutableStateOf("root") }
    var speed by remember { mutableFloatStateOf(1f) }
    var pitch by remember { mutableFloatStateOf(1f) }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 26.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Artwork(song, Modifier.size(80.dp), 6.dp)
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    song.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    song.artist,
                    fontSize = 14.sp,
                    color = Color(0xFFB8B8B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        HorizontalDivider(
            Modifier.padding(horizontal = 28.dp, vertical = 4.dp),
            color = Color(0xFF4A4A4A)
        )

        when (page) {
            "sleep" -> {
                SheetBackHeader("Sleep Timer") { page = "root" }
                listOf(15, 30, 45, 60).forEach { minutes ->
                    ActionRow(
                        icon = Icons.Rounded.Timer,
                        title = minutes.toString() + " minutes",
                        onClick = { onSleep(minutes) }
                    )
                }
            }

            "speed" -> {
                SheetBackHeader("Playback speed & pitch") { page = "root" }

                Column(Modifier.padding(horizontal = 28.dp, vertical = 8.dp)) {
                    Text(
                        "Speed  " + String.format("%.2fx", speed),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = speed,
                        onValueChange = {
                            speed = it
                            onPlayback(speed, pitch)
                        },
                        valueRange = 0.5f..2f,
                        steps = 5
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Pitch  " + String.format("%.2fx", pitch),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = pitch,
                        onValueChange = {
                            pitch = it
                            onPlayback(speed, pitch)
                        },
                        valueRange = 0.5f..1.5f,
                        steps = 3
                    )
                }
            }

            else -> {
                ActionRow(
                    icon = if (favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    title = if (favorite) "Liked" else "Like",
                    onClick = onLike
                )
                ActionRow(Icons.Rounded.Download, "Download", onClick = onDownload)
                ActionRow(Icons.Rounded.PlaylistAdd, "Add to a playlist", onClick = onAddPlaylist)
                ActionRow(Icons.Rounded.PlayCircle, "Play next", onClick = onPlayNext)
                ActionRow(Icons.Rounded.QueueMusic, "Add to queue", onClick = onAddQueue)
                ActionRow(
                    Icons.Rounded.Person,
                    "Artists",
                    subtitle = song.artist,
                    onClick = onArtist
                )
                ActionRow(
                    Icons.Rounded.Album,
                    "Album",
                    subtitle = song.title,
                    onClick = onAlbum
                )
                ActionRow(Icons.Rounded.Radio, "Start radio", onClick = onRadio)
                ActionRow(Icons.Rounded.MusicNote, "Main Lyrics Provider", onClick = onLyrics)
                ActionRow(Icons.Rounded.Timer, "Sleep Timer", onClick = { page = "sleep" })
                ActionRow(
                    Icons.Rounded.Speed,
                    "Playback speed & pitch",
                    onClick = { page = "speed" }
                )
                ActionRow(Icons.Rounded.Share, "Share", onClick = onShare)
            }
        }
    }
}

@Composable
private fun SheetBackHeader(
    title: String,
    onBack: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onBack)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.ArrowBack,
            contentDescription = "Back",
            tint = Color.White
        )
        Spacer(Modifier.width(14.dp))
        Text(
            title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 30.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.width(24.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    subtitle,
                    color = Color(0xFFAAAAAA),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AccountSheet(
    profileName: String,
    profileAvatar: String,
    likedCount: Int,
    onOpenLiked: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 34.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                Modifier.size(54.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (profileAvatar.isNotBlank()) {
                        AsyncImage(
                            model = profileAvatar,
                            contentDescription = profileName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Rounded.Person,
                            null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    profileName.ifBlank { "YouTube Music connected" },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    likedCount.toString() + " liked songs synced",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        SettingRow(
            Icons.Rounded.Favorite,
            "YouTube Liked Music",
            "Open synced liked songs",
            onClick = onOpenLiked
        )
        SettingRow(
            Icons.Rounded.Person,
            "Sign out",
            "Remove this YouTube Music session",
            onClick = onLogout
        )
    }
}

@Composable
private fun SettingsScreen(
    youtubeLoggedIn: Boolean,
    spotifyLoggedIn: Boolean,
    translucentNav: Boolean,
    liquidGlass: Boolean,
    romanizedLyrics: Boolean,
    nowPlayingStyle: String,
    lyricsStyle: String,
    themeColor: String,
    onBack: () -> Unit,
    onAccount: () -> Unit,
    onSpotify: () -> Unit,
    onTranslucentNav: (Boolean) -> Unit,
    onLiquidGlass: (Boolean) -> Unit,
    onRomanizedLyrics: (Boolean) -> Unit,
    onNowPlayingStyle: (String) -> Unit,
    onLyricsStyle: (String) -> Unit,
    onThemeColor: (String) -> Unit
) {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 36.dp)
    ) {
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Rounded.ArrowBack,
                        "Back",
                        modifier = Modifier.size(31.dp)
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    "Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(26.dp))

            SettingsSectionTitle("Interface")

            SettingsValueRow(
                title = "Theme",
                value = "Dark",
                onClick = {}
            )

            SettingsValueRow(
                title = "Now Playing style",
                value = nowPlayingStyle,
                onClick = {
                    onNowPlayingStyle(
                        if (nowPlayingStyle == "Classic") "Immersive"
                        else "Classic"
                    )
                }
            )

            SettingsValueRow(
                title = "Lyrics style",
                value = lyricsStyle,
                onClick = {
                    onLyricsStyle(
                        if (lyricsStyle == "Classic") "Card"
                        else "Classic"
                    )
                }
            )

            SettingsToggleRow(
                title = "Lyrics romanization",
                subtitle = "Show lyrics in Latin script when supported",
                checked = romanizedLyrics,
                onChecked = onRomanizedLyrics
            )

            SettingsValueRow(
                title = "Theme color",
                value = themeColor,
                onClick = {
                    onThemeColor(
                        when (themeColor) {
                            "Default" -> "Purple"
                            "Purple" -> "Red"
                            else -> "Default"
                        }
                    )
                }
            )

            SettingsToggleRow(
                title = "Translucent bottom navigation bar",
                subtitle = "Show content through the bottom navigation surface",
                checked = translucentNav,
                onChecked = onTranslucentNav
            )

            SettingsToggleRow(
                title = "Enable liquid glass (BETA)",
                subtitle = "Use glass-like translucent player and navigation surfaces",
                checked = liquidGlass,
                onChecked = onLiquidGlass
            )

            Spacer(Modifier.height(24.dp))
            SettingsSectionTitle("Content")

            SettingsValueRow(
                title = "YouTube Account",
                value =
                    if (youtubeLoggedIn)
                        "Connected"
                    else
                        "Not connected",
                onClick = onAccount
            )

            SettingsValueRow(
                title = "Language",
                value = "English",
                onClick = {}
            )

            SettingsValueRow(
                title = "Content country",
                value = "India",
                onClick = {}
            )

            SettingsValueRow(
                title = "Playback source",
                value = "YouTube Music API + YouTube stream extraction",
                onClick = {}
            )

            Spacer(Modifier.height(24.dp))
            SettingsSectionTitle("Spotify")

            SettingsValueRow(
                title =
                    if (spotifyLoggedIn)
                        "Disconnect Spotify"
                    else
                        "Connect Spotify",
                value =
                    if (spotifyLoggedIn)
                        "Connected • Spotify lyrics enabled with LRCLIB fallback"
                    else
                        "Optional • used for lyrics, not song streaming",
                onClick = onSpotify
            )

            Spacer(Modifier.height(24.dp))

            Text(
                "Dhunora",
                Modifier.padding(horizontal = 28.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                "Developer: Alok",
                Modifier.padding(horizontal = 28.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        title,
        Modifier.padding(horizontal = 28.dp, vertical = 10.dp),
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.White
    )
}

@Composable
private fun SettingsValueRow(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 28.dp, vertical = 12.dp)
    ) {
        Text(
            title,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Spacer(Modifier.height(5.dp))
        Text(
            value,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 28.dp, end = 24.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 24.sp,
                color = Color.White
            )
            Spacer(Modifier.height(5.dp))
            Text(
                subtitle,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.width(16.dp))

        Switch(
            checked = checked,
            onCheckedChange = onChecked
        )
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
    if (ms <= 1L || ms == Long.MIN_VALUE || ms < 0L) return "--:--"
    val total = ms / 1000
    val m = total / 60
    val s = total % 60
    return m.toString() + ":" + s.toString().padStart(2, '0')
}
