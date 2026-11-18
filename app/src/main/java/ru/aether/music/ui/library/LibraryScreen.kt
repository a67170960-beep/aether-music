package ru.aether.music.ui.library

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.aether.music.data.model.Track
import ru.aether.music.data.repo.GlassMode
import ru.aether.music.ui.AetherViewModel
import ru.aether.music.ui.components.TrackListItem

@Composable
fun LibraryScreen(
    vm: AetherViewModel,
    glassMode: GlassMode,
    onPlay: (List<Track>, Int) -> Unit
) {
    val tracks by vm.tracks.collectAsState()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp, top = 40.dp)
    ) {
        item {
            Text(
                "Library · ${tracks.size}",
                color = Color.White,
                fontSize = 26.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        itemsIndexed(tracks, key = { _, t -> t.id }) { index, t ->
            TrackListItem(
                track = t,
                artUri = vm.albumArt(t),
                onClick = { onPlay(tracks, index) },
                onFavorite = { vm.setFavorite(t.id, !t.isFavorite) }
            )
        }
    }
}
