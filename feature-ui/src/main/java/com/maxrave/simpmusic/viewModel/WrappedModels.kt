package com.alok.dhunora.ui.viewModel

import com.maxrave.domain.data.entities.AlbumEntity
import com.maxrave.domain.data.entities.ArtistEntity
import com.maxrave.domain.data.entities.SongEntity

/**
 * Minimal Wrapped models backing [com.alok.dhunora.ui.ui.component.WrappedEntryCard].
 *
 * The full Wrapped reel (clock card, archetype, analytics stats) is not ported yet; these carry
 * exactly what the entry card reads so the year-in-review entry point compiles and renders.
 */
data class WrappedStats(
    val listenedSeconds: Long = 0L,
    val distinctArtists: Int = 0,
)

data class WrappedTrack(
    val song: SongEntity,
)

data class WrappedAlbum(
    val album: AlbumEntity,
)

data class WrappedArtist(
    val artist: ArtistEntity,
)

data class WrappedYear(
    val year: Int,
    val stats: WrappedStats = WrappedStats(),
    val topTracks: List<WrappedTrack> = emptyList(),
    val topArtists: List<WrappedArtist> = emptyList(),
    val topAlbums: List<WrappedAlbum> = emptyList(),
)
