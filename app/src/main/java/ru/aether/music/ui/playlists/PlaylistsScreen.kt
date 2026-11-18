package ru.aether.music.ui.playlists

import androidx.compose.ui.Modifier.Modifier

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier.fillMaxSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.aether.music.data.repo.GlassMode
import ru.aether.music.ui.AetherViewModel
import ru.aether.music.ui.components.GlassButton
import ru.aether.music.ui.components.LiquidGlassPanel

@Composable
fun PlaylistsScreen(vm: AetherViewModel, glassMode: GlassMode) {
    val playlists by vm.playlists.collectAsState()
    var counter by remember { mutableStateOf(1) }

    Column(Modifier.fillMaxSize().padding(top = 40.dp)) {
        Text("Playlists", color = Color.White, fontSize = 26.sp, modifier = Modifier.padding(16.dp))
        GlassButton(
            text = "New playlist",
            onClick = {
                vm.createPlaylist("Playlist $counter")
                counter++
            },
            modifier = Modifier.padding(horizontal = 16.dp),
            mode = glassMode
        )
        LazyColumn(contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 100.dp)) {
            items(playlists, key = { it.id }) { p ->
                LiquidGlassPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    mode = glassMode
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(p.name, color = Color.White, fontSize = 16.sp)
                        if (p.description.isNotBlank()) {
                            Text(p.description, color = Color.White.copy(0.6f), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
