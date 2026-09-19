package com.alok.dhunora.data

import com.alok.dhunora.model.MusicSearchItem
import com.alok.dhunora.model.SearchKind
import com.alok.dhunora.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.channel.ChannelInfoItem
import org.schabi.newpipe.extractor.playlist.PlaylistInfo
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import java.util.concurrent.ConcurrentHashMap

object MusicRepository {
    private val typedSearchCache = ConcurrentHashMap<String, List<MusicSearchItem>>()
    private data class CachedAudio(val url: String, val cachedAtMs: Long)

    private val audioUrlCache = ConcurrentHashMap<String, CachedAudio>()
    private val relatedSongsCache = ConcurrentHashMap<String, List<Song>>()
    private const val AUDIO_CACHE_TTL_MS = 20L * 60L * 1000L

    suspend fun searchSongs(query: String): List<Song> =
        search(query, SearchKind.SONG).mapNotNull { it.toSongOrNull() }

    suspend fun search(query: String, kind: SearchKind): List<MusicSearchItem> =
        withContext(Dispatchers.IO) {
            val clean = query.trim()
            if (clean.isBlank()) return@withContext emptyList()

            val key = kind.name + ":" + clean.lowercase()
            typedSearchCache[key]?.let { return@withContext it }

            val results =
                when (kind) {
                    SearchKind.SONG ->
                        coroutineScope {
                            val ytmJob =
                                async(Dispatchers.IO) {
                                    runCatching {
                                        YouTubeMusicApi.searchSongs(clean)
                                    }.getOrDefault(emptyList())
                                }

                            val musicJob =
                                async(Dispatchers.IO) {
                                    runCatching {
                                        doSearch(
                                            clean,
                                            YoutubeSearchQueryHandlerFactory.MUSIC_SONGS,
                                            SearchKind.SONG
                                        )
                                    }.getOrDefault(emptyList())
                                }

                            val fallbackJob =
                                async(Dispatchers.IO) {
                                    runCatching {
                                        doGeneralSongSearch(clean)
                                    }.getOrDefault(emptyList())
                                }

                            val ytm =
                                ytmJob.await()
                                    .sortedByDescending { scoreSongResult(clean, it) }

                            val music =
                                musicJob.await()
                                    .sortedByDescending { scoreSongResult(clean, it) }

                            val fallback =
                                fallbackJob.await()
                                    .sortedByDescending { scoreSongResult(clean, it) }

                            (ytm + music + fallback)
                                .distinctBy { it.sourceUrl }
                                .take(50)
                        }

                    else -> {
                        val filter =
                            when (kind) {
                                SearchKind.ALBUM -> YoutubeSearchQueryHandlerFactory.MUSIC_ALBUMS
                                SearchKind.PLAYLIST -> YoutubeSearchQueryHandlerFactory.MUSIC_PLAYLISTS
                                SearchKind.ARTIST -> YoutubeSearchQueryHandlerFactory.MUSIC_ARTISTS
                                SearchKind.SONG -> YoutubeSearchQueryHandlerFactory.MUSIC_SONGS
                            }

                        val primary =
                            runCatching {
                                doSearch(clean, filter, kind)
                            }.getOrDefault(emptyList())

                        if (kind == SearchKind.ARTIST && primary.isEmpty()) {
                            runCatching {
                                doSearch(
                                    clean,
                                    YoutubeSearchQueryHandlerFactory.CHANNELS,
                                    kind
                                )
                            }.getOrDefault(emptyList())
                        } else {
                            primary
                        }
                    }
                }

            typedSearchCache[key] = results
            results
        }

    private fun doSearch(
        query: String,
        contentFilter: String,
        requestedKind: SearchKind
    ): List<MusicSearchItem> {
        val service = ServiceList.YouTube
        val handler =
            service.searchQHFactory.fromQuery(
                query,
                listOf(contentFilter),
                ""
            )

        return SearchInfo.getInfo(service, handler).relatedItems
            .mapNotNull { item -> item.toSearchItem(requestedKind) }
            .distinctBy { it.sourceUrl }
    }

