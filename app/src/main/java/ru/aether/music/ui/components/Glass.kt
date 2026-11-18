package ru.aether.music.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.aether.music.data.repo.GlassMode
import ru.aether.music.ui.theme.AetherPrimary
import ru.aether.music.ui.theme.GlassBorder
import ru.aether.music.ui.theme.GlassWhite

@Composable
fun LiquidGlassPanel(
    modifier: Modifier = Modifier,
    radius: Dp = 22.dp,
    intensity: Float = 0.75f,
    mode: GlassMode = GlassMode.EXTREME,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(radius)
    val alpha = when (mode) {
        GlassMode.ULTRA, GlassMode.EXTREME -> 0.14f + intensity * 0.12f
        GlassMode.FROSTED -> 0.22f
        GlassMode.CRYSTAL -> 0.10f
        GlassMode.MINIMAL -> 0.08f
        GlassMode.PERFORMANCE -> 0.18f
        GlassMode.CUSTOM -> 0.14f + intensity * 0.1f
    }
    val blurRadius = when (mode) {
        GlassMode.PERFORMANCE, GlassMode.MINIMAL -> 0f
        GlassMode.FROSTED -> 18f * intensity
        GlassMode.ULTRA, GlassMode.EXTREME -> 28f * intensity
        else -> 16f * intensity
    }

    val base = Modifier
        .then(modifier)
        .shadow(12.dp, shape, ambientColor = Color.Black.copy(0.35f), spotColor = AetherPrimary.copy(0.25f))
        .clip(shape)
        .then(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 1f) {
                Modifier.graphicsLayer {
                    renderEffect = RenderEffect
                        .createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP)
                        .asComposeRenderEffect()
                }
            } else Modifier
        )
        .background(
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = alpha + 0.06f),
                    Color.White.copy(alpha = alpha * 0.5f),
                    Color(0xFF1A1A3A).copy(alpha = alpha + 0.15f)
                )
            )
        )
        .border(1.dp, GlassBorder.copy(alpha = 0.35f + intensity * 0.25f), shape)
        .then(
            if (onClick != null) Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ) else Modifier
        )

    Box(modifier = base, content = content)
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    mode: GlassMode = GlassMode.EXTREME
) {
    LiquidGlassPanel(
        modifier = modifier,
        radius = 16.dp,
        mode = mode,
        onClick = onClick
    ) {
        Box(
            Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = Color.White, fontSize = 15.sp)
        }
    }
}

@Composable
fun LiquidBackground(mode: GlassMode = GlassMode.EXTREME) {
    val vivid = mode != GlassMode.PERFORMANCE && mode != GlassMode.MINIMAL
    Box(
        Modifier
            .background(Color(0xFF0A0A12))
            .then(
                if (vivid) Modifier.background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0A0A12),
                            Color(0xFF12123A),
                            Color(0xFF0A1A22),
                            Color(0xFF0A0A12)
                        )
                    )
                ) else Modifier
            )
    )
}
