package ru.aether.music
data class Track(
    val id: Long, val title: String, val artist: String, val album: String,
    val uri: String, val durationMs: Long, var favorite: Boolean = false, var playCount: Int = 0
)
fun formatMs(ms: Long): String {
    val s = (ms / 1000).toInt().coerceAtLeast(0)
    return "%d:%02d".format(s / 60, s % 60)
}