    private fun doGeneralSongSearch(query: String): List<MusicSearchItem> {
        val service = ServiceList.YouTube
        val handler = service.searchQHFactory.fromQuery(query)

        return SearchInfo.getInfo(service, handler).relatedItems
            .filterIsInstance<StreamInfoItem>()
            .map { item ->
                MusicSearchItem(
                    kind = SearchKind.SONG,
                    title = item.name,
                    subtitle = item.uploaderName ?: "Unknown artist",
                    sourceUrl = item.url,
                    thumbnailUrl =
                        bestThumbnail(item.thumbnails) ?: youtubeThumbnail(item.url),
                    durationSeconds = item.duration.coerceAtLeast(0)
                )
            }
            .distinctBy { it.sourceUrl }
    }

    private fun scoreSongResult(
        query: String,
        item: MusicSearchItem
    ): Int {
        val q = query.lowercase().trim()
        val haystack = (item.title + " " + item.subtitle).lowercase()
        val tokens =
            q.split(Regex("\\s+"))
                .filter { it.length >= 2 }
                .distinct()

        var score = 0
        if (haystack.contains(q)) score += 40
        score += tokens.count { haystack.contains(it) } * 9

        if (item.durationSeconds in 60..900) score += 8
        if (
            haystack.contains("official audio") ||
            haystack.contains("official song") ||
            haystack.contains("music")
        ) {
            score += 6
        }

        if (
            haystack.contains("reaction") ||
            haystack.contains("trailer") ||
            haystack.contains("shorts") ||
            haystack.contains("status video")
        ) {
            score -= 18
        }

        return score
    }

    private fun InfoItem.toSearchItem(requestedKind: SearchKind): MusicSearchItem? =
        when (this) {
            is StreamInfoItem -> {
                if (requestedKind != SearchKind.SONG) return null
                MusicSearchItem(
                    kind = SearchKind.SONG,
                    title = name,
                    subtitle = uploaderName ?: "Unknown artist",
                    sourceUrl = url,
                    thumbnailUrl = bestThumbnail(thumbnails) ?: youtubeThumbnail(url),
                    durationSeconds = duration.coerceAtLeast(0)
                )
            }

            is PlaylistInfoItem -> {
                val kind =
                    if (requestedKind == SearchKind.ALBUM) SearchKind.ALBUM
                    else SearchKind.PLAYLIST
                MusicSearchItem(
                    kind = kind,
                    title = name,
                    subtitle = uploaderName ?: if (kind == SearchKind.ALBUM) "Album" else "Playlist",
                    sourceUrl = url,
                    thumbnailUrl = bestThumbnail(thumbnails),
                    itemCount = streamCount.coerceAtLeast(0)
                )
            }

            is ChannelInfoItem -> {
                MusicSearchItem(
                    kind = SearchKind.ARTIST,
                    title = name,
                    subtitle =
                        if (subscriberCount > 0) {
                            formatCount(subscriberCount) + " subscribers"
                        } else {
                            "Artist"
                        },
                    sourceUrl = url,
                    thumbnailUrl = bestThumbnail(thumbnails),
                    itemCount = subscriberCount.coerceAtLeast(0)
                )
            }

            else -> null
        }

    suspend fun relatedSongs(song: Song): List<Song> =
        withContext(Dispatchers.IO) {
            relatedSongsCache[song.sourceUrl]?.let {
                return@withContext it
            }

            val directRelated =
                runCatching {
                    StreamInfo.getInfo(ServiceList.YouTube, song.sourceUrl)
                        .relatedItems
                        .filterIsInstance<StreamInfoItem>()
                        .map { item ->
                            Song(
                                title = item.name,
                                artist = item.uploaderName ?: "Unknown artist",
                                sourceUrl = item.url,
                                durationSeconds = item.duration.coerceAtLeast(0),
                                thumbnailUrl =
                                    bestThumbnail(item.thumbnails) ?: youtubeThumbnail(item.url)
                            )
                        }
                        .filter { candidate ->
                            candidate.sourceUrl != song.sourceUrl &&
                                candidate.durationSeconds !in 1..44
                        }
                }.getOrDefault(emptyList())

            val searched =
                coroutineScope {
                    val ytmJob =
                        async(Dispatchers.IO) {
                            runCatching {
                                YouTubeMusicApi.searchSongs(song.artist + " " + song.title)
                            }.getOrDefault(emptyList())
                        }

                    val artistJob =
                        async(Dispatchers.IO) {
                            runCatching {
                                doSearch(
                                    song.artist + " songs",
                                    YoutubeSearchQueryHandlerFactory.MUSIC_SONGS,
                                    SearchKind.SONG
                                )
                            }.getOrDefault(emptyList())
                        }

                    (ytmJob.await() + artistJob.await())
                        .mapNotNull { it.toSongOrNull() }
                }

            val result =
                (directRelated + searched)
                    .filter { it.sourceUrl != song.sourceUrl }
                    .distinctBy { it.sourceUrl }
                    .take(35)

            relatedSongsCache[song.sourceUrl] = result
            result
        }

