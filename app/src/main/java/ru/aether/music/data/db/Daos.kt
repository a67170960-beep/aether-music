package ru.aether.music.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.aether.music.data.model.PlayEvent
import ru.aether.music.data.model.Playlist
import ru.aether.music.data.model.PlaylistTrack
import ru.aether.music.data.model.Track

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY title COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<Track>>

    @Query("SELECT * FROM tracks ORDER BY title COLLATE NOCASE ASC")
    suspend fun getAll(): List<Track>

    @Query("SELECT * FROM tracks WHERE id = :id")
    suspend fun getById(id: Long): Track?

    @Query(
        """SELECT * FROM tracks WHERE title LIKE '%' || :q || '%' 
        OR artist LIKE '%' || :q || '%' OR album LIKE '%' || :q || '%' 
        OR genre LIKE '%' || :q || '%' ORDER BY title COLLATE NOCASE LIMIT 200"""
    )
    suspend fun search(q: String): List<Track>

    @Query("SELECT * FROM tracks WHERE isFavorite = 1 ORDER BY title COLLATE NOCASE")
    fun observeFavorites(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE lastPlayed > 0 ORDER BY lastPlayed DESC LIMIT :limit")
    fun observeRecent(limit: Int = 40): Flow<List<Track>>

    @Query("SELECT * FROM tracks ORDER BY playCount DESC LIMIT :limit")
    fun observeMostPlayed(limit: Int = 40): Flow<List<Track>>

    @Query("SELECT * FROM tracks ORDER BY dateAdded DESC LIMIT :limit")
    fun observeRecentlyAdded(limit: Int = 40): Flow<List<Track>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(tracks: List<Track>)

    @Update
    suspend fun update(track: Track)

    @Query("UPDATE tracks SET isFavorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: Long, fav: Boolean)

    @Query("UPDATE tracks SET playCount = playCount + 1, lastPlayed = :whenMs WHERE id = :id")
    suspend fun recordPlay(id: Long, whenMs: Long = System.currentTimeMillis())

    @Query("UPDATE tracks SET customCoverUri = :uri WHERE id = :id")
    suspend fun setCover(id: Long, uri: String?)

    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun count(): Int

    @Query("DELETE FROM tracks")
    suspend fun clear()
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(playlist: Playlist): Long

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun get(id: Long): Playlist?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTracks(items: List<PlaylistTrack>)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrack(playlistId: Long, trackId: Long)

    @Query(
        """SELECT t.* FROM tracks t 
        INNER JOIN playlist_tracks pt ON pt.trackId = t.id 
        WHERE pt.playlistId = :playlistId ORDER BY pt.position ASC"""
    )
    fun observeTracks(playlistId: Long): Flow<List<Track>>

    @Query("SELECT trackId FROM playlist_tracks WHERE playlistId = :playlistId ORDER BY position")
    suspend fun trackIds(playlistId: Long): List<Long>
}

@Dao
interface StatsDao {
    @Insert
    suspend fun insertEvent(event: PlayEvent)

    @Query("SELECT SUM(durationHeardMs) FROM play_events")
    suspend fun totalListenMs(): Long?

    @Query(
        """SELECT trackId, SUM(durationHeardMs) as total FROM play_events 
        GROUP BY trackId ORDER BY total DESC LIMIT :limit"""
    )
    suspend fun topTrackIds(limit: Int = 20): List<TrackListenAgg>
}

data class TrackListenAgg(val trackId: Long, val total: Long)
