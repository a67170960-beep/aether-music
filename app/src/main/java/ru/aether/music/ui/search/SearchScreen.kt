package ru.aether.music.ui.search

import androidx.compose.ui.Modifier.Modifier

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier.fillMaxSize
import androidx.compose.ui.modifier.fillMaxWidth
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.aether.music.data.model.Track
import ru.aether.music.data.repo.GlassMode
import ru.aether.music.ui.AetherViewModel
import ru.aether.music.ui.components.LiquidGlassPanel
import ru.aether.music.ui.components.TrackListItem

@Composable
fun SearchScreen(
    vm: AetherViewModel,
    glassMode: GlassMode,
    onPlay: (List<Track>, Int) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val results by vm.searchResults.collectAsState()

    Column(Modifier.fillMaxSize().padding(top = 40.dp)) {
        Text("Search", color = Color.White, fontSize = 26.sp, modifier = Modifier.padding(horizontal = 16.dp))
        LiquidGlassPanel(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            radius = 16.dp,
            mode = glassMode
        ) {
            BasicTextField(
                value = query,
                onValueChange = {
                    query = it
                    vm.search(it)
                },
                singleLine = true,
                cursorBrush = SolidColor(Color.White),
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 16.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                decorationBox = { inner ->
                    if (query.isEmpty()) Text("Songs, artists, albums…", color = Color.White.copy(0.45f))
                    inner()
                }
            )
        }
        LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
            itemsIndexed(results) { index, t ->
                TrackListItem(
                    track = t,
                    artUri = vm.albumArt(t),
                    onClick = { onPlay(results, index) },
                    onFavorite = { vm.setFavorite(t.id, !t.isFavorite) }
                )
            }
        }
    }
}
