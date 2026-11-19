package ru.aether.music

import android.app.Application
import android.content.Context

class AetherApp : Application() {
    lateinit var library: LibraryStore
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        library = LibraryStore(this)
    }

    companion object {
        lateinit var instance: AetherApp
            private set
    }
}

class LibraryStore(private val ctx: Context) {
    var tracks: List<Track> = emptyList()
    private val prefs = ctx.getSharedPreferences("aether", Context.MODE_PRIVATE)
    private val favIds get() = prefs.getStringSet("favs", emptySet())!!.mapNotNull { it.toLongOrNull() }.toMutableSet()
    private val plays get() = prefs.getStringSet("plays", emptySet()) ?: emptySet()

    fun isFavorite(id: Long) = id in favIds

    fun toggleFavorite(id: Long): Boolean {
        val s = favIds
        if (id in s) s.remove(id) else s.add(id)
        prefs.edit().putStringSet("favs", s.map { it.toString() }.toSet()).apply()
        tracks = tracks.map { if (it.id == id) it.copy(favorite = id in s) else it }
        return id in s
    }

    fun bumpPlay(id: Long) {
        val map = plays.associate {
            val p = it.split(":")
            p[0].toLong() to (p.getOrNull(1)?.toIntOrNull() ?: 0)
        }.toMutableMap()
        map[id] = (map[id] ?: 0) + 1
        prefs.edit().putStringSet("plays", map.map { "${it.key}:${it.value}" }.toSet()).apply()
        tracks = tracks.map { if (it.id == id) it.copy(playCount = map[id] ?: 0) else it }
    }

    fun playCount(id: Long): Int {
        plays.forEach {
            val p = it.split(":")
            if (p[0].toLongOrNull() == id) return p.getOrNull(1)?.toIntOrNull() ?: 0
        }
        return 0
    }

    fun favorites(): List<Track> = tracks.filter { it.favorite }
    fun mostPlayed(): List<Track> = tracks.sortedByDescending { it.playCount }.filter { it.playCount > 0 }

    fun addListenMs(ms: Long) {
        val cur = prefs.getLong("listen_ms", 0L)
        prefs.edit().putLong("listen_ms", cur + ms).apply()
    }

    fun totalListenMs() = prefs.getLong("listen_ms", 0L)

    fun glassMode(): String = prefs.getString("glass", "EXTREME") ?: "EXTREME"
    fun setGlassMode(m: String) = prefs.edit().putString("glass", m).apply()
}
