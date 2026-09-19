package com.alok.dhunora.data

import com.alok.dhunora.model.MusicSearchItem
import com.alok.dhunora.model.SearchKind
import com.alok.dhunora.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.InfoItem
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
    private val audioUrlCache = ConcurrentHashMap<String, String>()

    suspend fun searchSongs(query: String): List<Song> =
        search(query, SearchKind.SONG).mapNotNull { it.toSongOrNull() }

    suspend fun search(query: String, kind: SearchKind): List<MusicSearchItem> =
        withContext(Dispatchers.IO) {
            val clean = query.trim()
            if (clean.isBlank()) return@withContext emptyList()

            val key = kind.name + ":" + clean.lowercase()
            typedSearchCache[key]?.let { return@withContext it }

            val filter = when (kind) {
                SearchKind.SONG -> YoutubeSearchQueryHandlerFactory.MUSIC_SONGS
                SearchKind.ALBUM -> YoutubeSearchQueryHandlerFactory.MUSIC_ALBUMS
                SearchKind.PLAYLIST -> YoutubeSearchQueryHandlerFactory.MUSIC_PLAYLISTS
                SearchKind.ARTIST -> YoutubeSearchQueryHandlerFactory.MUSIC_ARTISTS
            }

            val primary = runCatching {
                doSearch(clean, filter, kind)
            }.getOrDefault(emptyList())

            val results =
                if (kind == SearchKind.ARTIST && primary.isEmpty()) {
                    runCatching {
                        doSearch(clean, YoutubeSearchQueryHandlerFactory.CHANNELS, kind)
                    }.getOrDefault(emptyList())
                } else {
                    primary
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
        val handler = service.searchQHFactory.fromQuery(
            query,
            listOf(contentFilter),
            ""
        )

        return SearchInfo.getInfo(service, handler).relatedItems
            .mapNotNull { item -> item.toSearchItem(requestedKind) }
            .distinctBy { it.sourceUrl }
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
                    thumbnailUrl = youtubeThumbnail(url) ?: bestThumbnail(thumbnails),
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
                                    youtubeThumbnail(stream.url) ?: bestThumbnail(stream.thumbnails)
                            )
                        }.distinctBy { it.sourceUrl }
                    }.getOrDefault(emptyList())
                }

                SearchKind.ARTIST -> {
                    searchSongs(item.title + " songs").take(30)
                }
            }
        }

    suspend fun resolveAudioUrl(song: Song): String = withContext(Dispatchers.IO) {
        audioUrlCache[song.sourceUrl]?.let { return@withContext it }

        val info = StreamInfo.getInfo(ServiceList.YouTube, song.sourceUrl)
        val audio = info.audioStreams.asSequence()
            .filter { it.isUrl && it.content.isNotBlank() }
            .maxByOrNull { it.averageBitrate }
            ?: error("No playable audio stream found")

        audioUrlCache[song.sourceUrl] = audio.content
        audio.content
    }

    fun artworkFor(song: Song): String? =
        youtubeThumbnail(song.sourceUrl) ?: song.thumbnailUrl?.takeIf { it.isNotBlank() }

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
