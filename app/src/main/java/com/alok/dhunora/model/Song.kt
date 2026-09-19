package com.alok.dhunora.model

data class Song(
    val title: String,
    val artist: String,
    val sourceUrl: String,
    val durationSeconds: Long = 0,
    val thumbnailUrl: String? = null,
    val localUri: String? = null
)
