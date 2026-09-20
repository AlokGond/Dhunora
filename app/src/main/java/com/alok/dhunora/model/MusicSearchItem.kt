package com.alok.dhunora.model

enum class SearchKind(val label: String) {
    ALL("All"),
    SONG("Songs"),
    VIDEO("Videos"),
    ALBUM("Albums"),
    ARTIST("Artists"),
    PLAYLIST("Playlists")
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
        if (kind == SearchKind.SONG || kind == SearchKind.VIDEO) {
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
