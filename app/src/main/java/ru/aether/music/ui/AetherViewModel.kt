package ru.aether.music.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.aether.music.AetherApp
import ru.aether.music.data.model.Track
import ru.aether.music.data.repo.GlassMode
import ru.aether.music.data.repo.QualityProfile
import ru.aether.music.player.PlayerController

class AetherViewModel(
    private val app: AetherApp,
    val player: PlayerController
) : ViewModel() {

    val tracks = app.libraryRepository.observeTracks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val favorites = app.libraryRepository.observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val recent = app.libraryRepository.observeRecent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val mostPlayed = app.libraryRepository.observeMostPlayed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val recentlyAdded = app.libraryRepository.observeRecentlyAdded()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val playlists = app.playlistRepository.observePlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val glassMode = app.settingsRepository.glassMode
    val glassIntensity = app.settingsRepository.glassIntensity
    val quality = app.settingsRepository.quality

    private val _scanning = MutableStateFlow(false)
    val scanning: StateFlow<Boolean> = _scanning
    private val _scanCount = MutableStateFlow(0)
    val scanCount: StateFlow<Int> = _scanCount
    private val _searchResults = MutableStateFlow<List<Track>>(emptyList())
    val searchResults: StateFlow<List<Track>> = _searchResults
    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status

    fun scanLibrary() {
        viewModelScope.launch {
            _scanning.value = true
            _status.value = "Scanning…"
            try {
                val n = app.libraryRepository.scanMediaStore()
                _scanCount.value = n
                _status.value = "Found $n tracks"
            } catch (e: Exception) {
                _status.value = "Scan failed: ${e.message}"
            } finally {
                _scanning.value = false
            }
        }
    }

    fun search(q: String) {
        viewModelScope.launch {
            _searchResults.value = if (q.isBlank()) emptyList() else app.libraryRepository.search(q)
        }
    }

    fun setFavorite(id: Long, fav: Boolean) {
        viewModelScope.launch { app.libraryRepository.setFavorite(id, fav) }
    }

    fun setGlassMode(mode: GlassMode) {
        viewModelScope.launch { app.settingsRepository.setGlassMode(mode) }
    }

    fun setQuality(p: QualityProfile) {
        viewModelScope.launch { app.settingsRepository.setQuality(p) }
    }

    fun setGlassIntensity(v: Float) {
        viewModelScope.launch { app.settingsRepository.setGlassIntensity(v) }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch { app.playlistRepository.create(name) }
    }

    fun albumArt(track: Track): Any =
        track.customCoverUri ?: app.libraryRepository.albumArtUri(track.albumId)

    suspend fun totalListenMs() = app.statsRepository.totalListenMs()

    companion object {
        fun factory(app: AetherApp, player: PlayerController) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AetherViewModel(app, player) as T
            }
    }
}
