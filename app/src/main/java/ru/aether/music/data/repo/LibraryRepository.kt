package ru.aether.music.data.repo

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.aether.music.data.db.AetherDatabase
import ru.aether.music.data.model.Track

class LibraryRepository(
    private val context: Context,
    private val db: AetherDatabase
) {
    fun observeTracks(): Flow<List<Track>> = db.tracks().observeAll()
    fun observeFavorites(): Flow<List<Track>> = db.tracks().observeFavorites()
    fun observeRecent(): Flow<List<Track>> = db.tracks().observeRecent()
    fun observeMostPlayed(): Flow<List<Track>> = db.tracks().observeMostPlayed()
    fun observeRecentlyAdded(): Flow<List<Track>> = db.tracks().observeRecentlyAdded()

    suspend fun search(q: String) = db.tracks().search(q.trim())
    suspend fun getTrack(id: Long) = db.tracks().getById(id)
    suspend fun setFavorite(id: Long, fav: Boolean) = db.tracks().setFavorite(id, fav)
    suspend fun setCover(id: Long, uri: String?) = db.tracks().setCover(id, uri)
    suspend fun updateTrack(track: Track) = db.tracks().update(track)
    suspend fun recordPlay(id: Long) = db.tracks().recordPlay(id)
    suspend fun count() = db.tracks().count()

    suspend fun scanMediaStore(): Int = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.MIME_TYPE
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC}!=0 AND ${MediaStore.Audio.Media.DURATION}>1000"
        context.contentResolver.query(
            collection, projection, selection, null,
            "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        )?.use { c ->
            val idC = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumIdC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val artistIdC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
            val yearC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val trackC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val addedC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            // Preserve user edits
            val existing = db.tracks().getAll().associateBy { it.id }

            while (c.moveToNext()) {
                val id = c.getLong(idC)
                val uri = ContentUris.withAppendedId(collection, id).toString()
                val prev = existing[id]
                tracks += Track(
                    id = id,
                    title = c.getString(titleC) ?: "Unknown",
                    artist = c.getString(artistC) ?: "Unknown",
                    album = c.getString(albumC) ?: "Unknown",
                    durationMs = c.getLong(durC),
                    path = c.getString(dataC) ?: "",
                    uri = uri,
                    albumId = c.getLong(albumIdC),
                    artistId = c.getLong(artistIdC),
                    year = c.getInt(yearC),
                    trackNumber = c.getInt(trackC),
                    dateAdded = c.getLong(addedC) * 1000,
                    customCoverUri = prev?.customCoverUri,
                    playCount = prev?.playCount ?: 0,
                    lastPlayed = prev?.lastPlayed ?: 0,
                    isFavorite = prev?.isFavorite ?: false,
                    rating = prev?.rating ?: 0,
                    userTitle = prev?.userTitle,
                    userArtist = prev?.userArtist,
                    userAlbum = prev?.userAlbum,
                    userGenre = prev?.userGenre,
                    genre = prev?.genre ?: ""
                )
            }
        }
        db.tracks().clear()
        if (tracks.isNotEmpty()) db.tracks().upsertAll(tracks)
        tracks.size
    }

    fun albumArtUri(albumId: Long): Uri =
        ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/albumart"),
            albumId
        )
}
