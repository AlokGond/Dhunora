package com.alok.dhunora.data

import com.alok.dhunora.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem

object MusicRepository {
    suspend fun searchSongs(query: String): List<Song> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val service = ServiceList.YouTube
        val handler = service.searchQHFactory.fromQuery(
            query.trim(),
            listOf(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS),
            ""
        )
        SearchInfo.getInfo(service, handler).relatedItems
            .filterIsInstance<StreamInfoItem>()
            .map {
                Song(
                    it.name,
                    it.uploaderName ?: "Unknown artist",
                    it.url,
                    it.duration.coerceAtLeast(0),
                    it.thumbnails.maxByOrNull { image -> image.width }?.url
                )
            }
            .distinctBy { it.sourceUrl }
    }

    suspend fun resolveAudioUrl(song: Song): String = withContext(Dispatchers.IO) {
        val info = StreamInfo.getInfo(ServiceList.YouTube, song.sourceUrl)
        val audio = info.audioStreams.asSequence()
            .filter { it.isUrl }
            .maxByOrNull { it.averageBitrate }
            ?: error("No playable audio stream found")
        audio.content
    }
}
