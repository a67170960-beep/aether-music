package ru.aether.music.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier.fillMaxSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.aether.music.data.repo.GlassMode
import ru.aether.music.data.repo.QualityProfile
import ru.aether.music.ui.AetherViewModel
import ru.aether.music.ui.components.GlassButton
import ru.aether.music.ui.components.LiquidGlassPanel

@Composable
fun SettingsScreen(
    vm: AetherViewModel,
    glassMode: GlassMode,
    onEqualizer: () -> Unit,
    onStats: () -> Unit
) {
    val intensity by vm.glassIntensity.collectAsState(0.75f)
    val quality by vm.quality.collectAsState(QualityProfile.BALANCED)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 40.dp, bottom = 100.dp)
    ) {
        Text("Settings", color = Color.White, fontSize = 26.sp, modifier = Modifier.padding(16.dp))

        Section("Liquid Glass") {
            Text("Mode: ${glassMode.name}", color = Color.White.copy(0.8f))
            Spacer(Modifier.height(8.dp))
            GlassMode.entries.forEach { mode ->
                GlassButton(
                    text = mode.name,
                    onClick = { vm.setGlassMode(mode) },
                    modifier = Modifier.padding(vertical = 4.dp),
                    mode = glassMode
                )
            }
            Text("Intensity", color = Color.White.copy(0.7f), modifier = Modifier.padding(top = 12.dp))
            Slider(value = intensity, onValueChange = { vm.setGlassIntensity(it) })
        }

        Section("Performance") {
            QualityProfile.entries.forEach { p ->
                GlassButton(
                    text = "${p.name}${if (quality == p) " ✓" else ""}",
                    onClick = { vm.setQuality(p) },
                    modifier = Modifier.padding(vertical = 4.dp),
                    mode = glassMode
                )
            }
        }

        Section("Audio") {
            GlassButton("Equalizer", onClick = onEqualizer, mode = glassMode)
            Spacer(Modifier.height(8.dp))
            GlassButton("Statistics", onClick = onStats, mode = glassMode)
            Spacer(Modifier.height(8.dp))
            GlassButton("Rescan library", onClick = { vm.scanLibrary() }, mode = glassMode)
        }

        Section("About") {
            Text("Aether Music 1.0.0-alpha", color = Color.White)
            Text("Local-first · Media3 · Liquid Glass", color = Color.White.copy(0.6f), fontSize = 13.sp)
            Text("No cloud upload of your files", color = Color.White.copy(0.6f), fontSize = 13.sp)
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Text(title, color = Color.White, fontSize = 18.sp, modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 6.dp))
    LiquidGlassPanel(modifier = Modifier.padding(horizontal = 16.dp), radius = 18.dp) {
        Column(Modifier.padding(14.dp), content = { content() })
    }
}
