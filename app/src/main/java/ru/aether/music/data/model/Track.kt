package ru.aether.music.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val path: String,
    val uri: String,
    val albumId: Long = 0,
    val artistId: Long = 0,
    val year: Int = 0,
    val trackNumber: Int = 0,
    val genre: String = "",
    val dateAdded: Long = 0,
    val customCoverUri: String? = null,
    val playCount: Int = 0,
    val lastPlayed: Long = 0,
    val isFavorite: Boolean = false,
    val rating: Int = 0,
    val userTitle: String? = null,
    val userArtist: String? = null,
    val userAlbum: String? = null,
    val userGenre: String? = null,
) {
    val displayTitle: String get() = userTitle ?: title
    val displayArtist: String get() = userArtist ?: artist
    val displayAlbum: String get() = userAlbum ?: album
    val displayGenre: String get() = userGenre ?: genre
}