    suspend fun personalizedHome(
        recent: List<Song>,
        favorites: List<Song>,
        accountLikes: List<Song>
    ): List<Song> =
        withContext(Dispatchers.IO) {
            val seeds =
                (recent.take(12) + favorites.take(8) + accountLikes.take(8))
                    .filter { it.artist.isNotBlank() }
                    .distinctBy { it.sourceUrl }

            if (seeds.isEmpty()) {
                return@withContext runCatching {
                    doSearch(
                        "Top music India",
                        YoutubeSearchQueryHandlerFactory.MUSIC_SONGS,
                        SearchKind.SONG
                    ).mapNotNull { it.toSongOrNull() }
                }.getOrDefault(emptyList())
            }

            val artistCounts = linkedMapOf<String, Int>()
            seeds.forEachIndexed { index, song ->
                val weight = (seeds.size - index).coerceAtLeast(1)
                val artist = song.artist.trim()
                if (
                    artist.isNotBlank() &&
                    !artist.equals("Unknown artist", ignoreCase = true)
                ) {
                    artistCounts[artist] = (artistCounts[artist] ?: 0) + weight
                }
            }

            val topArtists =
                artistCounts.entries
                    .sortedByDescending { it.value }
                    .map { it.key }
                    .take(4)

            val queries = mutableListOf<String>()
            topArtists.forEach { artist ->
                queries += artist + " hits"
            }

            seeds.take(3).forEach { seed ->
                queries += seed.title + " " + seed.artist
            }

            if (queries.size < 4) {
                queries += listOf(
                    "New Bollywood music",
                    "Trending Punjabi songs",
                    "Indian music hits"
                )
            }

            val buckets =
                coroutineScope {
                    queries
                        .distinct()
                        .take(7)
                        .map { query ->
                            async(Dispatchers.IO) {
                                runCatching {
                                    doSearch(
                                        query,
                                        YoutubeSearchQueryHandlerFactory.MUSIC_SONGS,
                                        SearchKind.SONG
                                    ).mapNotNull { it.toSongOrNull() }
                                }.getOrDefault(emptyList())
                            }
                        }
                        .map { it.await() }
                }

            val interleaved = mutableListOf<Song>()
            val maxSize = buckets.maxOfOrNull { it.size } ?: 0
            for (index in 0 until maxSize) {
                buckets.forEach { bucket ->
                    bucket.getOrNull(index)?.let { interleaved += it }
                }
            }

            val seedUrls = seeds.map { it.sourceUrl }.toSet()

            interleaved
                .filter { it.sourceUrl !in seedUrls }
                .distinctBy { it.sourceUrl }
                .take(50)
        }

