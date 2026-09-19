package com.alok.dhunora.data

import com.alok.dhunora.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import java.util.concurrent.ConcurrentHashMap

object MusicRepository {
    private val searchCache = ConcurrentHashMap<String, List<Song>>()
    private val audioUrlCache = ConcurrentHashMap<String, String>()

    suspend fun searchSongs(query: String): List<Song> = withContext(Dispatchers.IO) {
        val key = query.trim()
        if (key.isBlank()) return@withContext emptyList()

        searchCache[key.lowercase()]?.let { return@withContext it }

        val service = ServiceList.YouTube
        val handler = service.searchQHFactory.fromQuery(
            key,
            listOf(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS),
            ""
        )

        val songs = SearchInfo.getInfo(service, handler).relatedItems
            .filterIsInstance<StreamInfoItem>()
            .map { item ->
                val extractorThumb = item.thumbnails
                    .filter { it.url.isNotBlank() }
                    .maxByOrNull { image ->
                        val w = image.width.coerceAtLeast(1)
                        val h = image.height.coerceAtLeast(1)
                        w * h
                    }
                    ?.url

                Song(
                    title = item.name,
                    artist = item.uploaderName ?: "Unknown artist",
                    sourceUrl = item.url,
                    durationSeconds = item.duration.coerceAtLeast(0),
                    thumbnailUrl = extractorThumb ?: youtubeThumbnail(item.url)
                )
            }
            .distinctBy { it.sourceUrl }

        searchCache[key.lowercase()] = songs
        songs
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

    fun artworkFor(song: Song): String? {
        return song.thumbnailUrl?.takeIf { it.isNotBlank() } ?: youtubeThumbnail(song.sourceUrl)
    }

    private fun youtubeThumbnail(url: String): String? {
        val id = Regex("(?:v=|youtu\\.be/|shorts/)([A-Za-z0-9_-]{11})")
            .find(url)
            ?.groupValues
            ?.getOrNull(1)
            ?: return null

        return "https://i.ytimg.com/vi/" + id + "/hqdefault.jpg"
    }
}
