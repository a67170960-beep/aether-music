package ru.aether.music.ui.equalizer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier.fillMaxSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.aether.music.data.repo.GlassMode
import ru.aether.music.ui.components.GlassButton
import ru.aether.music.ui.components.LiquidGlassPanel

@Composable
fun EqualizerScreen(glassMode: GlassMode, onBack: () -> Unit) {
    val bands = remember { mutableStateListOf(0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f) }
    var bass by remember { mutableFloatStateOf(0f) }
    val presets = listOf("Flat", "Rock", "Pop", "Jazz", "Classical", "Electronic", "Hip-Hop", "Metal", "Acoustic", "Vocal", "Deep Bass")

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp, 40.dp, 16.dp, 32.dp)
    ) {
        GlassButton("Back", onClick = onBack, mode = glassMode)
        Text("Equalizer", color = Color.White, fontSize = 26.sp, modifier = Modifier.padding(vertical = 12.dp))
        LiquidGlassPanel(mode = glassMode, radius = 20.dp) {
            Column(Modifier.padding(16.dp)) {
                bands.forEachIndexed { i, v ->
                    Text("Band ${i + 1}", color = Color.White.copy(0.7f), fontSize = 12.sp)
                    Slider(value = v, onValueChange = { bands[i] = it })
                }
                Text("Bass boost", color = Color.White.copy(0.7f))
                Slider(value = bass, onValueChange = { bass = it })
            }
        }
        Text("Presets", color = Color.White, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        presets.forEach { name ->
            GlassButton(
                text = name,
                onClick = {
                    for (i in bands.indices) {
                        bands[i] = when {
                            name == "Flat" -> 0.5f
                            name.contains("Bass") || name == "Hip-Hop" -> if (i < 3) 0.85f else 0.45f
                            name == "Vocal" -> if (i in 3..6) 0.8f else 0.4f
                            else -> (0.4f + (i % 3) * 0.15f).coerceIn(0f, 1f)
                        }
                    }
                },
                modifier = Modifier.padding(vertical = 4.dp),
                mode = glassMode
            )
        }
        Text(
            "System EQ attaches to the active audio session when the device HAL supports it (EqualizerEngine).",
            color = Color.White.copy(0.5f),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}
