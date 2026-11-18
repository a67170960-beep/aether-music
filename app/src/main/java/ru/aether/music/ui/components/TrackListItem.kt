package ru.aether.music.ui.components

import androidx.compose.ui.Modifier.Modifier

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ru.aether.music.data.model.Track
import ru.aether.music.ui.theme.AetherPrimary

@Composable
fun TrackListItem(
    track: Track,
    artUri: Any?,
    onClick: () -> Unit,
    onFavorite: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = artUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(Modifier.Width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(track.displayTitle, color = androidx.compose.ui.graphics.Color.White, fontSize = 15.sp, maxLines = 1)
            Text(
                "${track.displayArtist} · ${track.displayAlbum}",
                color = androidx.compose.ui.graphics.Color.White.copy(0.6f),
                fontSize = 12.sp,
                maxLines = 1
            )
        }
        IconButton(onClick = onFavorite) {
            Icon(
                if (track.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (track.isFavorite) AetherPrimary else androidx.compose.ui.graphics.Color.White.copy(0.7f)
            )
        }
    }
}
