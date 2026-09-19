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

                            val generalJob =
                                async(Dispatchers.IO) {
                                    runCatching {
                                        doGeneralSongSearch(clean)
                                    }.getOrDefault(emptyList())
                                }

                            val officialVideoJob =
                                async(Dispatchers.IO) {
                                    runCatching {
                                        doGeneralSongSearch("$clean official video")
                                    }.getOrDefault(emptyList())
                                }

                            val songJob =
                                async(Dispatchers.IO) {
                                    runCatching {
                                        doGeneralSongSearch("$clean song")
                                    }.getOrDefault(emptyList())
                                }

                            val audioJob =
                                async(Dispatchers.IO) {
                                    runCatching {
                                        doGeneralSongSearch("$clean official audio")
                                    }.getOrDefault(emptyList())
                                }

                            (
                                ytmJob.await() +
                                    musicJob.await() +
                                    generalJob.await() +
                                    officialVideoJob.await() +
                                    songJob.await() +
                                    audioJob.await()
                            )
                                .distinctBy { it.sourceUrl }
                                .sortedByDescending { scoreSongResult(clean, it) }
                                .take(70)
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

                        if (kind == SearchKind.ARTIST) {
                            val channels =
                                runCatching {
                                    doSearch(
                                        clean,
                                        YoutubeSearchQueryHandlerFactory.CHANNELS,
                                        kind
                                    )
                                }.getOrDefault(emptyList())

                            (primary + channels)
                                .distinctBy { it.sourceUrl }
                                .sortedByDescending { item ->
                                    val q = normalize(clean)
                                    val title = normalize(item.title)
                                    when {
                                        title == q -> 100
                                        title.startsWith(q) -> 80
                                        title.contains(q) -> 60
                                        else -> q.split(" ").count { it.isNotBlank() && title.contains(it) } * 10
                                    }
                                }
                                .take(30)
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
        val q = normalize(query)
        val title = normalize(item.title)
        val haystack = normalize(item.title + " " + item.subtitle)
        val tokens =
            q.split(Regex("\\s+"))
                .filter { it.length >= 2 }
                .distinct()

        var score = 0
        if (title == q) score += 130
        if (title.startsWith(q)) score += 85
        if (haystack.contains(q)) score += 70

        val looseQ = q.replace("h", "")
        val looseHaystack = haystack.replace("h", "")
        if (looseQ.length >= 5 && looseHaystack.contains(looseQ)) score += 38

        score += tokens.count { haystack.contains(it) } * 12
        score += tokens.count {
            val loose = it.replace("h", "")
            loose.length >= 3 && looseHaystack.contains(loose)
        } * 4

        if (item.durationSeconds in 60..900) score += 10
        if (
            haystack.contains("official video") ||
            haystack.contains("official audio") ||
            haystack.contains("official song")
        ) {
            score += 10
        }

        if (
            haystack.contains("reaction") ||
            haystack.contains("trailer") ||
            haystack.contains("shorts") ||
            haystack.contains("status video") ||
            haystack.contains("whatsapp status")
        ) {
            score -= 35
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
                        if (subscriberCount in 1..99_999_999) {
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
        accountLikes: List<Song>,
        searchTerms: List<String> = emptyList()
    ): List<Song> =
        withContext(Dispatchers.IO) {
            val seeds =
                (recent.take(15) + favorites.take(8) + accountLikes.take(8))
                    .filter { it.artist.isNotBlank() }
                    .distinctBy(::songIdentity)

            val blockedKeys =
                (recent.take(35) + favorites.take(20) + accountLikes.take(20))
                    .map(::songIdentity)
                    .toSet()

            val artistCounts = linkedMapOf<String, Int>()
            seeds.forEachIndexed { index, song ->
                val artist = song.artist.trim()
                if (
                    artist.isNotBlank() &&
                    !artist.equals("Unknown artist", ignoreCase = true)
                ) {
                    val weight = (seeds.size - index).coerceAtLeast(1)
                    artistCounts[artist] = (artistCounts[artist] ?: 0) + weight
                }
            }

            val topArtists =
                artistCounts.entries
                    .sortedByDescending { it.value }
                    .map { it.key }
                    .take(5)

            val queries =
                buildList {
                    searchTerms
                        .map { it.trim() }
                        .filter { it.length >= 2 }
                        .distinctBy { it.lowercase() }
                        .take(6)
                        .forEach { add(it) }

                    topArtists.forEach { artist ->
                        add("$artist songs")
                    }

                    if (size < 5) {
                        add("new Indian songs")
                        add("trending music India")
                        add("popular music India")
                    }
                }
                    .distinctBy { it.lowercase() }
                    .take(10)

            val relatedSeeds =
                seeds
                    .take(5)
                    .shuffled()
                    .take(3)

            val buckets =
                coroutineScope {
                    val queryJobs =
                        queries.map { query ->
                            async(Dispatchers.IO) {
                                val ytm =
                                    runCatching {
                                        YouTubeMusicApi.searchSongs(query)
                                    }.getOrDefault(emptyList())
                                        .mapNotNull { it.toSongOrNull() }

                                val fallback =
                                    if (ytm.size >= 8) {
                                        emptyList()
                                    } else {
                                        runCatching {
                                            doSearch(
                                                query,
                                                YoutubeSearchQueryHandlerFactory.MUSIC_SONGS,
                                                SearchKind.SONG
                                            ).mapNotNull { it.toSongOrNull() }
                                        }.getOrDefault(emptyList())
                                    }

                                (ytm + fallback)
                                    .distinctBy(::songIdentity)
                                    .take(12)
                                    .shuffled()
                            }
                        }

                    val relatedJobs =
                        relatedSeeds.map { seed ->
                            async(Dispatchers.IO) {
                                runCatching { relatedSongs(seed) }
                                    .getOrDefault(emptyList())
                                    .distinctBy(::songIdentity)
                                    .take(12)
                                    .shuffled()
                            }
                        }

                    (queryJobs + relatedJobs).map { it.await() }
                }
                    .filter { it.isNotEmpty() }
                    .shuffled()

            if (buckets.isEmpty()) {
                return@withContext runCatching {
                    YouTubeMusicApi.searchSongs("trending music India")
                        .mapNotNull { it.toSongOrNull() }
                        .filter { songIdentity(it) !in blockedKeys }
                        .distinctBy(::songIdentity)
                        .shuffled()
                }.getOrDefault(emptyList())
            }

            val interleaved = mutableListOf<Song>()
            val maxSize = buckets.maxOfOrNull { it.size } ?: 0
            for (index in 0 until maxSize) {
                buckets.forEach { bucket ->
                    bucket.getOrNull(index)?.let(interleaved::add)
                }
            }

            val artistUse = mutableMapOf<String, Int>()
            interleaved
                .asSequence()
                .filter { song ->
                    val key = songIdentity(song)
                    key !in blockedKeys &&
                        song.title.isNotBlank() &&
                        song.durationSeconds !in 1..44
                }
                .distinctBy(::songIdentity)
                .filter { song ->
                    val artist = normalize(song.artist)
                    val count = artistUse[artist] ?: 0
                    if (artist.isBlank() || count < 4) {
                        if (artist.isNotBlank()) artistUse[artist] = count + 1
                        true
                    } else {
                        false
                    }
                }
                .take(60)
                .toList()
        }

    private fun songIdentity(song: Song): String {
        val cleanTitle =
            normalize(song.title)
                .replace(Regex("\\b(official|video|audio|lyrics|lyrical|full|song|hd|4k)\\b"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()

        val cleanArtist =
            normalize(song.artist)
                .replace(Regex("\\b(official|music|records|channel)\\b"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()

        return "$cleanTitle|$cleanArtist"
    }

    private fun normalize(value: String): String =
        value
            .lowercase()
            .replace(Regex("[^a-z0-9\\p{L}\\p{N}]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

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
                    val exact =
                        runCatching {
                            YouTubeMusicApi.searchSongs(item.title)
                                .mapNotNull { it.toSongOrNull() }
                        }.getOrDefault(emptyList())

                    val fallback =
                        runCatching {
                            searchSongs(item.title + " songs")
                        }.getOrDefault(emptyList())

                    (exact + fallback)
                        .distinctBy(::songIdentity)
                        .sortedByDescending { song ->
                            val artist = normalize(song.artist)
                            val target = normalize(item.title)
                            when {
                                artist == target -> 100
                                artist.contains(target) -> 70
                                target.split(" ").any { it.length >= 3 && artist.contains(it) } -> 35
                                else -> 0
                            }
                        }
                        .take(40)
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
        highQualityThumbnail(
            song.thumbnailUrl?.takeIf { it.isNotBlank() }
                ?: youtubeThumbnail(song.sourceUrl)
        )

    fun highQualityThumbnail(url: String?): String? {
        if (url.isNullOrBlank()) return null

        var upgraded = url

        if (
            upgraded.contains("googleusercontent.com") ||
            upgraded.contains("ggpht.com") ||
            upgraded.contains("yt3.ggpht.com")
        ) {
            upgraded =
                upgraded
                    .replace(Regex("w\\d+-h\\d+"), "w800-h800")
                    .replace(Regex("=s\\d+(-c)?"), "=s800-c")
        }

        if (upgraded.contains("i.ytimg.com/vi/")) {
            val id =
                Regex("/vi/([A-Za-z0-9_-]{11})/")
                    .find(upgraded)
                    ?.groupValues
                    ?.getOrNull(1)

            if (!id.isNullOrBlank()) {
                upgraded = "https://i.ytimg.com/vi/$id/hq720.jpg"
            }
        }

        return upgraded
    }

    private fun bestThumbnail(images: List<org.schabi.newpipe.extractor.Image>): String? =
        images
            .filter { it.url.isNotBlank() }
            .maxByOrNull { image ->
                image.width.coerceAtLeast(1) * image.height.coerceAtLeast(1)
            }
            ?.url
            ?.let(::upgradeThumbnailUrl)

    fun upgradeThumbnailUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null

        val googleLike =
            url.contains("googleusercontent.com") ||
                url.contains("ggpht.com") ||
                url.contains("yt3.")

        return when {
            googleLike && Regex("=w\\d+-h\\d+").containsMatchIn(url) ->
                url.replace(
                    Regex("=w\\d+-h\\d+[^?]*$"),
                    "=w1200-h1200-l90-rj"
                )

            googleLike && Regex("w\\d+-h\\d+").containsMatchIn(url) ->
                url.replace(Regex("w\\d+-h\\d+"), "w1200-h1200")

            url.contains("i.ytimg.com/vi/") ->
                url.replace(
                    Regex("/(default|mqdefault|hqdefault|sddefault|maxresdefault)\\.jpg.*$"),
                    "/sddefault.jpg"
                )

            else -> url
        }
    }
            ?.let(::highQualityThumbnail)

    private fun youtubeThumbnail(url: String): String? {
        val id = Regex("(?:v=|youtu\\.be/|shorts/)([A-Za-z0-9_-]{11})")
            .find(url)
            ?.groupValues
            ?.getOrNull(1)
            ?: return null

        return "https://i.ytimg.com/vi/" + id + "/hq720.jpg"
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
