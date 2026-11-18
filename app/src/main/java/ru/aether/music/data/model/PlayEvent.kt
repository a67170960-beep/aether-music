package ru.aether.music.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "play_events")
data class PlayEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackId: Long,
    val playedAt: Long = System.currentTimeMillis(),
    val durationHeardMs: Long = 0,
)