    suspend fun loadCollection(item: MusicSearchItem): List<Song> =
        withContext(Dispatchers.IO) {
            when (item.kind) {
                SearchKind.SONG -> listOfNotNull(item.toSongOrNull())

                SearchKind.ALBUM, SearchKind.PLAYLIST -> {
                    runCatching {
                        PlaylistInfo.getInfo(item.sourceUrl).relatedItems.map { stream ->
                            Song(
                                title = stream.name,
                                artist = stream.uploaderName ?: item.subtitle,
                                sourceUrl = stream.url,
                                durationSeconds = stream.duration.coerceAtLeast(0),
                                thumbnailUrl =
                                    bestThumbnail(stream.thumbnails) ?: youtubeThumbnail(stream.url)
                            )
                        }.distinctBy { it.sourceUrl }
                    }.getOrDefault(emptyList())
                }

                SearchKind.ARTIST -> {
                    searchSongs(item.title + " songs").take(30)
                }
            }
        }
    suspend fun loadYouTubeLikedMusic(): List<Song> =
        withContext(Dispatchers.IO) {
            val urls = listOf(
                "https://music.youtube.com/playlist?list=LM",
                "https://www.youtube.com/playlist?list=LM"
            )

            for (url in urls) {
                val songs = runCatching {
                    PlaylistInfo.getInfo(url).relatedItems.map { stream ->
                        Song(
                            title = stream.name,
                            artist = stream.uploaderName ?: "YouTube Music",
                            sourceUrl = stream.url,
                            durationSeconds = stream.duration.coerceAtLeast(0),
                            thumbnailUrl =
                                bestThumbnail(stream.thumbnails) ?: youtubeThumbnail(stream.url)
                        )
                    }.distinctBy { it.sourceUrl }
                }.getOrDefault(emptyList())

                if (songs.isNotEmpty()) return@withContext songs
            }

            emptyList()
        }


    suspend fun resolveAudioUrl(song: Song): String = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        audioUrlCache[song.sourceUrl]
            ?.takeIf { now - it.cachedAtMs < AUDIO_CACHE_TTL_MS }
            ?.let { return@withContext it.url }

        val info = StreamInfo.getInfo(ServiceList.YouTube, song.sourceUrl)
        val streams = info.audioStreams
            .asSequence()
            .filter { it.isUrl && it.content.isNotBlank() }
            .toList()

        if (streams.isEmpty()) {
            error("No playable audio stream found")
        }

        // Prefer Android-friendly M4A/AAC around normal music bitrates.
        // It normally starts faster and is less error-prone than picking the
        // highest-bitrate stream blindly.
        val preferred = streams
            .filter { it.format == MediaFormat.M4A }
            .sortedByDescending { it.averageBitrate }
            .firstOrNull { it.averageBitrate in 96..192 }
            ?: streams
                .filter { it.format == MediaFormat.M4A }
                .maxByOrNull { it.averageBitrate }
            ?: streams
                .filter { it.averageBitrate in 64..192 }
                .maxByOrNull { it.averageBitrate }
            ?: streams.maxByOrNull { it.averageBitrate }
            ?: error("No playable audio stream found")

        audioUrlCache[song.sourceUrl] = CachedAudio(
            url = preferred.content,
            cachedAtMs = now
        )
        preferred.content
    }

    suspend fun prefetchAudioUrls(songs: List<Song>) = coroutineScope {
        songs
            .distinctBy { it.sourceUrl }
            .take(2)
            .map { song ->
                async(Dispatchers.IO) {
                    runCatching { resolveAudioUrl(song) }
                }
            }
            .forEach { it.await() }
    }

    fun invalidateAudioUrl(song: Song) {
        audioUrlCache.remove(song.sourceUrl)
    }

    fun artworkFor(song: Song): String? =
        song.thumbnailUrl?.takeIf { it.isNotBlank() } ?: youtubeThumbnail(song.sourceUrl)

    private fun bestThumbnail(images: List<org.schabi.newpipe.extractor.Image>): String? =
        images
            .filter { it.url.isNotBlank() }
            .maxByOrNull { image ->
                image.width.coerceAtLeast(1) * image.height.coerceAtLeast(1)
            }
            ?.url

    private fun youtubeThumbnail(url: String): String? {
        val id = Regex("(?:v=|youtu\\.be/|shorts/)([A-Za-z0-9_-]{11})")
            .find(url)
            ?.groupValues
            ?.getOrNull(1)
            ?: return null

        return "https://i.ytimg.com/vi/" + id + "/hqdefault.jpg"
    }

    private fun formatCount(value: Long): String =
        when {
            value >= 1_000_000 -> {
                val n = value / 100_000 / 10.0
                n.toString().removeSuffix(".0") + "M"
            }
            value >= 1_000 -> {
                val n = value / 100 / 10.0
                n.toString().removeSuffix(".0") + "K"
            }
            else -> value.toString()
        }
}
