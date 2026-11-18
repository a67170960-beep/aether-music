package ru.aether.music.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.aether.music.data.model.Track

data class PlayerUiState(
    val connected: Boolean = false,
    val isPlaying: Boolean = false,
    val current: Track? = null,
    val queue: List<Track> = emptyList(),
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val shuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
)

/**
 * Thin facade over Media3 MediaController bound to [AetherPlaybackService].
 */
class PlayerController(private val context: Context) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    private var trackMap: Map<String, Track> = emptyMap()

    fun connect() {
        if (controller != null) return
        val token = SessionToken(context, ComponentName(context, AetherPlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, token).buildAsync()
        controllerFuture?.addListener({
            controller = controllerFuture?.get()
            controller?.addListener(listener)
            publish()
        }, MoreExecutors.directExecutor())
    }

    fun release() {
        controller?.removeListener(listener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controller = null
        controllerFuture = null
    }

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            publish()
        }
    }

    private fun publish() {
        val c = controller ?: return
        val mediaId = c.currentMediaItem?.mediaId
        val current = mediaId?.let { trackMap[it] }
        _state.value = PlayerUiState(
            connected = true,
            isPlaying = c.isPlaying,
            current = current,
            queue = trackMap.values.toList(),
            positionMs = c.currentPosition,
            durationMs = c.duration.coerceAtLeast(0),
            shuffle = c.shuffleModeEnabled,
            repeatMode = c.repeatMode
        )
    }

    fun playTracks(tracks: List<Track>, startIndex: Int = 0) {
        val c = controller ?: return
        if (tracks.isEmpty()) return
        trackMap = tracks.associateBy { it.id.toString() }
        val items = tracks.map { t ->
            MediaItem.Builder()
                .setMediaId(t.id.toString())
                .setUri(t.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(t.displayTitle)
                        .setArtist(t.displayArtist)
                        .setAlbumTitle(t.displayAlbum)
                        .build()
                )
                .build()
        }
        c.setMediaItems(items, startIndex.coerceIn(0, items.lastIndex), 0)
        c.prepare()
        c.play()
        publish()
    }

    fun playPause() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun next() { controller?.seekToNext() }
    fun previous() { controller?.seekToPrevious() }
    fun seek(ms: Long) { controller?.seekTo(ms) }
    fun setShuffle(v: Boolean) { controller?.shuffleModeEnabled = v }
    fun cycleRepeat() {
        val c = controller ?: return
        c.repeatMode = when (c.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        publish()
    }

    fun setSpeed(speed: Float) {
        controller?.setPlaybackSpeed(speed.coerceIn(0.5f, 2f))
    }
}
