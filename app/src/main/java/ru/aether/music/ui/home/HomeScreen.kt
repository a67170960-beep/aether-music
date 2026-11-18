package ru.aether.music.ui.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ru.aether.music.data.model.Track
import ru.aether.music.data.repo.GlassMode
import ru.aether.music.ui.AetherViewModel
import ru.aether.music.ui.components.GlassButton
import ru.aether.music.ui.components.LiquidGlassPanel
import ru.aether.music.ui.components.TrackListItem
import java.util.Calendar

@Composable
fun HomeScreen(
    vm: AetherViewModel,
    glassMode: GlassMode,
    onOpenPlayer: () -> Unit,
    onPlayTrack: (List<Track>, Int) -> Unit
) {
    val tracks by vm.tracks.collectAsState()
    val recent by vm.recent.collectAsState()
    val favorites by vm.favorites.collectAsState()
    val most by vm.mostPlayed.collectAsState()
    val added by vm.recentlyAdded.collectAsState()
    val scanning by vm.scanning.collectAsState()
    val status by vm.status.collectAsState()

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greet = when {
        hour < 6 -> "Good night"
        hour < 12 -> "Good morning"
        hour < 18 -> "Good afternoon"
        else -> "Good evening"
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Text(
                "$greet",
                color = Color.White,
                fontSize = 28.sp,
                modifier = Modifier.padding(start = 20.dp, top = 48.dp, end = 20.dp)
            )
            Text(
                "Aether Music · local library",
                color = Color.White.copy(0.55f),
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 20.dp, bottom = 12.dp)
            )
        }
        item {
            Row(
                Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassButton(
                    text = if (scanning) "Scanning…" else "Scan library",
                    onClick = { if (!scanning) vm.scanLibrary() },
                    mode = glassMode
                )
                if (scanning) CircularProgressIndicator(Modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                if (status.isNotBlank()) Text(status, color = Color.White.copy(0.7f), fontSize = 12.sp)
            }
            Spacer(Modifier.height(12.dp))
        }

        if (recent.isNotEmpty()) {
            item { SectionTitle("Continue listening") }
            item {
                Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp)) {
                    recent.take(12).forEachIndexed { index, t ->
                        LiquidGlassPanel(
                            modifier = Modifier
                                .width(130.dp)
                                .padding(6.dp),
                            radius = 18.dp,
                            mode = glassMode,
                            onClick = {
                                onPlayTrack(recent, index)
                                onOpenPlayer()
                            }
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                AsyncImage(
                                    model = vm.albumArt(t),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(108.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                Text(t.displayTitle, color = Color.White, fontSize = 12.sp, maxLines = 1)
                                Text(t.displayArtist, color = Color.White.copy(0.6f), fontSize = 11.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        if (favorites.isNotEmpty()) {
            item { SectionTitle("Favorites") }
            itemsIndexed(favorites.take(8)) { index, t ->
                TrackListItem(
                    track = t,
                    artUri = vm.albumArt(t),
                    onClick = { onPlayTrack(favorites, index); onOpenPlayer() },
                    onFavorite = { vm.setFavorite(t.id, !t.isFavorite) }
                )
            }
        }

        if (most.isNotEmpty()) {
            item { SectionTitle("Most played") }
            itemsIndexed(most.take(8)) { index, t ->
                TrackListItem(
                    track = t,
                    artUri = vm.albumArt(t),
                    onClick = { onPlayTrack(most, index); onOpenPlayer() },
                    onFavorite = { vm.setFavorite(t.id, !t.isFavorite) }
                )
            }
        }

        if (added.isNotEmpty()) {
            item { SectionTitle("Recently added") }
            itemsIndexed(added.take(10)) { index, t ->
                TrackListItem(
                    track = t,
                    artUri = vm.albumArt(t),
                    onClick = { onPlayTrack(added, index); onOpenPlayer() },
                    onFavorite = { vm.setFavorite(t.id, !t.isFavorite) }
                )
            }
        }

        if (tracks.isEmpty() && !scanning) {
            item {
                LiquidGlassPanel(Modifier = Modifier.padding(20.dp).fillMaxWidth(), mode = glassMode) {
                    Text(
                        "No tracks yet. Grant audio permission and tap Scan library.",
                        color = Color.White.copy(0.8f),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        color = Color.White,
        fontSize = 18.sp,
        modifier = Modifier.padding(start = 16.dp, top = 18.dp, bottom = 8.dp)
    )
}
