package ru.aether.music
import android.app.Application
import android.content.Context

class AetherApp : Application() {
    lateinit var library: LibraryStore private set
    override fun onCreate() {
        super.onCreate()
        instance = this
        library = LibraryStore(this)
    }
    companion object { lateinit var instance: AetherApp private set }
}

class LibraryStore(ctx: Context) {
    var tracks: List<Track> = emptyList()
    private val prefs = ctx.getSharedPreferences("aether", Context.MODE_PRIVATE)
    private fun favIds() = prefs.getStringSet("favs", emptySet())!!.mapNotNull { it.toLongOrNull() }.toMutableSet()
    fun isFavorite(id: Long) = id in favIds()
    fun toggleFavorite(id: Long): Boolean {
        val s = favIds()
        if (id in s) s.remove(id) else s.add(id)
        prefs.edit().putStringSet("favs", s.map { it.toString() }.toSet()).apply()
        tracks = tracks.map { if (it.id == id) it.copy(favorite = id in s) else it }
        return id in s
    }
    fun bumpPlay(id: Long) {
        val map = (prefs.getStringSet("plays", emptySet()) ?: emptySet()).associate {
            val p = it.split(":"); p[0].toLong() to (p.getOrNull(1)?.toIntOrNull() ?: 0)
        }.toMutableMap()
        map[id] = (map[id] ?: 0) + 1
        prefs.edit().putStringSet("plays", map.map { "${it.key}:${it.value}" }.toSet()).apply()
        tracks = tracks.map { if (it.id == id) it.copy(playCount = map[id] ?: 0) else it }
    }
    fun playCount(id: Long) = (prefs.getStringSet("plays", emptySet()) ?: emptySet()).firstOrNull {
        it.startsWith("$id:")
    }?.substringAfter(":")?.toIntOrNull() ?: 0
    fun favorites() = tracks.filter { it.favorite }
    fun mostPlayed() = tracks.sortedByDescending { it.playCount }.filter { it.playCount > 0 }
    fun glassStyle() = prefs.getString("glass_style", "frost") ?: "frost"
    fun setGlassStyle(s: String) = prefs.edit().putString("glass_style", s).apply()
    fun sortMode() = prefs.getString("sort", "title") ?: "title"
    fun setSortMode(s: String) = prefs.edit().putString("sort", s).apply()
}
