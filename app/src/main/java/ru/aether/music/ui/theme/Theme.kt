package ru.aether.music.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AetherBg = Color(0xFF0A0A12)
val AetherSurface = Color(0xFF12122A)
val AetherPrimary = Color(0xFF7C6CFF)
val AetherSecondary = Color(0xFF5EEAD4)
val AetherTertiary = Color(0xFFF472B6)
val GlassWhite = Color(0x22FFFFFF)
val GlassBorder = Color(0x44FFFFFF)

private val DarkColors = darkColorScheme(
    primary = AetherPrimary,
    secondary = AetherSecondary,
    tertiary = AetherTertiary,
    background = AetherBg,
    surface = AetherSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color(0xFFF5F5FA),
    onSurface = Color(0xFFF5F5FA),
)

private val LightColors = lightColorScheme(
    primary = AetherPrimary,
    secondary = AetherSecondary,
    background = Color(0xFFF2F4FA),
    surface = Color(0xEEFFFFFF),
    onBackground = Color(0xFF12121A),
    onSurface = Color(0xFF12121A),
)

@Composable
fun AetherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
