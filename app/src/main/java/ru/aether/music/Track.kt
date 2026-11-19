package ru.aether.music

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val uri: String,
    val durationMs: Long,
    var favorite: Boolean = false,
    var playCount: Int = 0
)
