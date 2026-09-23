package com.alok.dhunora.ui.ui.screen.home

import androidx.compose.animation.AnimatedContent
import com.alok.dhunora.ui.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.eygraber.uri.toKmpUri
import com.maxrave.common.LIMIT_CACHE_SIZE
import com.maxrave.common.QUALITY
import com.maxrave.common.SUPPORTED_LANGUAGE
import com.maxrave.common.SUPPORTED_LOCATION
import com.maxrave.common.SponsorBlockType
import com.maxrave.common.VIDEO_QUALITY
import com.maxrave.domain.extension.now
import com.maxrave.domain.data.model.lyrics.RomanizationDictionaryState
import com.maxrave.domain.data.model.lyrics.RomanizationLanguage
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.domain.manager.DataStoreManager.Values.TRUE
import com.maxrave.domain.repository.ImportProgress
import com.maxrave.domain.utils.LocalResource
import com.maxrave.logger.Logger
import com.alok.dhunora.ui.Platform
import com.alok.dhunora.ui.expect.ui.LoginSyncDialog
import com.alok.dhunora.ui.expect.ui.fileSaverResult
import com.alok.dhunora.ui.expect.ui.isLyricsBlurSupported
import com.alok.dhunora.ui.expect.ui.isWallpaperDynamicColorSupported
import com.alok.dhunora.ui.expect.ui.openEqResult
import com.alok.dhunora.ui.extension.bytesToMB
import com.alok.dhunora.ui.extension.displayString
import com.alok.dhunora.ui.extension.isTwoLetterCode
import com.alok.dhunora.ui.extension.isValidProxyHost
import com.alok.dhunora.ui.getPlatform
import com.alok.dhunora.ui.ui.component.ActionButton
import com.alok.dhunora.ui.ui.component.AmbientThemeGlow
import com.alok.dhunora.ui.ui.component.CenterLoadingBox
import com.alok.dhunora.ui.ui.component.EndOfPage
import com.alok.dhunora.ui.ui.component.LoadingDialog
import com.alok.dhunora.ui.ui.component.RippleIconButton
import com.alok.dhunora.ui.ui.component.SettingItem
import com.alok.dhunora.ui.ui.component.rememberNowPlayingGlowTint
import com.alok.dhunora.ui.ui.icon.ArrowBackIosNew
import com.alok.dhunora.ui.ui.icon.Close
import com.alok.dhunora.ui.ui.icon.Error
import com.alok.dhunora.ui.ui.icon.PeopleAlt
import com.alok.dhunora.ui.ui.icon.PlaylistAdd
import com.alok.dhunora.ui.ui.icon.SimpIcons
import com.alok.dhunora.ui.ui.navigation.destination.login.LoginDestination
import com.alok.dhunora.ui.ui.navigation.destination.login.SpotifyLoginDestination
import com.alok.dhunora.ui.ui.theme.md_theme_dark_primary
import com.alok.dhunora.ui.ui.theme.parseThemeColorHex
import com.alok.dhunora.ui.ui.theme.typo
import com.alok.dhunora.ui.utils.VersionManager
import com.alok.dhunora.ui.viewModel.ImportViewModel
import com.alok.dhunora.ui.viewModel.SettingAlertState
import com.alok.dhunora.ui.viewModel.SettingBasicAlertState
import com.alok.dhunora.ui.viewModel.SettingsViewModel
import com.alok.dhunora.ui.viewModel.SharedViewModel
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.ui.compose.ChipColors
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.libraryColors
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import com.mohamedrejeb.calf.core.ExperimentalCalfApi
import com.mohamedrejeb.calf.io.getPath
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import com.alok.dhunora.ui.compat.getString
import androidx.compose.ui.res.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalCoilApi::class,
    ExperimentalHazeMaterialsApi::class,
    FormatStringsInDatetimeFormats::class,
    ExperimentalCalfApi::class,
)
@Composable
fun SettingScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    viewModel: SettingsViewModel = koinViewModel(),
    sharedViewModel: SharedViewModel = koinInject(),
) {
    val platformContext = LocalPlatformContext.current
    val context = LocalContext.current
    val pl = com.mohamedrejeb.calf.core.LocalPlatformContext.current
    val localDensity = LocalDensity.current
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()

    var width by rememberSaveable { mutableIntStateOf(0) }

    // Backup and restore
    val formatter =
        LocalDateTime.Format {
            byUnicodePattern("yyyyMMddHHmmss")
        }
    val appName = stringResource(R.string.app_name)

    val backupLauncher =
        fileSaverResult(
            "${appName}_${
                now().format(
                    formatter,
                )
            }.backup",
            "application/octet-stream",
        ) { uri ->
            uri?.let {
                viewModel.backup(it.toKmpUri())
            }
        }

    val restoreLauncher =
        rememberFilePickerLauncher(
            type =
                FilePickerFileType.All,
            selectionMode = FilePickerSelectionMode.Single,
        ) { file ->
            file.firstOrNull()?.getPath(pl)?.toKmpUri()?.let {
                viewModel.restore(it)
            }
        }

    // Import playlists converted on the web. Unlike restore, the file is read through Calf's
    // KmpFile rather than a Uri, so no expect/actual is needed. The type stays All because a
    // converted .json arrives with whatever MIME its source assigned it, and an application/json
    // filter would hide it on some hosts.
    val importViewModel: ImportViewModel = koinViewModel()
    val importState by importViewModel.importState.collectAsStateWithLifecycle()
    val importLauncher =
        rememberFilePickerLauncher(
            type =
                FilePickerFileType.All,
            selectionMode = FilePickerSelectionMode.Single,
        ) { file ->
            file.firstOrNull()?.let {
                importViewModel.import(it, pl)
            }
        }

    // Open equalizer
    val resultLauncher = openEqResult(viewModel.getAudioSessionId())

    val language by viewModel.language.collectAsStateWithLifecycle()
    val location by viewModel.location.collectAsStateWithLifecycle()
    val quality by viewModel.quality.collectAsStateWithLifecycle()
    val downloadQuality by viewModel.downloadQuality.collectAsStateWithLifecycle()
    val autoDownloadLikedSongs by viewModel.autoDownloadLikedSongs.collectAsStateWithLifecycle()
    val videoDownloadQuality by viewModel.videoDownloadQuality.collectAsStateWithLifecycle()
    val keepYoutubePlaylistOffline by viewModel.keepYouTubePlaylistOffline.collectAsStateWithLifecycle()
    val localTrackingEnabled by viewModel.localTrackingEnabled.collectAsStateWithLifecycle(initialValue = false)
    val blogNotificationEnabled by viewModel.blogNotificationEnabled.collectAsStateWithLifecycle()
    val combineLocalAndYouTubeLiked by viewModel.combineLocalAndYouTubeLiked.collectAsStateWithLifecycle()
    val playVideo by remember { viewModel.playVideoInsteadOfAudio.map { it == TRUE } }.collectAsStateWithLifecycle(initialValue = false)
    val radioAudioOnly by remember { viewModel.radioAudioOnly.map { it == TRUE } }.collectAsStateWithLifecycle(initialValue = false)
    val videoQuality by viewModel.videoQuality.collectAsStateWithLifecycle()
    val sendData by remember { viewModel.sendBackToGoogle.map { it == TRUE } }.collectAsStateWithLifecycle(initialValue = false)
    val normalizeVolume by remember { viewModel.normalizeVolume.map { it == TRUE } }.collectAsStateWithLifecycle(initialValue = false)
    val skipSilent by remember { viewModel.skipSilent.map { it == TRUE } }.collectAsStateWithLifecycle(initialValue = false)
    val savePlaybackState by remember { viewModel.savedPlaybackState.map { it == TRUE } }.collectAsStateWithLifecycle(initialValue = false)
    val killServiceOnExit by remember { viewModel.killServiceOnExit.map { it == TRUE } }.collectAsStateWithLifecycle(initialValue = true)
    val mainLyricsProvider by viewModel.mainLyricsProvider.collectAsStateWithLifecycle()
    val lyricsOffsetMs by viewModel.lyricsOffsetMs.collectAsStateWithLifecycle()
    val youtubeSubtitleLanguage by viewModel.youtubeSubtitleLanguage.collectAsStateWithLifecycle()
    val preferredAudioLanguage by viewModel.preferredAudioLanguage.collectAsStateWithLifecycle()
    val spotifyLoggedIn by viewModel.spotifyLogIn.collectAsStateWithLifecycle()
    val spotifyLyrics by viewModel.spotifyLyrics.collectAsStateWithLifecycle()
    val spotifyCanvas by viewModel.spotifyCanvas.collectAsStateWithLifecycle()
    val amAnimatedArtwork by viewModel.amAnimatedArtwork.collectAsStateWithLifecycle()
    val enableSponsorBlock by remember { viewModel.sponsorBlockEnabled.map { it == TRUE } }.collectAsStateWithLifecycle(initialValue = false)
    val skipSegments by viewModel.sponsorBlockCategories.collectAsStateWithLifecycle()
    val playerCache by viewModel.cacheSize.collectAsStateWithLifecycle()
    val downloadedCache by viewModel.downloadedCacheSize.collectAsStateWithLifecycle()
    val thumbnailCache by viewModel.thumbCacheSize.collectAsStateWithLifecycle()
    val canvasCache by viewModel.canvasCacheSize.collectAsStateWithLifecycle()
    val limitPlayerCache by viewModel.playerCacheLimit.collectAsStateWithLifecycle()
    val fraction by viewModel.fraction.collectAsStateWithLifecycle()
    val lastCheckUpdate by viewModel.lastCheckForUpdate.collectAsStateWithLifecycle()
    val explicitContentEnabled by viewModel.explicitContentEnabled.collectAsStateWithLifecycle()
    val usingProxy by viewModel.usingProxy.collectAsStateWithLifecycle()
    val proxyType by viewModel.proxyType.collectAsStateWithLifecycle()
    val proxyHost by viewModel.proxyHost.collectAsStateWithLifecycle()
    val proxyPort by viewModel.proxyPort.collectAsStateWithLifecycle()
    val proxyUsername by viewModel.proxyUsername.collectAsStateWithLifecycle()
    val proxyPassword by viewModel.proxyPassword.collectAsStateWithLifecycle()
    val autoCheckUpdate by viewModel.autoCheckUpdate.collectAsStateWithLifecycle()
    val aiProvider by viewModel.aiProvider.collectAsStateWithLifecycle()
    val isHasApiKey by viewModel.isHasApiKey.collectAsStateWithLifecycle()
    val useAITranslation by viewModel.useAITranslation.collectAsStateWithLifecycle()
    val translationLanguage by viewModel.translationLanguage.collectAsStateWithLifecycle()
    val customModelId by viewModel.customModelId.collectAsStateWithLifecycle()
    val customOpenAIBaseUrl by viewModel.customOpenAIBaseUrl.collectAsStateWithLifecycle()
    val customOpenAIHeaders by viewModel.customOpenAIHeaders.collectAsStateWithLifecycle()
    val helpBuildLyricsDatabase by viewModel.helpBuildLyricsDatabase.collectAsStateWithLifecycle()
    val contributor by viewModel.contributor.collectAsStateWithLifecycle()
    val backupDownloaded by viewModel.backupDownloaded.collectAsStateWithLifecycle()
    val autoBackupEnabled by viewModel.autoBackupEnabled.collectAsStateWithLifecycle()
    val autoBackupFrequency by viewModel.autoBackupFrequency.collectAsStateWithLifecycle()
    val autoBackupMaxFiles by viewModel.autoBackupMaxFiles.collectAsStateWithLifecycle()
    val autoBackupLastTime by viewModel.autoBackupLastTime.collectAsStateWithLifecycle()
    val updateChannel by viewModel.updateChannel.collectAsStateWithLifecycle()
    val enableLiquidGlass by viewModel.enableLiquidGlass.collectAsStateWithLifecycle()
    val themeMode by sharedViewModel.getThemeMode().collectAsStateWithLifecycle(DataStoreManager.THEME_MODE_DARK)
    val themeColorSource by sharedViewModel.getThemeColorSource().collectAsStateWithLifecycle(DataStoreManager.THEME_COLOR_DEFAULT)
    val customThemeColorHex by sharedViewModel.getCustomThemeColor().collectAsStateWithLifecycle(DataStoreManager.DEFAULT_THEME_COLOR_HEX)
    val nowPlayingStyle by sharedViewModel.getNowPlayingStyle().collectAsStateWithLifecycle(DataStoreManager.NOW_PLAYING_STYLE_SPOTIFY)
    val lyricsStyle by sharedViewModel.getLyricsStyle().collectAsStateWithLifecycle(DataStoreManager.LYRICS_STYLE_CLASSIC)
    val romanizationStored by sharedViewModel.getRomanizationLanguages().collectAsStateWithLifecycle("")
    val japaneseDictionaryState by viewModel.japaneseDictionaryState.collectAsStateWithLifecycle()
    var showColorPickerDialog by rememberSaveable { mutableStateOf(false) }
    val loggedIn by viewModel.loggedIn.collectAsStateWithLifecycle()
    val syncFollowToYouTube by viewModel.syncFollowToYouTube.collectAsStateWithLifecycle()
    val equalizerEnabled by viewModel.equalizerEnabled.collectAsStateWithLifecycle()
    val equalizerType by viewModel.equalizerType.collectAsStateWithLifecycle()
    val delayEnabled by viewModel.delayEnabled.collectAsStateWithLifecycle()
    val reverbEnabled by viewModel.reverbEnabled.collectAsStateWithLifecycle()
    val richPresenceEnabled by viewModel.richPresenceEnabled.collectAsStateWithLifecycle()

    val crossfadeEnabled by viewModel.crossfadeEnabled.collectAsStateWithLifecycle()
    val crossfadeDuration by viewModel.crossfadeDuration.collectAsStateWithLifecycle()
    val crossfadeDjMode by viewModel.crossfadeDjMode.collectAsStateWithLifecycle()
    val crossfadeSkipAlbum by viewModel.crossfadeSkipAlbum.collectAsStateWithLifecycle()
    val castState by viewModel.castState.collectAsStateWithLifecycle()

    val isCheckingUpdate by sharedViewModel.isCheckingUpdate.collectAsStateWithLifecycle()

    val hazeState =
        rememberHazeState(
            blurEnabled = true,
        )

    val checkForUpdateSubtitle by remember {
        derivedStateOf {
            if (isCheckingUpdate) {
                return@derivedStateOf runBlocking { getString(R.string.checking) }
            } else {
                val lastCheckLong = lastCheckUpdate?.toLong() ?: 0L
                return@derivedStateOf runBlocking {
                    getString(
                        R.string.last_checked_at,
                        DateTimeFormatter
                            .ofPattern("yyyy-MM-dd HH:mm:ss")
                            .withZone(ZoneId.systemDefault())
                            .format(Instant.ofEpochMilli(lastCheckLong)),
                    )
                }
            }
        }
    }
    var showYouTubeAccountDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var showLoginSyncDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var showThirdPartyLibraries by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(true) {
        viewModel.getAllGoogleAccount()
    }

    LaunchedEffect(true) {
        viewModel.getData()
        viewModel.getThumbCacheSize(platformContext)
    }

    val settingListState = rememberLazyListState()
    // Home's rule: transparent only while pixel-0 is on screen. The frost itself is kept LIGHT
    // (below) so frosting over the glow reads as a veil, not a lid.
    val isAtTop by remember {
        derivedStateOf { settingListState.firstVisibleItemIndex == 0 && settingListState.firstVisibleItemScrollOffset == 0 }
    }
    // Home-family ambient ground, and like Home's it SCROLLS AWAY with the content instead of
    // hanging off the ceiling. Still a sibling (so it sits behind the floating bar), but its draw
    // rides the list: exact tracking while item 0 is on screen, parked off-screen after. Item 0 is
    // taller than the glow, so the glow has fully left before the branch ever switches — no jump.
    // graphicsLayer reads the state in the DRAW phase, so scrolling redraws without recomposing.
    val glowNowPlaying by sharedViewModel.nowPlayingState.collectAsStateWithLifecycle()
    AmbientThemeGlow(
        tint = rememberNowPlayingGlowTint(glowNowPlaying?.songEntity?.thumbnails),
        modifier =
            Modifier.graphicsLayer {
                translationY =
                    if (settingListState.firstVisibleItemIndex == 0) {
                        -settingListState.firstVisibleItemScrollOffset.toFloat()
                    } else {
                        -size.height
                    }
            },
    )
    LazyColumn(
        state = settingListState,
        contentPadding = innerPadding,
        modifier =
            Modifier
                .padding(horizontal = 16.dp)
                .hazeSource(hazeState),
    ) {
        item(key = "user_interface") {
            Column {
                // Was its own item. Folded in so item 0 is taller than the glow — the glow's
                // translation tracks item 0's offset exactly and parks once it scrolls past, and a
                // 64dp item 0 would have switched branches while the glow was still half-visible.
                Spacer(Modifier.height(64.dp))
                Spacer(Modifier.height(16.dp))
                // Above every section, and inside item 0 rather than an item of its own, for the
                // glow reason above.
                Text(text = stringResource(R.string.login_sync_section), style = typo().labelMedium, color = MaterialTheme.colorScheme.onBackground)
                SettingItem(
                    title =
                        stringResource(
                            if (getPlatform() == Platform.Android) R.string.login_sync_android_title else R.string.login_sync_desktop_title,
                        ),
                    subtitle =
                        stringResource(
                            if (getPlatform() == Platform.Android) {
                                R.string.login_sync_android_description
                            } else {
                                R.string.login_sync_desktop_description
                            },
                        ),
                    onClick = { showLoginSyncDialog = true },
                )
                Spacer(Modifier.height(8.dp))
                Text(text = stringResource(R.string.user_interface), style = typo().labelMedium, color = MaterialTheme.colorScheme.onBackground)
                val themeModeLabels =
                    listOf(
                        DataStoreManager.THEME_MODE_SYSTEM to stringResource(R.string.theme_mode_system),
                        DataStoreManager.THEME_MODE_DARK to stringResource(R.string.theme_mode_dark),
                        DataStoreManager.THEME_MODE_LIGHT to stringResource(R.string.theme_mode_light),
                    )
                SettingItem(
                    title = stringResource(R.string.theme),
                    subtitle = themeModeLabels.firstOrNull { it.first == themeMode }?.second ?: "",
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(R.string.theme) },
                                selectOne =
                                    SettingAlertState.SelectData(
                                        listSelect = themeModeLabels.map { (it.first == themeMode) to it.second },
                                    ),
                                confirm =
                                    runBlocking { getString(R.string.change) } to { state ->
                                        val selected = state.selectOne?.getSelected()
                                        themeModeLabels.firstOrNull { it.second == selected }?.first?.let {
                                            sharedViewModel.setThemeMode(it)
                                        }
                                    },
                                dismiss = runBlocking { getString(R.string.cancel) },
                            ),
                        )
                    },
                )
                // The Apple Music treatments ARE the blur — the frosted page behind the player, and
                // the depth of field on the lyrics — and Modifier.blur is a documented no-op below
                // Android 12, so on an older device they render as a flat, wrong-looking version of
                // themselves. The requirement is spelled out on the option itself rather than left
                // for the user to discover after switching.
                val requiresAndroid12 = " (" + stringResource(R.string.requires_android_12) + ")"
                val nowPlayingStyleLabels =
                    listOf(
                        DataStoreManager.NOW_PLAYING_STYLE_SPOTIFY to stringResource(R.string.now_playing_style_spotify),
                        DataStoreManager.NOW_PLAYING_STYLE_M3_EXPRESSIVE to stringResource(R.string.now_playing_style_m3_expressive),
                        DataStoreManager.NOW_PLAYING_STYLE_APPLE_MUSIC to
                            stringResource(R.string.now_playing_style_apple_music) + requiresAndroid12,
                    )
                SettingItem(
                    title = stringResource(R.string.now_playing_style),
                    subtitle = nowPlayingStyleLabels.firstOrNull { it.first == nowPlayingStyle }?.second ?: "",
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(R.string.now_playing_style) },
                                selectOne =
                                    SettingAlertState.SelectData(
                                        listSelect = nowPlayingStyleLabels.map { (it.first == nowPlayingStyle) to it.second },
                                    ),
                                confirm =
                                    runBlocking { getString(R.string.change) } to { state ->
                                        val selected = state.selectOne?.getSelected()
                                        nowPlayingStyleLabels.firstOrNull { it.second == selected }?.first?.let {
                                            sharedViewModel.setNowPlayingStyle(it)
                                        }
                                    },
                                dismiss = runBlocking { getString(R.string.cancel) },
                            ),
                        )
                    },
                )
                // Hidden outright below Android 12 rather than offered with one option: the Apple
                // Music treatment IS the blur, and Modifier.blur is a documented no-op there, so
                // the choice would be between Classic and a broken-looking Classic.
                if (isLyricsBlurSupported()) {
                    val lyricsStyleLabels =
                        listOf(
                            DataStoreManager.LYRICS_STYLE_CLASSIC to stringResource(R.string.lyrics_style_classic),
                            DataStoreManager.LYRICS_STYLE_APPLE_MUSIC to
                                stringResource(R.string.lyrics_style_apple_music) + requiresAndroid12,
                        )
                    SettingItem(
                        title = stringResource(R.string.lyrics_style),
                        subtitle = lyricsStyleLabels.firstOrNull { it.first == lyricsStyle }?.second ?: "",
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = runBlocking { getString(R.string.lyrics_style) },
                                    selectOne =
                                        SettingAlertState.SelectData(
                                            listSelect = lyricsStyleLabels.map { (it.first == lyricsStyle) to it.second },
                                        ),
                                    confirm =
                                        runBlocking { getString(R.string.change) } to { state ->
                                            val selected = state.selectOne?.getSelected()
                                            lyricsStyleLabels.firstOrNull { it.second == selected }?.first?.let {
                                                sharedViewModel.setLyricsStyle(it)
                                            }
                                        },
                                    dismiss = runBlocking { getString(R.string.cancel) },
                                ),
                            )
                        },
                    )
                }

                // Independent of BOTH style settings, and not gated on Android 12: this changes
                // what the words SAY, not how they are drawn, so it applies to every style on
                // every version. Sits next to them because a user looking for "something about
                // lyrics" looks in one place.
                val romanizationLabels =
                    listOf(
                        RomanizationLanguage.JAPANESE to stringResource(R.string.romanization_japanese),
                        RomanizationLanguage.KOREAN to stringResource(R.string.romanization_korean),
                        RomanizationLanguage.CHINESE to stringResource(R.string.romanization_chinese),
                        RomanizationLanguage.HINDI to stringResource(R.string.romanization_hindi),
                        RomanizationLanguage.PUNJABI to stringResource(R.string.romanization_punjabi),
                        RomanizationLanguage.RUSSIAN to stringResource(R.string.romanization_russian),
                        RomanizationLanguage.UKRAINIAN to stringResource(R.string.romanization_ukrainian),
                        RomanizationLanguage.SERBIAN to stringResource(R.string.romanization_serbian),
                        RomanizationLanguage.BULGARIAN to stringResource(R.string.romanization_bulgarian),
                        RomanizationLanguage.BELARUSIAN to stringResource(R.string.romanization_belarusian),
                        RomanizationLanguage.KYRGYZ to stringResource(R.string.romanization_kyrgyz),
                        RomanizationLanguage.MACEDONIAN to stringResource(R.string.romanization_macedonian),
                    )
                val romanizationSelected = RomanizationLanguage.parse(romanizationStored)
                SettingItem(
                    title = stringResource(R.string.lyrics_romanization),
                    // Two different jobs for one line. Off, the row has to explain what the
                    // feature IS — nobody guesses "romanization" from the title alone. On, the only
                    // question worth answering at a glance is which of the twelve are picked, and
                    // the explanation has served its purpose.
                    subtitle =
                        if (romanizationSelected.isEmpty()) {
                            stringResource(R.string.lyrics_romanization_description)
                        } else {
                            val selectedNames =
                                romanizationLabels.filter { it.first in romanizationSelected }.joinToString(", ") { it.second }
                            // Japanese is the one language with a dictionary pack to fetch; while
                            // that is in flight — or has failed — the row says so, instead of
                            // listing Japanese as if it were already live.
                            when {
                                RomanizationLanguage.JAPANESE !in romanizationSelected -> selectedNames
                                japaneseDictionaryState == RomanizationDictionaryState.DOWNLOADING ->
                                    "$selectedNames — ${stringResource(R.string.romanization_japanese_dict_downloading)}"
                                japaneseDictionaryState == RomanizationDictionaryState.FAILED ->
                                    "$selectedNames — ${stringResource(R.string.romanization_japanese_dict_failed)}"
                                else -> selectedNames
                            }
                        },
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(R.string.lyrics_romanization) },
                                // NO `message` here, deliberately. The dialog picks its body with
                                // an if/else-if chain that tests `message` FIRST, and that branch
                                // renders only the text and an optional textField — a multipleSelect
                                // passed alongside it is never reached, so the dialog came up with
                                // the description and no languages at all.
                                multipleSelect =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            romanizationLabels.map { (language, label) ->
                                                (language in romanizationSelected) to label
                                            },
                                    ),
                                confirm =
                                    runBlocking { getString(R.string.save) } to { state ->
                                        val chosen = state.multipleSelect?.getListSelected().orEmpty()
                                        val languages =
                                            romanizationLabels.filter { it.second in chosen }.map { it.first }.toSet()
                                        sharedViewModel.setRomanizationLanguages(languages)
                                        // Japanese needs its dictionary pack on disk. A no-op when
                                        // it is already there (or bundled, as on Desktop) — and the
                                        // retry after a FAILED attempt is simply confirming again.
                                        if (RomanizationLanguage.JAPANESE in languages) {
                                            viewModel.downloadJapaneseDictionaryIfNeeded()
                                        }
                                    },
                                dismiss = runBlocking { getString(R.string.cancel) },
                            ),
                        )
                    },
                )
                val colorSourceLabels =
                    buildList {
                        add(DataStoreManager.THEME_COLOR_DEFAULT to stringResource(R.string.theme_color_default))
                        if (isWallpaperDynamicColorSupported()) {
                            add(DataStoreManager.THEME_COLOR_WALLPAPER to stringResource(R.string.theme_color_wallpaper))
                        }
                        add(DataStoreManager.THEME_COLOR_CUSTOM to stringResource(R.string.theme_color_custom))
                    }
                SettingItem(
                    title = stringResource(R.string.theme_color),
                    subtitle = colorSourceLabels.firstOrNull { it.first == themeColorSource }?.second ?: "",
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(R.string.theme_color) },
                                selectOne =
                                    SettingAlertState.SelectData(
                                        listSelect = colorSourceLabels.map { (it.first == themeColorSource) to it.second },
                                    ),
                                confirm =
                                    runBlocking { getString(R.string.change) } to { state ->
                                        val selected = state.selectOne?.getSelected()
                                        colorSourceLabels.firstOrNull { it.second == selected }?.first?.let {
                                            sharedViewModel.setThemeColorSource(it)
                                            if (it == DataStoreManager.THEME_COLOR_CUSTOM) {
                                                showColorPickerDialog = true
                                            }
                                        }
                                    },
                                dismiss = runBlocking { getString(R.string.cancel) },
                            ),
                        )
                    },
                )
                if (themeColorSource == DataStoreManager.THEME_COLOR_CUSTOM) {
                    SettingItem(
                        title = stringResource(R.string.custom_color),
                        subtitle = "#${customThemeColorHex.takeLast(6)}",
                        smallSubtitle = true,
                        onClick = { showColorPickerDialog = true },
                    )
                }
                if (getPlatform() == Platform.Android) {
                    SettingItem(
                        title = stringResource(R.string.enable_liquid_glass_effect),
                        subtitle = stringResource(R.string.enable_liquid_glass_effect_description),
                        smallSubtitle = true,
                        switch = (enableLiquidGlass to { viewModel.setEnableLiquidGlass(it) }),
                        isEnable = getPlatform() == Platform.Android,
                    )
                }
            }
        }
        item(key = "content") {
            Column {
                Text(
                    text = stringResource(R.string.content),
                    style = typo().labelMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                SettingItem(
                    title = stringResource(R.string.youtube_account),
                    subtitle = stringResource(R.string.manage_your_youtube_accounts),
                    onClick = {
                        viewModel.getAllGoogleAccount()
                        showYouTubeAccountDialog = true
                    },
                )
                SettingItem(
                    title = stringResource(R.string.language),
                    subtitle = SUPPORTED_LANGUAGE.getLanguageFromCode(language ?: "en-US"),
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(R.string.language) },
                                selectOne =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            SUPPORTED_LANGUAGE.items.map {
                                                (it.toString() == SUPPORTED_LANGUAGE.getLanguageFromCode(language ?: "en-US")) to it.toString()
                                            },
                                    ),
                                confirm =
                                    runBlocking { getString(R.string.change) } to { state ->
                                        val code = SUPPORTED_LANGUAGE.getCodeFromLanguage(state.selectOne?.getSelected() ?: "English")
                                        viewModel.setBasicAlertData(
                                            SettingBasicAlertState(
                                                title = runBlocking { getString(R.string.warning) },
                                                message = runBlocking { getString(R.string.change_language_warning) },
                                                confirm =
                                                    runBlocking { getString(R.string.change) } to {
                                                        sharedViewModel.activityRecreate()
                                                        viewModel.setBasicAlertData(null)
                                                        viewModel.changeLanguage(code)
                                                    },
                                                dismiss = runBlocking { getString(R.string.cancel) },
                                            ),
                                        )
                                    },
                                dismiss = runBlocking { getString(R.string.cancel) },
                            ),
                        )
                    },
                )
                SettingItem(
                    title = stringResource(R.string.content_country),
                    subtitle = location ?: "",
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(R.string.content_country) },
                                selectOne =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            SUPPORTED_LOCATION.items.map { item ->
                                                (item.toString() == location) to item.toString()
                                            },
                                    ),
                                confirm =
                                    runBlocking { getString(R.string.change) } to { state ->
                                        viewModel.changeLocation(
                                            state.selectOne?.getSelected() ?: "US",
                                        )
                                    },
                                dismiss = runBlocking { getString(R.string.cancel) },
                            ),
                        )
                    },
                )
                SettingItem(
                    title = stringResource(R.string.preferred_audio_language),
                    subtitle = preferredAudioLanguage.ifEmpty { stringResource(R.string.original_audio) },
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(R.string.preferred_audio_language) },
                                textField =
                                    SettingAlertState.TextFieldData(
                                        label = runBlocking { getString(R.string.preferred_audio_language) },
                                        value = preferredAudioLanguage,
                                        // Empty is valid here: it means "original audio".
                                        verifyCodeBlock = {
                                            (it.isEmpty() || it.isTwoLetterCode()) to
                                                runBlocking { getString(R.string.invalid_language_code) }
                                        },
                                    ),
                                message = runBlocking { getString(R.string.preferred_audio_language_message) },
                                confirm =
                                    runBlocking { getString(R.string.change) } to { state ->
                                        viewModel.setPreferredAudioLanguage(state.textField?.value ?: "")
                                    },
                                dismiss = runBlocking { getString(R.string.cancel) },
                            ),
                        )
                    },
                )
                SettingItem(
                    title = stringResource(R.string.quality),
                    subtitle = quality ?: "",
                    smallSubtitle = true,
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(R.string.quality) },
                                selectOne =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            QUALITY.items.map { item ->
                                                (item.toString() == quality) to item.toString()
                                            },
                                    ),
                                confirm =
                                    runBlocking { getString(R.string.change) } to { state ->
                                        viewModel.changeQuality(state.selectOne?.getSelected())
                                    },
                                dismiss = runBlocking { getString(R.string.cancel) },
                            ),
                        )
                    },
                )
                SettingItem(
                    title = stringResource(R.string.download_quality),
                    subtitle = downloadQuality ?: "",
                    smallSubtitle = true,
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(R.string.download_quality) },
                                selectOne =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            QUALITY.items.map { item ->
                                                (item.toString() == downloadQuality) to item.toString()
                                            },
                                    ),
                                confirm =
                                    runBlocking { getString(R.string.change) } to { state ->
                                        state.selectOne?.getSelected()?.let { viewModel.setDownloadQuality(it) }
                                    },
                                dismiss = runBlocking { getString(R.string.cancel) },
                            ),
                        )
                    },
                )
                SettingItem(
                    title = stringResource(R.string.video_quality),
                    subtitle = videoQuality ?: "",
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(R.string.video_quality) },
                                selectOne =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            VIDEO_QUALITY.items.map { item ->
                                                (item.toString() == videoQuality) to item.toString()
                                            },
                                    ),
                                confirm =
                                    runBlocking { getString(R.string.change) } to { state ->
                                        viewModel.changeVideoQuality(state.selectOne?.getSelected() ?: "")
                                    },
                                dismiss = runBlocking { getString(R.string.cancel) },
                            ),
                        )
                    },
                )
                SettingItem(
                    title = stringResource(R.string.video_download_quality),
                    subtitle = videoDownloadQuality ?: "",
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(R.string.video_download_quality) },
                                selectOne =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            VIDEO_QUALITY.items.map { item ->
                                                (item.toString() == videoDownloadQuality) to item.toString()
                                            },
                                    ),
                                confirm =
                                    runBlocking { getString(R.string.change) } to { state ->
                                        viewModel.setVideoDownloadQuality(state.selectOne?.getSelected() ?: "")
                                    },
                                dismiss = runBlocking { getString(R.string.cancel) },
                            ),
                        )
                    },
                )
                SettingItem(
                    title = stringResource(R.string.auto_download_liked_songs),
                    subtitle = stringResource(R.string.auto_download_liked_songs_description),
                    smallSubtitle = true,
                    switch = (autoDownloadLikedSongs to { viewModel.setAutoDownloadLikedSongs(it) }),
                )
                SettingItem(
                    title = stringResource(R.string.play_video_for_video_track_instead_of_audio_only),
                    subtitle = stringResource(R.string.such_as_music_video_lyrics_video_podcasts_and_more),
                    smallSubtitle = true,
                    switch = (playVideo to { viewModel.setPlayVideoInsteadOfAudio(it) }),
                )
                SettingItem(
                    title = stringResource(R.string.radio_audio_only),
                    subtitle = stringResource(R.string.radio_audio_only_description),
                    smallSubtitle = true,
                    switch = (radioAudioOnly to { viewModel.setRadioAudioOnly(it) }),
                )
                SettingItem(
                    title = stringResource(R.string.sync_follow_to_youtube),
                    subtitle = stringResource(R.string.sync_follow_to_youtube_description),
                    smallSubtitle = true,
                    switch = (syncFollowToYouTube to { viewModel.setSyncFollowToYouTube(it) }),
                    // Writing to someone's YouTube account needs a session, so the row is dead
                    // while signed out. Clearing the stored flag is NOT done from here: the reset
                    // belongs to the logout itself (SettingsViewModel.setUsedAccount /
                    // logOutAllYouTube), which runs whether or not Settings is ever opened.
                    isEnable = loggedIn == DataStoreManager.TRUE,
                )
                SettingItem(
                    title = stringResource(R.string.send_back_listening_data_to_google),
                    subtitle =
                        stringResource(
                            R.string
                                .upload_your_listening_history_to_youtube_music_server_it_will_make_yt_music_recommendation_system_better_working_only_if_logged_in,
                        ),
                    smallSubtitle = true,
                    switch = (sendData to { viewModel.setSendBackToGoogle(it) }),
                )
                SettingItem(
                    title = stringResource(R.string.play_explicit_content),
                    subtitle = stringResource(R.string.play_explicit_content_description),
                    switch = (explicitContentEnabled to { viewModel.setExplicitContentEnabled(it) }),
                )
                SettingItem(
                    title = stringResource(R.string.keep_your_youtube_playlist_offline),
                    subtitle = stringResource(R.string.keep_your_youtube_playlist_offline_description),
                    switch = (keepYoutubePlaylistOffline to { viewModel.setKeepYouTubePlaylistOffline(it) }),
                )
                /*
                SettingItem(
                    title = stringResource(R.string.combine_local_and_youtube_liked_songs),
                    subtitle = stringResource(R.string.combine_local_and_youtube_liked_songs_description),
                    switch = (combineLocalAndYouTubeLiked to { viewModel.setCombineLocalAndYouTubeLiked(it) })
                )
                 */
                SettingItem(
                    title = stringResource(R.string.proxy),
                    subtitle = stringResource(R.string.proxy_description),
                    switch = (usingProxy to { viewModel.setUsingProxy(it) }),
                )
            }
        }
        item(key = "proxy") {
            Crossfade(usingProxy) { it ->
                if (it) {
                    Column {
                        SettingItem(
                            title = stringResource(R.string.proxy_type),
                            subtitle =
                                when (proxyType) {
                                    DataStoreManager.ProxyType.PROXY_TYPE_HTTP -> stringResource(R.string.http)
                                    DataStoreManager.ProxyType.PROXY_TYPE_SOCKS -> stringResource(R.string.socks)
                                },
                            onClick = {
                                viewModel.setAlertData(
                                    SettingAlertState(
                                        title = runBlocking { getString(R.string.proxy_type) },
                                        selectOne =
                                            SettingAlertState.SelectData(
                                                listSelect =
                                                    listOf(
                                                        (proxyType == DataStoreManager.ProxyType.PROXY_TYPE_HTTP) to
                                                            runBlocking {
                                                                getString(
                                                                    R.string.http,
                                                                )
                                                            },
                                                        (proxyType == DataStoreManager.ProxyType.PROXY_TYPE_SOCKS) to
                                                            runBlocking { getString(R.string.socks) },
                                                    ),
                                            ),
                                        confirm =
                                            runBlocking { getString(R.string.change) } to { state ->
                                                viewModel.setProxy(
                                                    if (state.selectOne?.getSelected() == runBlocking { getString(R.string.socks) }) {
                                                        DataStoreManager.ProxyType.PROXY_TYPE_SOCKS
                                                    } else {
                                                        DataStoreManager.ProxyType.PROXY_TYPE_HTTP
                                                    },
                                                    proxyHost,
                                                    proxyPort,
                                                )
                                            },
                                        dismiss = runBlocking { getString(R.string.cancel) },
                                    ),
                                )
                            },
                        )
                        SettingItem(
                            title = stringResource(R.string.proxy_host),
                            subtitle = proxyHost,
                            onClick = {
                                viewModel.setAlertData(
                                    SettingAlertState(
                                        title = runBlocking { getString(R.string.proxy_host) },
                                        message = runBlocking { getString(R.string.proxy_host_message) },
                                        textField =
                                            SettingAlertState.TextFieldData(
                                                label = runBlocking { getString(R.string.proxy_host) },
                                                value = proxyHost,
                                                verifyCodeBlock = {
                                                    isValidProxyHost(it) to runBlocking { getString(R.string.invalid_host) }
                                                },
                                            ),
                                        confirm =
                                            runBlocking { getString(R.string.change) } to { state ->
                                                viewModel.setProxy(
                                                    proxyType,
                                                    state.textField?.value ?: "",
                                                    proxyPort,
                                                )
                                            },
                                        dismiss = runBlocking { getString(R.string.cancel) },
                                    ),
                                )
                            },
                        )
                        SettingItem(
                            title = stringResource(R.string.proxy_port),
                            subtitle = proxyPort.toString(),
                            onClick = {
                                viewModel.setAlertData(
                                    SettingAlertState(
                                        title = runBlocking { getString(R.string.proxy_port) },
                                        message = runBlocking { getString(R.string.proxy_port_message) },
                                        textField =
                                            SettingAlertState.TextFieldData(
                                                label = runBlocking { getString(R.string.proxy_port) },
                                                value = proxyPort.toString(),
                                                verifyCodeBlock = {
                                                    (it.toIntOrNull() != null) to runBlocking { getString(R.string.invalid_port) }
                                                },
                                            ),
                                        confirm =
                                            runBlocking { getString(R.string.change) } to { state ->
                                                viewModel.setProxy(
                                                    proxyType,
                                                    proxyHost,
                                                    state.textField?.value?.toIntOrNull() ?: 0,
                                                )
                                            },
                                        dismiss = runBlocking { getString(R.string.cancel) },
                                    ),
                                )
                            },
                        )
                        SettingItem(
                            title = stringResource(R.string.proxy_username),
                            subtitle = proxyUsername,
                            onClick = {
                                viewModel.setAlertData(
                                    SettingAlertState(
                                        title = runBlocking { getString(R.string.proxy_username) },
                                        message = runBlocking { getString(R.string.proxy_username_message) },
                                        textField =
                                            SettingAlertState.TextFieldData(
                                                label = runBlocking { getString(R.string.proxy_username) },
                                                value = proxyUsername,
                                            ),
                                        confirm =
                                            runBlocking { getString(R.string.change) } to { state ->
                                                viewModel.setProxyCredentials(
                                                    state.textField?.value ?: "",
                                                    proxyPassword,
                                                )
                                            },
                                        dismiss = runBlocking { getString(R.string.cancel) },
                                    ),
                                )
                            },
                        )
                        SettingItem(
                            title = stringResource(R.string.proxy_password),
                            subtitle =
                                if (proxyPassword.isEmpty()) {
                                    ""
                                } else {
                                    "\u2022".repeat(proxyPassword.length)
                                },
                            onClick = {
                                viewModel.setAlertData(
                                    SettingAlertState(
                                        title = runBlocking { getString(R.string.proxy_password) },
                                        message = runBlocking { getString(R.string.proxy_password_message) },
                                        textField =
                                            SettingAlertState.TextFieldData(
                                                label = runBlocking { getString(R.string.proxy_password) },
                                                value = proxyPassword,
                                            ),
                                        confirm =
                                            runBlocking { getString(R.string.change) } to { state ->
                                                viewModel.setProxyCredentials(
                                                    proxyUsername,
                                                    state.textField?.value ?: "",
                                                )
                                            },
                                        dismiss = runBlocking { getString(R.string.cancel) },
                                    ),
                                )
                            },
                        )
                    }
                }
            }
        }
        if (getPlatform() == Platform.Android) {
            item(key = "audio") {
                Column {
                    Text(
                        text = stringResource(R.string.audio),
                        style = typo().labelMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    SettingItem(
                        title = stringResource(R.string.normalize_volume),
                        subtitle = stringResource(R.string.balance_media_loudness),
                        switch = (normalizeVolume to { viewModel.setNormalizeVolume(it) }),
                    )
                    SettingItem(
                        title = stringResource(R.string.skip_silent),
                        subtitle = stringResource(R.string.skip_no_music_part),
                        switch = (skipSilent to { viewModel.setSkipSilent(it) }),
                    )
                    val equalizerTypeLabels =
                        listOf(
                            DataStoreManager.EQUALIZER_TYPE_BUILT_IN to stringResource(R.string.equalizer_type_built_in),
                            DataStoreManager.EQUALIZER_TYPE_SYSTEM to stringResource(R.string.equalizer_type_system),
                        )
                    SettingItem(
                        title = stringResource(R.string.equalizer_type),
                        subtitle = equalizerTypeLabels.firstOrNull { it.first == equalizerType }?.second ?: "",
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = runBlocking { getString(R.string.equalizer_type) },
                                    selectOne =
                                        SettingAlertState.SelectData(
                                            listSelect = equalizerTypeLabels.map { (it.first == equalizerType) to it.second },
                                        ),
                                    confirm =
                                        runBlocking { getString(R.string.change) } to { state ->
                                            val selected = state.selectOne?.getSelected()
                                            equalizerTypeLabels.firstOrNull { it.second == selected }?.first?.let {
                                                viewModel.setEqualizerType(it)
                                            }
                                        },
                                    dismiss = runBlocking { getString(R.string.cancel) },
                                ),
                            )
                        },
                    )
                    AnimatedVisibility(visible = equalizerType == DataStoreManager.EQUALIZER_TYPE_SYSTEM) {
                        SettingItem(
                            title = stringResource(R.string.open_system_equalizer),
                            subtitle =
                                if (castState.isRemote) {
                                    stringResource(R.string.not_available_while_casting)
                                } else {
                                    stringResource(R.string.use_your_system_equalizer)
                                },
                            isEnable = !castState.isRemote,
                            onClick = {
                                coroutineScope.launch {
                                    resultLauncher.launch()
                                }
                            },
                        )
                    }
                }
            }
        }
        item(key = "playback") {
            Column {
                Text(
                    text = stringResource(R.string.playback),
                    style = typo().labelMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                // Under Playback rather than Audio because that whole group sits inside an
                // Android-only branch — "Open system equalizer" is an Android feature — and this
                // one is on both platforms: mpv's `af` chain on Desktop, an AudioProcessor in the
                // Media3 sink on Android, driven from the same stored curve.
                AnimatedVisibility(visible = getPlatform() != Platform.Android || equalizerType != DataStoreManager.EQUALIZER_TYPE_SYSTEM) {
                    Column {
                        SettingItem(
                            title = stringResource(R.string.equalizer),
                            subtitle = stringResource(R.string.equalizer_description),
                            smallSubtitle = true,
                            switch = (equalizerEnabled to { viewModel.setEqualizerEnabled(it) }),
                        )
                        // Only while on. A curve that visibly does nothing is worse than no curve —
                        // and the stored bands survive the switch, so turning it back on returns to
                        // the shape the user built rather than to flat.
                        AnimatedVisibility(visible = equalizerEnabled) {
                            EqualizerSection()
                        }
                    }
                }
                // Beside the equalizer rather than in its own group: all three are the same kind of
                // thing — one stored setting reshaping the audio on both backends — and a user
                // hunting for "reverb" looks wherever the sound settings are, not under a heading
                // they have to guess.
                SettingItem(
                    title = stringResource(R.string.audio_delay),
                    subtitle = stringResource(R.string.audio_delay_description),
                    smallSubtitle = true,
                    switch = (delayEnabled to { viewModel.setDelayEnabled(it) }),
                )
                // Only while on, like the curve — and the three values survive the switch, so
                // turning it back on returns to the echo the user dialled in.
                AnimatedVisibility(visible = delayEnabled) {
                    DelaySection()
                }
                SettingItem(
                    title = stringResource(R.string.audio_reverb),
                    subtitle = stringResource(R.string.audio_reverb_description),
                    smallSubtitle = true,
                    switch = (reverbEnabled to { viewModel.setReverbEnabled(it) }),
                )
                // Same again: the room and the wet level outlive the switch.
                AnimatedVisibility(visible = reverbEnabled) {
                    ReverbSection()
                }
                SettingItem(
                    title = stringResource(R.string.save_playback_state),
                    subtitle = stringResource(R.string.save_shuffle_and_repeat_mode),
                    switch = (savePlaybackState to { viewModel.setSavedPlaybackState(it) }),
                )
                if (getPlatform() == Platform.Android) {
                    SettingItem(
                        title = stringResource(R.string.kill_service_on_exit),
                        subtitle = stringResource(R.string.kill_service_on_exit_description),
                        switch = (killServiceOnExit to { viewModel.setKillServiceOnExit(it) }),
                    )
                }
            }
        }
        // Crossfade Settings (all platforms)
        item(key = "crossfade_settings") {
            Column {
                SettingItem(
                    title = stringResource(R.string.crossfade),
                    subtitle =
                        if (castState.isRemote) {
                            stringResource(R.string.not_available_while_casting)
                        } else {
                            stringResource(R.string.crossfade_description)
                        },
                    smallSubtitle = true,
                    switch = (crossfadeEnabled to { viewModel.setCrossfadeEnabled(it) }),
                    isEnable = !castState.isRemote,
                )
                AnimatedVisibility(visible = crossfadeEnabled) {
                    Column {
                        SettingItem(
                            title = stringResource(R.string.crossfade_duration),
                            subtitle =
                                if (castState.isRemote) {
                                    stringResource(R.string.not_available_while_casting)
                                } else if (crossfadeDuration == DataStoreManager.CROSSFADE_DURATION_AUTO) {
                                    stringResource(R.string.crossfade_auto)
                                } else {
                                    "${crossfadeDuration / 1000}s"
                                },
                            isEnable = !castState.isRemote,
                            onClick = {
                                viewModel.setAlertData(
                                    SettingAlertState(
                                        title = runBlocking { getString(R.string.crossfade_duration) },
                                        selectOne =
                                            SettingAlertState.SelectData(
                                                listSelect =
                                                    listOf(
                                                        (crossfadeDuration == DataStoreManager.CROSSFADE_DURATION_AUTO) to
                                                            runBlocking { getString(R.string.crossfade_auto) },
                                                        (crossfadeDuration == 1000) to "1s",
                                                        (crossfadeDuration == 2000) to "2s",
                                                        (crossfadeDuration == 3000) to "3s",
                                                        (crossfadeDuration == 5000) to "5s",
                                                        (crossfadeDuration == 8000) to "8s",
                                                        (crossfadeDuration == 10000) to "10s",
                                                        (crossfadeDuration == 12000) to "12s",
                                                        (crossfadeDuration == 15000) to "15s",
                                                        (crossfadeDuration == 20000) to "20s",
                                                        (crossfadeDuration == 30000) to "30s",
                                                    ),
                                            ),
                                        confirm =
                                            runBlocking { getString(R.string.change) } to { state ->
                                                val duration =
                                                    when (state.selectOne?.getSelected()) {
                                                        runBlocking {
                                                            getString(
                                                                R.string.crossfade_auto,
                                                            )
                                                        },
                                                        -> DataStoreManager.CROSSFADE_DURATION_AUTO
                                                        "1s" -> 1000
                                                        "2s" -> 2000
                                                        "3s" -> 3000
                                                        "5s" -> 5000
                                                        "8s" -> 8000
                                                        "10s" -> 10000
                                                        "12s" -> 12000
                                                        "15s" -> 15000
                                                        "20s" -> 20000
                                                        "30s" -> 30000
                                                        else -> 5000
                                                    }
                                                viewModel.setCrossfadeDuration(duration)
                                            },
                                        dismiss = runBlocking { getString(R.string.cancel) },
                                    ),
                                )
                            },
                        )
//                        if (getPlatform() == Platform.Android) {
                        SettingItem(
                            title = stringResource(R.string.crossfade_dj_mode),
                            subtitle =
                                if (castState.isRemote) {
                                    stringResource(R.string.not_available_while_casting)
                                } else {
                                    stringResource(R.string.crossfade_dj_mode_description)
                                },
                            smallSubtitle = true,
                            switch = ((crossfadeDjMode) to { viewModel.setCrossfadeDjMode(it) }),
                            isEnable = !castState.isRemote,
                        )
                        SettingItem(
                            title = stringResource(R.string.crossfade_skip_album),
                            subtitle =
                                if (castState.isRemote) {
                                    stringResource(R.string.not_available_while_casting)
                                } else {
                                    stringResource(R.string.crossfade_skip_album_description)
                                },
                            smallSubtitle = true,
                            switch = ((crossfadeSkipAlbum) to { viewModel.setCrossfadeSkipAlbum(it) }),
                            isEnable = !castState.isRemote,
                        )
//                        }
                    }
                }
            }
        }
        // Deliberately not part of "storage" further down, which is Android-only: tracking and the
        // rows it leaves behind exist on Desktop just the same. The switch that produces the history
        // and the button that erases it belong together.
        item(key = "listening_history") {
            Column {
                Text(
                    text = stringResource(R.string.listening_history),
                    style = typo().labelMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                SettingItem(
                    title = stringResource(R.string.local_tracking_title),
                    subtitle = stringResource(R.string.local_tracking_description),
                    switch = (localTrackingEnabled to { viewModel.setLocalTrackingEnabled(it) }),
                )
                SettingItem(
                    title = stringResource(R.string.clear_listening_history),
                    subtitle = stringResource(R.string.clear_listening_history_description),
                    onClick = {
                        viewModel.setBasicAlertData(
                            SettingBasicAlertState(
                                title = runBlocking { getString(R.string.clear_listening_history) },
                                message = runBlocking { getString(R.string.clear_listening_history_confirm) },
                                confirm =
                                    runBlocking { getString(R.string.clear) } to {
                                        viewModel.clearListeningHistory()
                                    },
                                dismiss = runBlocking { getString(R.string.cancel) },
                            ),
                        )
                    },
                )
            }
        }
        item(key = "lyrics") {
            Column {
                Text(
                    text = stringResource(R.string.lyrics),
                    style = typo().labelMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                SettingItem(
                    title = stringResource(R.string.main_lyrics_provider),
                    subtitle =
                        when (mainLyricsProvider) {
                            DataStoreManager.SIMPMUSIC -> stringResource(R.string.simpmusic_lyrics)
                            DataStoreManager.YOUTUBE -> stringResource(R.string.youtube_transcript)
                            DataStoreManager.LRCLIB -> stringResource(R.string.lrclib)
                            DataStoreManager.BETTER_LYRICS -> stringResource(R.string.better_lyrics)
                            else -> stringResource(R.string.unknown)
                        },
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(R.string.main_lyrics_provider) },
                                selectOne =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            listOf(
                                                (mainLyricsProvider == DataStoreManager.SIMPMUSIC) to
                                                    runBlocking { getString(R.string.simpmusic_lyrics) },
                                                (mainLyricsProvider == DataStoreManager.YOUTUBE) to
                                                    runBlocking { getString(R.string.youtube_transcript) },
                                                (mainLyricsProvider == DataStoreManager.LRCLIB) to runBlocking { getString(R.string.lrclib) },
                                                (mainLyricsProvider == DataStoreManager.BETTER_LYRICS) to
                                                    runBlocking { getString(R.string.better_lyrics) },
                                            ),
                                    ),
                                confirm =
                                    runBlocking { getString(R.string.change) } to { state ->
                                        viewModel.setLyricsProvider(
                                            when (state.selectOne?.getSelected()) {
                                                runBlocking { getString(R.string.simpmusic_lyrics) } -> DataStoreManager.SIMPMUSIC
                                                runBlocking { getString(R.string.youtube_transcript) } -> DataStoreManager.YOUTUBE
                                                runBlocking { getString(R.string.lrclib) } -> DataStoreManager.LRCLIB
                                                runBlocking { getString(R.string.better_lyrics) } -> DataStoreManager.BETTER_LYRICS
                                                else -> DataStoreManager.SIMPMUSIC
                                            },
                                        )
                                    },
                                dismiss = runBlocking { getString(R.string.cancel) },
                            ),
                        )
                    },
                )

                SettingItem(
                    title = stringResource(R.string.lyrics_offset),
                    subtitle =
                        stringResource(
                            R.string.lyrics_offset_value,
                            if (lyricsOffsetMs > 0) "+$lyricsOffsetMs" else lyricsOffsetMs.toString(),
                        ),
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(R.string.lyrics_offset) },
                                // The dialog renders its text field INSIDE the `message != null`
                                // branch, so a state carrying a textField and no message opens an
                                // empty box with no error anywhere.
                                message = runBlocking { getString(R.string.lyrics_offset_message) },
                                textField =
                                    SettingAlertState.TextFieldData(
                                        label = runBlocking { getString(R.string.lyrics_offset) },
                                        value = lyricsOffsetMs.toString(),
                                        // Only that it is a whole number — no range. How far a
                                        // listener's own audio path lags is theirs to say.
                                        verifyCodeBlock = {
                                            (it.trim().toIntOrNull() != null) to
                                                runBlocking { getString(R.string.lyrics_offset_invalid) }
                                        },
                                    ),
                                confirm =
                                    runBlocking { getString(R.string.change) } to { state ->
                                        state.textField
                                            ?.value
                                            ?.trim()
                                            ?.toIntOrNull()
                                            ?.let { viewModel.setLyricsOffsetMs(it) }
                                    },
                                dismiss = runBlocking { getString(R.string.cancel) },
                            ),
                        )
                    },
                )

                SettingItem(
                    title = stringResource(R.string.translation_language),
                    subtitle = translationLanguage ?: "",
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(R.string.translation_language) },
                                textField =
                                    SettingAlertState.TextFieldData(
                                        label = runBlocking { getString(R.string.translation_language) },
                                        value = translationLanguage ?: "",
                                        verifyCodeBlock = {
                                            (it.length == 2 && it.isTwoLetterCode()) to
                                                runBlocking { getString(R.string.invalid_language_code) }
                                        },
                                    ),
                                message = runBlocking { getString(R.string.translation_language_message) },
                                confirm =
                                    runBlocking { getString(R.string.change) } to { state ->
                                        viewModel.setTranslationLanguage(state.textField?.value ?: "")
                                    },
                                dismiss = runBlocking { getString(R.string.cancel) },
                            ),
                        )
                    },
                    isEnable = true,
                )
                SettingItem(
                    title = stringResource(R.string.youtube_subtitle_language),
                    subtitle = youtubeSubtitleLanguage,
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(R.string.youtube_subtitle_language) },
                                textField =
                                    SettingAlertState.TextFieldData(
                                        label = runBlocking { getString(R.string.youtube_subtitle_language) },
                                        value = youtubeSubtitleLanguage,
                                        verifyCodeBlock = {
                                            (it.length == 2 && it.isTwoLetterCode()) to
                                                runBlocking { getString(R.string.invalid_language_code) }
                                        },
                                    ),
                                message = runBlocking { getString(R.string.youtube_subtitle_language_message) },
                                confirm =
                                    runBlocking { getString(R.string.change) } to { state ->
                                        viewModel.setYoutubeSubtitleLanguage(state.textField?.value ?: "")
                                    },
                                dismiss = runBlocking { getString(R.string.cancel) },
                            ),
                        )
                    },
                )
                SettingItem(
                    title = stringResource(R.string.help_build_lyrics_database),
                    subtitle = stringResource(R.string.help_build_lyrics_database_description),
                    switch = (helpBuildLyricsDatabase to { viewModel.setHelpBuildLyricsDatabase(it) }),
                )
                SettingItem(
                    title = stringResource(R.string.contributor_name),
                    subtitle = contributor.first.ifEmpty { stringResource(R.string.anonymous) },
                    isEnable = helpBuildLyricsDatabase,
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(R.string.contributor_name) },
                                textField =
                                    SettingAlertState.TextFieldData(
                                        label = runBlocking { getString(R.string.contributor_name) },
                                        value = "",
                                    ),
                                message = "",
                                confirm =
                                    runBlocking { getString(R.string.set) } to { state ->
                                        viewModel.setContributorName(state.textField?.value ?: "")
                                    },
                                dismiss = runBlocking { getString(R.string.cancel) },
                            ),
                        )
                    },
                )
                SettingItem(
                    title = stringResource(R.string.contributor_email),
                    subtitle = contributor.second.ifEmpty { stringResource(R.string.anonymous) },
                    isEnable = helpBuildLyricsDatabase,
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(R.string.contributor_email) },
                                textField =
                                    SettingAlertState.TextFieldData(
                                        label = runBlocking { getString(R.string.contributor_email) },
                                        value = "",
                                        verifyCodeBlock = {
                                            if (it.isNotEmpty()) {
                                                (it.contains("@")) to runBlocking { getString(R.string.invalid) }
                                            } else {
                                                true to ""
                                            }
                                        },
                                    ),
                                message = "",
                                confirm =
                                    runBlocking { getString(R.string.set) } to { state ->
                                        viewModel.setContributorEmail(state.textField?.value ?: "")
                                    },
                                dismiss = runBlocking { getString(R.string.cancel) },
                            ),
                        )
                    },
                )
            }
        }
        item(key = "AI") {
            Column {
                Text(
                    text = stringResource(R.string.ai),
                    style = typo().labelMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                SettingItem(
                    title = stringResource(R.string.ai_provider),
                    subtitle =
                        when (aiProvider) {
                            DataStoreManager.AI_PROVIDER_OPENAI -> stringResource(R.string.openai)
                            DataStoreManager.AI_PROVIDER_GEMINI -> stringResource(R.string.gemini)
                            DataStoreManager.AI_PROVIDER_CUSTOM_OPENAI -> stringResource(R.string.openai_api_compatible)
                            else -> stringResource(R.string.unknown)
                        },
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(R.string.ai_provider) },
                                selectOne =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            listOf(
                                                (mainLyricsProvider == DataStoreManager.AI_PROVIDER_OPENAI) to
                                                    runBlocking { getString(R.string.openai) },
                                                (mainLyricsProvider == DataStoreManager.AI_PROVIDER_GEMINI) to
                                                    runBlocking { getString(R.string.gemini) },
                                                (mainLyricsProvider == DataStoreManager.AI_PROVIDER_CUSTOM_OPENAI) to
                                                    runBlocking { getString(R.string.openai_api_compatible) },
                                            ),
                                    ),
                                confirm =
                                    runBlocking { getString(R.string.change) } to { state ->
                                        viewModel.setAIProvider(
                                            when (state.selectOne?.getSelected()) {
                                                runBlocking { getString(R.string.openai) } -> DataStoreManager.AI_PROVIDER_OPENAI
                                                runBlocking { getString(R.string.gemini) } -> DataStoreManager.AI_PROVIDER_GEMINI
                                                runBlocking {
                                                    getString(
                                                        R.string.openai_api_compatible,
                                                    )
                                                },
                                                -> DataStoreManager.AI_PROVIDER_CUSTOM_OPENAI

                                                else -> DataStoreManager.AI_PROVIDER_OPENAI
                                            },
                                        )
                                    },
                                dismiss = runBlocking { getString(R.string.cancel) },
                            ),
                        )
                    },
                )
                SettingItem(
                    title = stringResource(R.string.ai_api_key),
                    subtitle = if (isHasApiKey) "XXXXXXXXXX" else "N/A",
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(R.string.ai_api_key) },
                                textField =
                                    SettingAlertState.TextFieldData(
                                        label = runBlocking { getString(R.string.ai_api_key) },
                                        value = "",
                                        verifyCodeBlock = {
                                            (it.isNotEmpty()) to runBlocking { getString(R.string.invalid_api_key) }
                                        },
                                    ),
                                message = "",
                                confirm =
                                    runBlocking { getString(R.string.set) } to { state ->
                                        viewModel.setAIApiKey(state.textField?.value ?: "")
                                    },
                                dismiss = runBlocking { getString(R.string.cancel) },
                            ),
                        )
                    },
                )
                SettingItem(
                    title = stringResource(R.string.custom_ai_model_id),
                    subtitle = customModelId.ifEmpty { stringResource(R.string.default_models) },
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(R.string.custom_ai_model_id) },
                                textField =
                                    SettingAlertState.TextFieldData(
                                        label = runBlocking { getString(R.string.custom_ai_model_id) },
                                        value = "",
                                        verifyCodeBlock = {
                                            (it.isNotEmpty() && !it.contains(" ")) to runBlocking { getString(R.string.invalid) }
                                        },
                                    ),
                                message = runBlocking { getString(R.string.custom_model_id_messages) },
                                confirm =
                                    runBlocking { getString(R.string.set) } to { state ->
                                        viewModel.setCustomModelId(state.textField?.value ?: "")
                                    },
                                dismiss = runBlocking { getString(R.string.cancel) },
                            ),
                        )
                    },
                )
                // Custom OpenAI Base URL - only show when Custom OpenAI is selected
                if (aiProvider == DataStoreManager.AI_PROVIDER_CUSTOM_OPENAI) {
                    SettingItem(
                        title = "Custom Base URL",
                        subtitle = customOpenAIBaseUrl.ifEmpty { "https://api.openai.com/v1/" },
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = "Custom Base URL",
                                    textField =
                                        SettingAlertState.TextFieldData(
                                            label = "Base URL",
                                            value = customOpenAIBaseUrl,
                                            verifyCodeBlock = {
                                                (it.isEmpty() || it.startsWith("http")) to "Invalid URL format"
                                            },
                                        ),
                                    message = "Enter OpenAI-compatible API base URL (e.g., https://api.openai.com/v1/)",
                                    confirm =
                                        runBlocking { getString(R.string.set) } to { state ->
                                            viewModel.setCustomOpenAIBaseUrl(state.textField?.value ?: "")
                                        },
                                    dismiss = runBlocking { getString(R.string.cancel) },
                                ),
                            )
                        },
                    )
                    SettingItem(
                        title = "Custom Headers",
                        subtitle = if (customOpenAIHeaders.isNotEmpty()) "Configured" else "Not set",
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = "Custom Headers (JSON)",
                                    textField =
                                        SettingAlertState.TextFieldData(
                                            label = "Headers JSON",
                                            value = customOpenAIHeaders,
                                            verifyCodeBlock = { input ->
                                                if (input.isEmpty()) {
                                                    true to null
                                                } else {
                                                    try {
                                                        // Simple validation: check if it looks like JSON
                                                        val trimmed = input.trim()
                                                        (trimmed.startsWith("{") && trimmed.endsWith("}")) to "Invalid JSON format"
                                                    } catch (e: Exception) {
                                                        false to "Invalid JSON format"
                                                    }
                                                }
                                            },
                                        ),
                                    message = "Enter custom headers in JSON format:\n{\"key1\":\"value1\",\"key2\":\"value2\"}",
                                    confirm =
                                        runBlocking { getString(R.string.set) } to { state ->
                                            viewModel.setCustomOpenAIHeaders(state.textField?.value ?: "")
                                        },
                                    dismiss = runBlocking { getString(R.string.cancel) },
                                ),
                            )
                        },
                    )
                }
                SettingItem(
                    title = stringResource(R.string.use_ai_translation),
                    subtitle = stringResource(R.string.use_ai_translation_description),
                    switch = (useAITranslation to { viewModel.setAITranslation(it) }),
                    isEnable = isHasApiKey,
                )
            }
        }
        item(key = "spotify") {
            Column {
                Text(
                    text = stringResource(R.string.spotify),
                    style = typo().labelMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                SettingItem(
                    // The title follows the state: a row that still reads "Log in" while logged in
                    // gives no clue that tapping it signs you out.
                    title =
                        if (spotifyLoggedIn) {
                            stringResource(R.string.log_out_from_spotify)
                        } else {
                            stringResource(R.string.log_in_to_spotify)
                        },
                    subtitle =
                        if (spotifyLoggedIn) {
                            stringResource(R.string.logged_in)
                        } else {
                            stringResource(R.string.intro_login_to_spotify)
                        },
                    onClick = {
                        if (spotifyLoggedIn) {
                            viewModel.confirmLogOut(
                                confirmLabel = runBlocking { getString(R.string.log_out_from_spotify) },
                            ) { viewModel.setSpotifyLogIn(false) }
                        } else {
                            navController.navigate(SpotifyLoginDestination)
                        }
                    },
                )
                SettingItem(
                    title = stringResource(R.string.enable_spotify_lyrics),
                    subtitle = stringResource(R.string.spotify_lyrícs_info),
                    switch = (spotifyLyrics to { viewModel.setSpotifyLyrics(it) }),
                    isEnable = spotifyLoggedIn,
                )
                SettingItem(
                    title = stringResource(R.string.enable_canvas),
                    subtitle = stringResource(R.string.canvas_info),
                    switch = (spotifyCanvas to { viewModel.setSpotifyCanvas(it) }),
                    isEnable = spotifyLoggedIn,
                )
                // Sits with the canvas because it replaces it, but carries no isEnable: the two
                // rows above need a Spotify session and this one needs no account at all, so
                // gating it on spotifyLoggedIn would lock it away from the users it works for.
                SettingItem(
                    title = stringResource(R.string.enable_animated_artwork),
                    subtitle = stringResource(R.string.animated_artwork_info),
                    switch = (amAnimatedArtwork to { viewModel.setAMAnimatedArtwork(it) }),
                )
            }
        }
        item(key = "sponsor_block") {
            Column {
                Text(
                    text = stringResource(R.string.sponsorBlock),
                    style = typo().labelMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                SettingItem(
                    title = stringResource(R.string.enable_sponsor_block),
                    subtitle = stringResource(R.string.skip_sponsor_part_of_video),
                    switch = (enableSponsorBlock to { viewModel.setSponsorBlockEnabled(it) }),
                )
                val listName =
                    SponsorBlockType.toList().map { it.displayString() }
                SettingItem(
                    title = stringResource(R.string.categories_sponsor_block),
                    subtitle = stringResource(R.string.what_segments_will_be_skipped),
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(R.string.categories_sponsor_block) },
                                multipleSelect =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            listName
                                                .mapIndexed { index, item ->
                                                    (
                                                        skipSegments?.contains(
                                                            SponsorBlockType.toList().getOrNull(index)?.value,
                                                        ) == true
                                                    ) to item
                                                }.also {
                                                    Logger.w("SettingScreen", "SettingAlertState: $skipSegments")
                                                    Logger.w("SettingScreen", "SettingAlertState: $it")
                                                },
                                    ),
                                confirm =
                                    runBlocking { getString(R.string.save) } to { state ->
                                        viewModel.setSponsorBlockCategories(
                                            state.multipleSelect
                                                ?.getListSelected()
                                                ?.map { selected ->
                                                    listName.indexOf(selected)
                                                }?.mapNotNull { s ->
                                                    SponsorBlockType.toList().getOrNull(s).let {
                                                        it?.value
                                                    }
                                                }?.toCollection(ArrayList()) ?: arrayListOf(),
                                        )
                                    },
                                dismiss = runBlocking { getString(R.string.cancel) },
                            ),
                        )
                    },
                    isEnable = enableSponsorBlock,
                )
                val beforeUrl = stringResource(R.string.sponsor_block_intro).substringBefore("https://sponsor.ajay.app/")
                val afterUrl = stringResource(R.string.sponsor_block_intro).substringAfter("https://sponsor.ajay.app/")
                Text(
                    buildAnnotatedString {
                        append(beforeUrl)
                        withLink(
                            LinkAnnotation.Url(
                                "https://sponsor.ajay.app/",
                                TextLinkStyles(style = SpanStyle(color = MaterialTheme.colorScheme.primary)),
                            ),
                        ) {
                            append("https://sponsor.ajay.app/")
                        }
                        append(afterUrl)
                    },
                    style = typo().bodySmall,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                )
            }
        }
        if (getPlatform() == Platform.Android) {
            item(key = "storage") {
                Column {
                    Text(
                        text = stringResource(R.string.storage),
                        style = typo().labelMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    SettingItem(
                        title = stringResource(R.string.player_cache),
                        subtitle = "${playerCache.bytesToMB()} MB",
                        onClick = {
                            viewModel.setBasicAlertData(
                                SettingBasicAlertState(
                                    title = runBlocking { getString(R.string.clear_player_cache) },
                                    message = null,
                                    confirm =
                                        runBlocking { getString(R.string.clear) } to {
                                            viewModel.clearPlayerCache()
                                        },
                                    dismiss = runBlocking { getString(R.string.cancel) },
                                ),
                            )
                        },
                    )
                    SettingItem(
                        title = stringResource(R.string.downloaded_cache),
                        subtitle = "${downloadedCache.bytesToMB()} MB",
                        onClick = {
                            viewModel.setBasicAlertData(
                                SettingBasicAlertState(
                                    title = runBlocking { getString(R.string.clear_downloaded_cache) },
                                    message = null,
                                    confirm =
                                        runBlocking { getString(R.string.clear) } to {
                                            viewModel.clearDownloadedCache()
                                        },
                                    dismiss = runBlocking { getString(R.string.cancel) },
                                ),
                            )
                        },
                    )
                    SettingItem(
                        title = stringResource(R.string.thumbnail_cache),
                        subtitle = "${thumbnailCache.bytesToMB()} MB",
                        onClick = {
                            viewModel.setBasicAlertData(
                                SettingBasicAlertState(
                                    title = runBlocking { getString(R.string.clear_thumbnail_cache) },
                                    message = null,
                                    confirm =
                                        runBlocking { getString(R.string.clear) } to {
                                            viewModel.clearThumbnailCache(platformContext)
                                        },
                                    dismiss = runBlocking { getString(R.string.cancel) },
                                ),
                            )
                        },
                    )
                    SettingItem(
                        title = stringResource(R.string.spotify_canvas_cache),
                        subtitle = "${canvasCache.bytesToMB()} MB",
                        onClick = {
                            viewModel.setBasicAlertData(
                                SettingBasicAlertState(
                                    title = runBlocking { getString(R.string.clear_canvas_cache) },
                                    message = null,
                                    confirm =
                                        runBlocking { getString(R.string.clear) } to {
                                            viewModel.clearCanvasCache()
                                        },
                                    dismiss = runBlocking { getString(R.string.cancel) },
                                ),
                            )
                        },
                    )
                    SettingItem(
                        title = stringResource(R.string.limit_player_cache),
                        subtitle = LIMIT_CACHE_SIZE.getItemFromData(limitPlayerCache).toString(),
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = runBlocking { getString(R.string.limit_player_cache) },
                                    selectOne =
                                        SettingAlertState.SelectData(
                                            listSelect =
                                                LIMIT_CACHE_SIZE.items.map { item ->
                                                    (item == LIMIT_CACHE_SIZE.getItemFromData(limitPlayerCache)) to item.toString()
                                                },
                                        ),
                                    confirm =
                                        runBlocking { getString(R.string.change) } to { state ->
                                            viewModel.setPlayerCacheLimit(
                                                LIMIT_CACHE_SIZE.getDataFromItem(state.selectOne?.getSelected()),
                                            )
                                        },
                                    dismiss = runBlocking { getString(R.string.cancel) },
                                ),
                            )
                        },
                    )
                    Box(
                        Modifier.padding(
                            horizontal = 24.dp,
                            vertical = 16.dp,
                        ),
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(0.dp),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .onGloballyPositioned { layoutCoordinates ->
                                        with(localDensity) {
                                            width =
                                                layoutCoordinates.size.width
                                                    .toDp()
                                                    .value
                                                    .toInt()
                                        }
                                    },
                        ) {
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                (fraction.otherApp * width).dp,
                                            ).background(
                                                md_theme_dark_primary,
                                            ).fillMaxHeight(),
                                )
                            }
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                (fraction.downloadCache * width).dp,
                                            ).background(
                                                Color(0xD540FF17),
                                            ).fillMaxHeight(),
                                )
                            }
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                (fraction.playerCache * width).dp,
                                            ).background(
                                                Color(0xD5FFFF00),
                                            ).fillMaxHeight(),
                                )
                            }
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                (fraction.canvasCache * width).dp,
                                            ).background(
                                                Color.Cyan,
                                            ).fillMaxHeight(),
                                )
                            }
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                (fraction.thumbCache * width).dp,
                                            ).background(
                                                Color.Magenta,
                                            ).fillMaxHeight(),
                                )
                            }
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                (fraction.appDatabase * width).dp,
                                            ).background(
                                                Color.White,
                                            ),
                                )
                            }
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                (fraction.freeSpace * width).dp,
                                            ).background(
                                                Color.DarkGray,
                                            ).fillMaxHeight(),
                                )
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    md_theme_dark_primary,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(R.string.other_app), style = typo().bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.Green,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(R.string.downloaded_cache), style = typo().bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.Yellow,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(R.string.player_cache), style = typo().bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.Cyan,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(R.string.spotify_canvas_cache), style = typo().bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.Magenta,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(R.string.thumbnail_cache), style = typo().bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.White,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(R.string.database), style = typo().bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.LightGray,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(R.string.free_space), style = typo().bodySmall)
                    }
                }
            }
        }
        item(key = "backup") {
            Column {
                Text(
                    text = stringResource(R.string.backup),
                    style = typo().labelMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                SettingItem(
                    title = stringResource(R.string.backup_downloaded),
                    subtitle = stringResource(R.string.backup_downloaded_description),
                    switch = (backupDownloaded to { viewModel.setBackupDownloaded(it) }),
                )
                // Auto Backup (Android only)
                if (getPlatform() == Platform.Android) {
                    SettingItem(
                        title = stringResource(R.string.auto_backup),
                        subtitle = stringResource(R.string.auto_backup_description),
                        switch = (autoBackupEnabled to { viewModel.setAutoBackupEnabled(it) }),
                    )
                    AnimatedVisibility(visible = autoBackupEnabled) {
                        Column {
                            SettingItem(
                                title = stringResource(R.string.backup_frequency),
                                subtitle =
                                    when (autoBackupFrequency) {
                                        DataStoreManager.AUTO_BACKUP_FREQUENCY_DAILY -> stringResource(R.string.daily)
                                        DataStoreManager.AUTO_BACKUP_FREQUENCY_WEEKLY -> stringResource(R.string.weekly)
                                        DataStoreManager.AUTO_BACKUP_FREQUENCY_MONTHLY -> stringResource(R.string.monthly)
                                        else -> stringResource(R.string.daily)
                                    },
                                onClick = {
                                    viewModel.setAlertData(
                                        SettingAlertState(
                                            title = runBlocking { getString(R.string.backup_frequency) },
                                            selectOne =
                                                SettingAlertState.SelectData(
                                                    listSelect =
                                                        listOf(
                                                            (autoBackupFrequency == DataStoreManager.AUTO_BACKUP_FREQUENCY_DAILY) to
                                                                runBlocking { getString(R.string.daily) },
                                                            (autoBackupFrequency == DataStoreManager.AUTO_BACKUP_FREQUENCY_WEEKLY) to
                                                                runBlocking { getString(R.string.weekly) },
                                                            (autoBackupFrequency == DataStoreManager.AUTO_BACKUP_FREQUENCY_MONTHLY) to
                                                                runBlocking { getString(R.string.monthly) },
                                                        ),
                                                ),
                                            confirm =
                                                runBlocking { getString(R.string.change) } to { state ->
                                                    val frequency =
                                                        when (state.selectOne?.getSelected()) {
                                                            runBlocking {
                                                                getString(
                                                                    R.string.daily,
                                                                )
                                                            },
                                                            -> DataStoreManager.AUTO_BACKUP_FREQUENCY_DAILY
                                                            runBlocking {
                                                                getString(
                                                                    R.string.weekly,
                                                                )
                                                            },
                                                            -> DataStoreManager.AUTO_BACKUP_FREQUENCY_WEEKLY
                                                            runBlocking {
                                                                getString(
                                                                    R.string.monthly,
                                                                )
                                                            },
                                                            -> DataStoreManager.AUTO_BACKUP_FREQUENCY_MONTHLY
                                                            else -> DataStoreManager.AUTO_BACKUP_FREQUENCY_DAILY
                                                        }
                                                    viewModel.setAutoBackupFrequency(frequency)
                                                },
                                            dismiss = runBlocking { getString(R.string.cancel) },
                                        ),
                                    )
                                },
                            )
                            SettingItem(
                                title = stringResource(R.string.keep_backups),
                                subtitle = stringResource(R.string.keep_backups_format, "$autoBackupMaxFiles"),
                                onClick = {
                                    viewModel.setAlertData(
                                        SettingAlertState(
                                            title = runBlocking { getString(R.string.keep_backups) },
                                            selectOne =
                                                SettingAlertState.SelectData(
                                                    listSelect =
                                                        listOf(
                                                            (autoBackupMaxFiles == 3) to "3",
                                                            (autoBackupMaxFiles == 5) to "5",
                                                            (autoBackupMaxFiles == 10) to "10",
                                                            (autoBackupMaxFiles == 15) to "15",
                                                        ),
                                                ),
                                            confirm =
                                                runBlocking { getString(R.string.change) } to { state ->
                                                    val maxFiles = state.selectOne?.getSelected()?.toIntOrNull() ?: 5
                                                    viewModel.setAutoBackupMaxFiles(maxFiles)
                                                },
                                            dismiss = runBlocking { getString(R.string.cancel) },
                                        ),
                                    )
                                },
                            )
                            SettingItem(
                                title = stringResource(R.string.last_backup),
                                subtitle =
                                    if (autoBackupLastTime == 0L) {
                                        stringResource(R.string.never)
                                    } else {
                                        DateTimeFormatter
                                            .ofPattern("yyyy-MM-dd HH:mm:ss")
                                            .withZone(ZoneId.systemDefault())
                                            .format(Instant.ofEpochMilli(autoBackupLastTime))
                                    },
                            )
                        }
                    }
                }
                SettingItem(
                    title = stringResource(R.string.backup),
                    subtitle = stringResource(R.string.save_all_your_playlist_data),
                    onClick = {
                        coroutineScope.launch {
                            backupLauncher.launch()
                        }
                    },
                )
                SettingItem(
                    title = stringResource(R.string.restore_your_data),
                    subtitle = stringResource(R.string.restore_your_saved_data),
                    onClick = {
                        coroutineScope.launch {
                            restoreLauncher.launch()
                        }
                    },
                )
                SettingItem(
                    title = stringResource(R.string.import_data),
                    subtitle = stringResource(R.string.import_playlists_from_other_apps),
                    onClick = {
                        coroutineScope.launch {
                            importLauncher.launch()
                        }
                    },
                )
                val beforeUrl = stringResource(R.string.import_data_intro).substringBefore("https://www.simpmusic.org/tools")
                val afterUrl = stringResource(R.string.import_data_intro).substringAfter("https://www.simpmusic.org/tools")
                Text(
                    buildAnnotatedString {
                        append(beforeUrl)
                        withLink(
                            LinkAnnotation.Url(
                                "https://www.simpmusic.org/tools",
                                TextLinkStyles(style = SpanStyle(color = MaterialTheme.colorScheme.primary)),
                            ),
                        ) {
                            append("https://www.simpmusic.org/tools")
                        }
                        append(afterUrl)
                    },
                    style = typo().bodySmall,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                )
            }
        }
        item(key = "about_us") {
            Column {
                Text(
                    text = stringResource(R.string.about_us),
                    style = typo().labelMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                SettingItem(
                    title = stringResource(R.string.version),
                    subtitle = stringResource(R.string.version_format, VersionManager.getVersionName()),
                    onClick = {},

                )
                SettingItem(
                    title = stringResource(R.string.auto_check_for_update),
                    subtitle = stringResource(R.string.auto_check_for_update_description),
                    switch = (autoCheckUpdate to { viewModel.setAutoCheckUpdate(it) }),
                )
                SettingItem(
                    title = stringResource(R.string.update_channel),
                    subtitle =
                        if (updateChannel == DataStoreManager.FDROID) {
                            "F-Droid"
                        } else {
                            "SimpMusic GitHub Release"
                        },
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(R.string.update_channel) },
                                selectOne =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            listOf(
                                                (updateChannel == DataStoreManager.FDROID) to "F-Droid",
                                                (updateChannel == DataStoreManager.GITHUB) to "SimpMusic GitHub Release",
                                            ),
                                    ),
                                confirm =
                                    runBlocking { getString(R.string.change) } to { state ->
                                        viewModel.setUpdateChannel(
                                            when (state.selectOne?.getSelected()) {
                                                "F-Droid" -> DataStoreManager.FDROID
                                                "SimpMusic GitHub Release" -> DataStoreManager.GITHUB
                                                else -> DataStoreManager.GITHUB
                                            },
                                        )
                                    },
                                dismiss = runBlocking { getString(R.string.cancel) },
                            ),
                        )
                    },
                )
                SettingItem(
                    title = stringResource(R.string.check_for_update),
                    subtitle = checkForUpdateSubtitle,
                    onClick = {
                        sharedViewModel.checkForUpdate()
                    },
                )
                SettingItem(
                    title = stringResource(R.string.author),
                    subtitle = stringResource(R.string.maxrave_dev),
                    onClick = {
                        uriHandler.openUri("https://github.com/maxrave-dev")
                    },
                )
                SettingItem(
                    title = stringResource(R.string.developer_blog),
                    subtitle = stringResource(R.string.developer_blog_tagline),
                    onClick = {
                        uriHandler.openUri("https://maxrave.dev")
                    },
                )
                if (getPlatform() == Platform.Android) {
                    SettingItem(
                        title = stringResource(R.string.blog_notification_title),
                        subtitle = stringResource(R.string.blog_notification_description),
                        switch = (blogNotificationEnabled to { viewModel.setBlogNotificationEnabled(it) }),
                    )
                }
                SettingItem(
                    title = stringResource(R.string.buy_me_a_coffee),
                    subtitle = stringResource(R.string.donation),
                    onClick = {
                        uriHandler.openUri("https://github.com/sponsors/maxrave-dev")
                    },
                )
                SettingItem(
                    title = stringResource(R.string.third_party_libraries),
                    subtitle = stringResource(R.string.description_and_licenses),
                    onClick = {
                        showThirdPartyLibraries = true
                    },
                )
            }
        }
        item(key = "end") {
            EndOfPage()
        }
    }
    importState?.let { progress ->
        ImportProgressDialog(
            progress = progress,
            onDismiss = importViewModel::dismiss,
        )
    }
    val showLoadingDialog by viewModel.showLoadingDialog.collectAsStateWithLifecycle()
    if (showLoadingDialog.first) {
        LoadingDialog(
            true,
            showLoadingDialog.second,
        )
    }
    val basisAlertData by viewModel.basicAlertData.collectAsStateWithLifecycle()
    if (basisAlertData != null) {
        val alertBasicState = basisAlertData ?: return
        AlertDialog(
            onDismissRequest = { viewModel.setBasicAlertData(null) },
            title = {
                Text(
                    text = alertBasicState.title,
                    style = typo().titleSmall,
                )
            },
            text = {
                if (alertBasicState.message != null) {
                    Text(text = alertBasicState.message)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        alertBasicState.confirm.second.invoke()
                        viewModel.setBasicAlertData(null)
                    },
                ) {
                    Text(text = alertBasicState.confirm.first)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.setBasicAlertData(null)
                    },
                ) {
                    Text(text = alertBasicState.dismiss)
                }
            },
        )
    }
    if (showColorPickerDialog) {
        val presetColors =
            listOf(
                "FF8ECAE6",
                "FF4C82EF",
                "FF9B72CF",
                "FFEF6C9B",
                "FFEF5350",
                "FFF4A340",
                "FFFFCA28",
                "FF66BB6A",
                "FF26A69A",
                "FFBDBDBD",
            )
        var pendingHex by rememberSaveable { mutableStateOf(customThemeColorHex.takeLast(6)) }
        val parsedColor = parseThemeColorHex(pendingHex)
        AlertDialog(
            onDismissRequest = { showColorPickerDialog = false },
            title = { Text(text = stringResource(R.string.custom_color), style = typo().titleSmall) },
            text = {
                Column {
                    presetColors.chunked(5).forEach { rowColors ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            rowColors.forEach { hex ->
                                val color = parseThemeColorHex(hex) ?: Color.Gray
                                val isSelected = pendingHex.equals(hex.takeLast(6), ignoreCase = true)
                                Box(
                                    modifier =
                                        Modifier
                                            .padding(4.dp)
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(
                                                width = if (isSelected) 3.dp else 0.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                                shape = CircleShape,
                                            ).clickable { pendingHex = hex.takeLast(6) },
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    TextField(
                        value = pendingHex,
                        onValueChange = { pendingHex = it.removePrefix("#").take(8).uppercase() },
                        label = { Text("HEX") },
                        prefix = { Text("#") },
                        singleLine = true,
                        isError = parsedColor == null,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = parsedColor != null,
                    onClick = {
                        parsedColor?.let {
                            val argb = "FF${pendingHex.takeLast(6).uppercase()}"
                            sharedViewModel.setCustomThemeColor(argb)
                            sharedViewModel.setThemeColorSource(DataStoreManager.THEME_COLOR_CUSTOM)
                        }
                        showColorPickerDialog = false
                    },
                ) { Text(text = stringResource(R.string.change)) }
            },
            dismissButton = {
                TextButton(onClick = { showColorPickerDialog = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
        )
    }
    if (showLoginSyncDialog) {
        LoginSyncDialog(onDismiss = { showLoginSyncDialog = false })
    }
    if (showYouTubeAccountDialog) {
        BasicAlertDialog(
            onDismissRequest = { },
            modifier = Modifier.wrapContentSize(),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = AlertDialogDefaults.TonalElevation,
                shadowElevation = 1.dp,
            ) {
                val googleAccounts by viewModel.googleAccounts.collectAsStateWithLifecycle(
                    minActiveState = Lifecycle.State.RESUMED,
                )
                LaunchedEffect(googleAccounts) {
                    Logger.w(
                        "SettingScreen",
                        "LaunchedEffect: ${
                            googleAccounts.data?.map {
                                it.name to it.isUsed
                            }
                        }",
                    )
                }
                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    item {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                        ) {
                            IconButton(
                                onClick = { showYouTubeAccountDialog = false },
                                colors =
                                    IconButtonDefaults.iconButtonColors().copy(
                                        contentColor = MaterialTheme.colorScheme.onSurface,
                                    ),
                                modifier =
                                    Modifier
                                        .align(Alignment.CenterStart)
                                        .fillMaxHeight(),
                            ) {
                                Icon(SimpIcons.Close, null, tint = MaterialTheme.colorScheme.onSurface)
                            }
                            Text(
                                stringResource(R.string.youtube_account),
                                style = typo().titleMedium,
                                modifier =
                                    Modifier
                                        .align(Alignment.Center)
                                        .wrapContentHeight(align = Alignment.CenterVertically)
                                        .wrapContentWidth(),
                            )
                        }
                    }
                    if (googleAccounts is LocalResource.Success) {
                        val data = googleAccounts.data
                        if (data.isNullOrEmpty()) {
                            item {
                                Text(
                                    stringResource(R.string.no_account),
                                    style = typo().bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier =
                                        Modifier
                                            .padding(12.dp)
                                            .fillMaxWidth(),
                                )
                            }
                        } else {
                            items(data) {
                                Row(
                                    modifier =
                                        Modifier
                                            .padding(vertical = 8.dp)
                                            .clickable {
                                                viewModel.setUsedAccount(it)
                                            },
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Spacer(Modifier.width(24.dp))
                                    AsyncImage(
                                        model =
                                            ImageRequest
                                                .Builder(LocalPlatformContext.current)
                                                .data(it.thumbnailUrl)
                                                .crossfade(550)
                                                .build(),
                                        placeholder = rememberVectorPainter(SimpIcons.PeopleAlt),
                                        error = rememberVectorPainter(SimpIcons.PeopleAlt),
                                        contentDescription = it.name,
                                        modifier =
                                            Modifier
                                                .size(48.dp)
                                                .clip(CircleShape),
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(it.name, style = typo().labelMedium, color = MaterialTheme.colorScheme.onBackground)
                                        Text(it.email, style = typo().bodySmall)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    AnimatedVisibility(it.isUsed) {
                                        Text(
                                            stringResource(R.string.signed_in),
                                            style = typo().bodySmall,
                                            maxLines = 2,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.widthIn(0.dp, 64.dp),
                                        )
                                    }
                                    Spacer(Modifier.width(24.dp))
                                }
                            }
                        }
                    } else {
                        item {
                            CenterLoadingBox(
                                Modifier
                                    .fillMaxWidth()
                                    .height(80.dp),
                            )
                        }
                    }
                    item {
                        Column {
                            ActionButton(
                                icon = SimpIcons.PeopleAlt,
                                text = R.string.guest,
                            ) {
                                viewModel.setUsedAccount(null)
                                showYouTubeAccountDialog = false
                            }
                            ActionButton(
                                icon = SimpIcons.Close,
                                text = R.string.log_out,
                            ) {
                                viewModel.setBasicAlertData(
                                    SettingBasicAlertState(
                                        title = runBlocking { getString(R.string.warning) },
                                        message = runBlocking { getString(R.string.log_out_warning) },
                                        confirm =
                                            runBlocking { getString(R.string.log_out) } to {
                                                viewModel.logOutAllYouTube()
                                                showYouTubeAccountDialog = false
                                            },
                                        dismiss = runBlocking { getString(R.string.cancel) },
                                    ),
                                )
                            }
                            ActionButton(
                                icon = SimpIcons.PlaylistAdd,
                                text = R.string.add_an_account,
                            ) {
                                showYouTubeAccountDialog = false
                                navController.navigate(LoginDestination)
                            }
                        }
                    }
                }
            }
        }
    }
    val alertData by viewModel.alertData.collectAsStateWithLifecycle()
    if (alertData != null) {
        val alertState = alertData ?: return
        // AlertDialog
        AlertDialog(
            onDismissRequest = { viewModel.setAlertData(null) },
            title = {
                Text(
                    text = alertState.title,
                    style = typo().titleSmall,
                )
            },
            text = {
                if (alertState.message != null) {
                    Column {
                        Text(text = alertState.message)
                        if (alertState.textField != null) {
                            val verify =
                                alertState.textField.verifyCodeBlock?.invoke(
                                    alertState.textField.value,
                                ) ?: (true to null)
                            TextField(
                                value = alertState.textField.value,
                                onValueChange = {
                                    viewModel.setAlertData(
                                        alertState.copy(
                                            textField =
                                                alertState.textField.copy(
                                                    value = it,
                                                ),
                                        ),
                                    )
                                },
                                isError = !verify.first,
                                label = { Text(text = alertState.textField.label) },
                                supportingText = {
                                    if (!verify.first) {
                                        Text(
                                            modifier = Modifier.fillMaxWidth(),
                                            text = verify.second ?: "",
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                },
                                trailingIcon = {
                                    if (!verify.first) {
                                        SimpIcons.Error
                                    }
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            vertical = 6.dp,
                                        ),
                            )
                        }
                    }
                } else if (alertState.selectOne != null) {
                    LazyColumn(
                        Modifier
                            .padding(vertical = 6.dp)
                            .heightIn(0.dp, 500.dp),
                    ) {
                        items(alertState.selectOne.listSelect) { item ->
                            val onSelect = {
                                viewModel.setAlertData(
                                    alertState.copy(
                                        selectOne =
                                            alertState.selectOne.copy(
                                                listSelect =
                                                    alertState.selectOne.listSelect.toMutableList().map {
                                                        if (it == item) {
                                                            true to it.second
                                                        } else {
                                                            false to it.second
                                                        }
                                                    },
                                            ),
                                    ),
                                )
                            }
                            Row(
                                Modifier
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        onSelect.invoke()
                                    }.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = item.first,
                                    onClick = {
                                        onSelect.invoke()
                                    },
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = item.second,
                                    style = typo().bodyMedium,
                                    maxLines = 1,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight(align = Alignment.CenterVertically)
                                            .basicMarquee(
                                                iterations = Int.MAX_VALUE,
                                                animationMode = MarqueeAnimationMode.Immediately,
                                            ).focusable(),
                                )
                            }
                        }
                    }
                } else if (alertState.multipleSelect != null) {
                    LazyColumn(
                        Modifier.padding(vertical = 6.dp),
                    ) {
                        items(alertState.multipleSelect.listSelect) { item ->
                            val onCheck = {
                                viewModel.setAlertData(
                                    alertState.copy(
                                        multipleSelect =
                                            alertState.multipleSelect.copy(
                                                listSelect =
                                                    alertState.multipleSelect.listSelect.toMutableList().map {
                                                        if (it == item) {
                                                            !it.first to it.second
                                                        } else {
                                                            it
                                                        }
                                                    },
                                            ),
                                    ),
                                )
                            }
                            Row(
                                Modifier
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        onCheck.invoke()
                                    }.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = item.first,
                                    onCheckedChange = {
                                        onCheck.invoke()
                                    },
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(text = item.second, style = typo().bodyMedium, maxLines = 1)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        alertState.confirm.second.invoke(alertState)
                        viewModel.setAlertData(null)
                    },
                    enabled =
                        if (alertState.textField?.verifyCodeBlock != null) {
                            alertState.textField.verifyCodeBlock
                                .invoke(
                                    alertState.textField.value,
                                ).first
                        } else {
                            true
                        },
                ) {
                    Text(text = alertState.confirm.first)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.setAlertData(null)
                    },
                ) {
                    Text(text = alertState.dismiss)
                }
            },
        )
    }

    if (showThirdPartyLibraries) {
        val libraries by produceLibraries {
            context.assets.open("aboutlibraries.json").readBytes().decodeToString()
        }
        val lazyListState = rememberLazyListState()
        val canScrollBackward by remember {
            derivedStateOf {
                lazyListState.canScrollBackward
            }
        }
        val sheetState =
            rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = {
                    !canScrollBackward
                },
            )
        val coroutineScope = rememberCoroutineScope()
        ModalBottomSheet(
            modifier =
                Modifier
                    .fillMaxHeight(),
            onDismissRequest = {
                showThirdPartyLibraries = false
            },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = {},
            scrimColor = Color.Black.copy(alpha = .5f),
            sheetState = sheetState,
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
            shape = RectangleShape,
        ) {
            // Capture theme colors here: the ChipColors getters below run outside composition.
            val surfaceContainerHighestColor = MaterialTheme.colorScheme.surfaceContainerHighest
            val onSurfaceColor = MaterialTheme.colorScheme.onSurface
            LibrariesContainer(
                libraries?.copy(
                    libraries =
                        libraries
                            ?.libraries
                            ?.distinctBy {
                                it.name
                            }?.toImmutableList() ?: emptyList<Library>().toImmutableList(),
                ),
                Modifier.fillMaxSize(),
                lazyListState = lazyListState,
                contentPadding = innerPadding,
                colors =
                    LibraryDefaults.libraryColors(
                        licenseChipColors =
                            object : ChipColors {
                                override val containerColor: Color
                                    get() = surfaceContainerHighestColor
                                override val contentColor: Color
                                    get() = onSurfaceColor
                            },
                    ),
                header = {
                    item {
                        TopAppBar(
                            windowInsets = WindowInsets(0, 0, 0, 0),
                            title = {
                                Text(
                                    text =
                                        stringResource(
                                            R.string.third_party_libraries,
                                        ),
                                    style = typo().titleMedium,
                                )
                            },
                            navigationIcon = {
                                Box(Modifier.padding(horizontal = 5.dp)) {
                                    RippleIconButton(
                                        SimpIcons.ArrowBackIosNew,
                                        Modifier
                                            .size(32.dp),
                                        true,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                    ) {
                                        coroutineScope.launch {
                                            sheetState.hide()
                                            showThirdPartyLibraries = false
                                        }
                                    }
                                }
                            },
                        )
                    }
                },
            )
        }
    }

    // Transparent while the list sits at the top — an always-on frost dimmed the glow behind the
    // bar into a black band, which is exactly where the glow carries its colour. Same crossfade
    // Home, Search and Mix run on their bars.
    // Captured outside the haze scope — HazeEffectScope is not composable (same move as
    // AlbumScreen's mutedPaletteBg capture).
    val settingBarTint = MaterialTheme.colorScheme.background
    AnimatedContent(
        targetState = isAtTop,
        transitionSpec = {
            fadeIn(tween(300)).togetherWith(fadeOut(tween(300)))
        },
    ) { atTop ->
        TopAppBar(
            title = {
                Text(
                    text =
                        stringResource(
                            R.string.settings,
                        ),
                    style = typo().titleMedium,
                )
            },
            navigationIcon = {
                Box(Modifier.padding(horizontal = 5.dp)) {
                    RippleIconButton(
                        SimpIcons.ArrowBackIosNew,
                        Modifier
                            .size(32.dp),
                        true,
                        tint = MaterialTheme.colorScheme.onSurface,
                    ) {
                        navController.navigateUp()
                    }
                }
            },
            modifier =
                Modifier
                    .then(
                        if (atTop) {
                            Modifier
                        } else {
                            // The house recipe from AlbumScreen's bars, thinned: ultraThin's built-in
                            // tint stacked on this page's dark ground read as a solid lid. 0.3 keeps
                            // the blur doing the work and the tint only settling legibility.
                            Modifier.hazeEffect(hazeState) {
                                blurEnabled = true
                                blurRadius = 24.dp
                                backgroundColor = settingBarTint
                                tints = listOf(HazeTint(settingBarTint.copy(alpha = 0.3f)))
                            }
                        },
                    ),
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
        )
    }
}

/**
 * Progress and outcome of a playlist import.
 *
 * Only dismissible once the import has finished — cancelling mid-write would leave the database
 * half-populated with no way to tell the user which half.
 */
@Composable
private fun ImportProgressDialog(
    progress: ImportProgress,
    onDismiss: () -> Unit,
) {
    val finished = progress is ImportProgress.Success || progress is ImportProgress.Error
    AlertDialog(
        onDismissRequest = { if (finished) onDismiss() },
        properties =
            DialogProperties(
                dismissOnBackPress = finished,
                dismissOnClickOutside = finished,
            ),
        title = {
            Text(
                text =
                    stringResource(
                        if (progress is ImportProgress.Error) R.string.import_failed else R.string.import_data,
                    ),
                style = typo().titleSmall,
            )
        },
        text = {
            Column {
                when (progress) {
                    is ImportProgress.Preparing -> {
                        Text(
                            text = stringResource(R.string.import_reading_file),
                            style = typo().bodyMedium,
                        )
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    is ImportProgress.Importing -> {
                        Text(
                            text = stringResource(R.string.import_progress_songs, progress.processed, progress.total),
                            style = typo().bodyMedium,
                        )
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = {
                                if (progress.total > 0) progress.processed.toFloat() / progress.total else 0f
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    is ImportProgress.Success -> {
                        Text(
                            text =
                                stringResource(
                                    R.string.import_result,
                                    progress.result.playlistsCreated,
                                    progress.result.songsImported,
                                ),
                            style = typo().bodyMedium,
                        )
                        if (progress.result.skippedEntries > 0) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.import_result_skipped, progress.result.skippedEntries),
                                style = typo().bodySmall,
                            )
                        }
                    }

                    is ImportProgress.Error -> {
                        Text(
                            text = progress.message,
                            style = typo().bodyMedium,
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (finished) {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.ok))
                }
            }
        },
    )
}