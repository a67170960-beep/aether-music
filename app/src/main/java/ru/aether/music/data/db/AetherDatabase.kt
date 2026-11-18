package ru.aether.music.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.aether.music.data.model.PlayEvent
import ru.aether.music.data.model.Playlist
import ru.aether.music.data.model.PlaylistTrack
import ru.aether.music.data.model.Track

@Database(
    entities = [Track::class, Playlist::class, PlaylistTrack::class, PlayEvent::class],
    version = 1,
    exportSchema = false
)
abstract class AetherDatabase : RoomDatabase() {
    abstract fun tracks(): TrackDao
    abstract fun playlists(): PlaylistDao
    abstract fun stats(): StatsDao

    companion object {
        @Volatile private var instance: AetherDatabase? = null

        fun get(context: Context): AetherDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AetherDatabase::class.java,
                    "aether_music.db"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
    }
}
