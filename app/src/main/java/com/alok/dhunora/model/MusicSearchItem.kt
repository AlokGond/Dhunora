package com.alok.dhunora.model

enum class SearchKind(val label: String) {
    SONG("Songs"),
    ALBUM("Albums"),
    PLAYLIST("Playlists"),
    ARTIST("Artists")
}

data class MusicSearchItem(
    val kind: SearchKind,
    val title: String,
    val subtitle: String,
    val sourceUrl: String,
    val thumbnailUrl: String? = null,
    val durationSeconds: Long = 0,
    val itemCount: Long = 0
) {
    fun toSongOrNull(): Song? =
        if (kind == SearchKind.SONG) {
            Song(
                title = title,
                artist = subtitle,
                sourceUrl = sourceUrl,
                durationSeconds = durationSeconds,
                thumbnailUrl = thumbnailUrl
            )
        } else {
            null
        }
}
