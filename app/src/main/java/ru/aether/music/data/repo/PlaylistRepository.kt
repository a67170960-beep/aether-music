package ru.aether.music.data.repo

import kotlinx.coroutines.flow.Flow
import ru.aether.music.data.db.AetherDatabase
import ru.aether.music.data.model.Playlist
import ru.aether.music.data.model.PlaylistTrack
import ru.aether.music.data.model.Track

class PlaylistRepository(private val db: AetherDatabase) {
    fun observePlaylists(): Flow<List<Playlist>> = db.playlists().observeAll()
    fun observeTracks(playlistId: Long): Flow<List<Track>> = db.playlists().observeTracks(playlistId)

    suspend fun create(name: String, description: String = ""): Long {
        return db.playlists().upsert(
            Playlist(name = name, description = description)
        )
    }

    suspend fun rename(id: Long, name: String) {
        val p = db.playlists().get(id) ?: return
        db.playlists().upsert(p.copy(name = name, updatedAt = System.currentTimeMillis()))
    }

    suspend fun delete(id: Long) = db.playlists().delete(id)

    suspend fun addTracks(playlistId: Long, trackIds: List<Long>) {
        val existing = db.playlists().trackIds(playlistId)
        var pos = existing.size
        val items = trackIds.filter { it !in existing }.map {
            PlaylistTrack(playlistId, it, pos++)
        }
        if (items.isNotEmpty()) db.playlists().addTracks(items)
        val p = db.playlists().get(playlistId) ?: return
        db.playlists().upsert(p.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun removeTrack(playlistId: Long, trackId: Long) =
        db.playlists().removeTrack(playlistId, trackId)
}
