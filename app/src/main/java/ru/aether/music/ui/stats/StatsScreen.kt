package ru.aether.music.ui.stats

import androidx.compose.ui.Modifier.Modifier

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.modifier.fillMaxSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.aether.music.data.repo.GlassMode
import ru.aether.music.ui.AetherViewModel
import ru.aether.music.ui.components.GlassButton
import ru.aether.music.ui.components.LiquidGlassPanel

@Composable
fun StatsScreen(vm: AetherViewModel, glassMode: GlassMode, onBack: () -> Unit) {
    val tracks by vm.tracks.collectAsState()
    val favorites by vm.favorites.collectAsState()
    val most by vm.mostPlayed.collectAsState()
    var listenMs by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) { listenMs = vm.totalListenMs() }

    Column(Modifier.fillMaxSize().padding(16.dp, 40.dp, 16.dp, 24.dp)) {
        GlassButton("Back", onClick = onBack, mode = glassMode)
        Text("Statistics", color = Color.White, fontSize = 26.sp, modifier = Modifier.padding(vertical = 12.dp))
        LiquidGlassPanel(mode = glassMode) {
            Column(Modifier.padding(16.dp)) {
                Text("Tracks in library: ${tracks.size}", color = Color.White)
                Text("Favorites: ${favorites.size}", color = Color.White)
                Text("Total listen (logged): ${listenMs / 60000} min", color = Color.White)
            }
        }
        Text("Most played", color = Color.White, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        most.take(10).forEach { t ->
            Text(
                "${t.displayTitle} · ${t.playCount} plays",
                color = Color.White.copy(0.8f),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
