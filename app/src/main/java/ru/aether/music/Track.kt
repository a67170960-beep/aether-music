package ru.aether.music

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val uri: String,
    val durationMs: Long
)
