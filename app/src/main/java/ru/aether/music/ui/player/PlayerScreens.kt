package ru.aether.music.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ru.aether.music.data.repo.GlassMode
import ru.aether.music.player.PlayerController
import ru.aether.music.player.PlayerUiState
import ru.aether.music.ui.components.LiquidGlassPanel
import ru.aether.music.ui.theme.AetherPrimary
import ru.aether.music.ui.theme.AetherSecondary

@Composable
fun MiniPlayerBar(
    state: PlayerUiState,
    glassMode: GlassMode,
    onOpen: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val t = state.current ?: return
    LiquidGlassPanel(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        radius = 18.dp,
        mode = glassMode,
        intensity = 0.85f,
        onClick = onOpen
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(t.displayTitle, color = Color.White, maxLines = 1)
                Text(t.displayArtist, color = Color.White.copy(0.6f), fontSize = 12.sp, maxLines = 1)
            }
            IconButton(onClick = onPlayPause) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Default.SkipNext, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Composable
fun FullPlayerScreen(
    player: PlayerController,
    glassMode: GlassMode,
    onBack: () -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit
) {
    val state by player.state.collectAsState()
    val track = state.current
    var pos by remember { mutableLongStateOf(0L) }
    LaunchedEffect(state.isPlaying, track?.id) {
        while (true) {
            pos = player.state.value.positionMs
            delay(400)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "⌄",
            color = Color.White,
            fontSize = 22.sp,
            modifier = Modifier.clickable(onClick = onBack)
        )
        Spacer(modifier = Modifier.height(16.dp))
        LiquidGlassPanel(radius = 28.dp, mode = glassMode, intensity = 0.9f) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    track?.displayTitle ?: "Nothing playing",
                    color = Color.White,
                    fontSize = 22.sp,
                    maxLines = 2
                )
                Text(
                    track?.displayArtist ?: "",
                    color = Color.White.copy(0.65f),
                    fontSize = 14.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        val dur = state.durationMs.coerceAtLeast(1)
        Slider(
            value = (pos.toFloat() / dur).coerceIn(0f, 1f),
            onValueChange = { player.seek((it * dur).toLong()) },
            colors = SliderDefaults.colors(
                thumbColor = AetherSecondary,
                activeTrackColor = AetherPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatMs(pos), color = Color.White.copy(0.6f), fontSize = 11.sp)
            Text(formatMs(dur), color = Color.White.copy(0.6f), fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        LiquidGlassPanel(radius = 24.dp, mode = glassMode, intensity = 0.8f) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { player.setShuffle(!state.shuffle) }) {
                    Text(if (state.shuffle) "⇄*" else "⇄", color = Color.White)
                }
                IconButton(onClick = { player.previous() }) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                IconButton(onClick = { player.playPause() }) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
                IconButton(onClick = { player.next() }) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                IconButton(onClick = { player.cycleRepeat() }) {
                    Text(
                        when (state.repeatMode) {
                            1 -> "①"
                            2 -> "↻"
                            else -> "↻"
                        },
                        color = Color.White
                    )
                }
            }
        }
        if (track != null) {
            IconButton(onClick = { onToggleFavorite(track.id, !track.isFavorite) }) {
                Icon(
                    imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = AetherPrimary
                )
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val s = (ms / 1000).toInt()
    return "%d:%02d".format(s / 60, s % 60)
}
