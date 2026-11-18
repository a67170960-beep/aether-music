package ru.aether.music.data.repo

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("aether_settings")

enum class GlassMode { ULTRA, EXTREME, FROSTED, CRYSTAL, MINIMAL, PERFORMANCE, CUSTOM }
enum class QualityProfile { ULTRA, BALANCED, PERFORMANCE, BATTERY }

class SettingsRepository(private val context: Context) {
    private object Keys {
        val glassMode = stringPreferencesKey("glass_mode")
        val glassIntensity = floatPreferencesKey("glass_intensity")
        val blurEnabled = booleanPreferencesKey("blur_enabled")
        val crossfadeMs = intPreferencesKey("crossfade_ms")
        val gapless = booleanPreferencesKey("gapless")
        val shuffle = booleanPreferencesKey("shuffle")
        val repeatMode = intPreferencesKey("repeat_mode") // 0 off 1 one 2 all
        val quality = stringPreferencesKey("quality_profile")
        val eqEnabled = booleanPreferencesKey("eq_enabled")
        val bassBoost = intPreferencesKey("bass_boost")
        val themeSeed = intPreferencesKey("theme_seed")
        val homeSections = stringPreferencesKey("home_sections")
        val playbackSpeed = floatPreferencesKey("playback_speed")
        val sleepTimerMin = intPreferencesKey("sleep_timer_min")
    }

    val glassMode: Flow<GlassMode> = context.dataStore.data.map {
        runCatching { GlassMode.valueOf(it[Keys.glassMode] ?: GlassMode.EXTREME.name) }
            .getOrDefault(GlassMode.EXTREME)
    }

    val glassIntensity: Flow<Float> = context.dataStore.data.map { it[Keys.glassIntensity] ?: 0.75f }
    val blurEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.blurEnabled] ?: true }
    val crossfadeMs: Flow<Int> = context.dataStore.data.map { it[Keys.crossfadeMs] ?: 0 }
    val gapless: Flow<Boolean> = context.dataStore.data.map { it[Keys.gapless] ?: true }
    val shuffle: Flow<Boolean> = context.dataStore.data.map { it[Keys.shuffle] ?: false }
    val repeatMode: Flow<Int> = context.dataStore.data.map { it[Keys.repeatMode] ?: 0 }
    val quality: Flow<QualityProfile> = context.dataStore.data.map {
        runCatching { QualityProfile.valueOf(it[Keys.quality] ?: QualityProfile.BALANCED.name) }
            .getOrDefault(QualityProfile.BALANCED)
    }
    val eqEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.eqEnabled] ?: false }
    val bassBoost: Flow<Int> = context.dataStore.data.map { it[Keys.bassBoost] ?: 0 }
    val playbackSpeed: Flow<Float> = context.dataStore.data.map { it[Keys.playbackSpeed] ?: 1f }
    val homeSections: Flow<String> = context.dataStore.data.map {
        it[Keys.homeSections] ?: "greeting,continue,recent,favorites,most,added"
    }

    suspend fun setGlassMode(mode: GlassMode) = context.dataStore.edit { it[Keys.glassMode] = mode.name }
    suspend fun setGlassIntensity(v: Float) = context.dataStore.edit { it[Keys.glassIntensity] = v }
    suspend fun setBlur(v: Boolean) = context.dataStore.edit { it[Keys.blurEnabled] = v }
    suspend fun setCrossfade(ms: Int) = context.dataStore.edit { it[Keys.crossfadeMs] = ms }
    suspend fun setGapless(v: Boolean) = context.dataStore.edit { it[Keys.gapless] = v }
    suspend fun setShuffle(v: Boolean) = context.dataStore.edit { it[Keys.shuffle] = v }
    suspend fun setRepeat(v: Int) = context.dataStore.edit { it[Keys.repeatMode] = v }
    suspend fun setQuality(p: QualityProfile) = context.dataStore.edit { it[Keys.quality] = p.name }
    suspend fun setEqEnabled(v: Boolean) = context.dataStore.edit { it[Keys.eqEnabled] = v }
    suspend fun setBassBoost(v: Int) = context.dataStore.edit { it[Keys.bassBoost] = v }
    suspend fun setPlaybackSpeed(v: Float) = context.dataStore.edit { it[Keys.playbackSpeed] = v }
    suspend fun setHomeSections(csv: String) = context.dataStore.edit { it[Keys.homeSections] = csv }
    suspend fun setSleepTimer(min: Int) = context.dataStore.edit { it[Keys.sleepTimerMin] = min }
}
