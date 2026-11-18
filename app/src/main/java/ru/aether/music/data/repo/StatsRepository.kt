package ru.aether.music.data.repo

import ru.aether.music.data.db.AetherDatabase
import ru.aether.music.data.model.PlayEvent

class StatsRepository(private val db: AetherDatabase) {
    suspend fun logPlay(trackId: Long, heardMs: Long) {
        db.stats().insertEvent(PlayEvent(trackId = trackId, durationHeardMs = heardMs))
        db.tracks().recordPlay(trackId)
    }

    suspend fun totalListenMs(): Long = db.stats().totalListenMs() ?: 0L
    suspend fun topTracks(limit: Int = 20) = db.stats().topTrackIds(limit)
}
