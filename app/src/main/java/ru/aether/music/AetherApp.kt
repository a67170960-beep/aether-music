package ru.aether.music

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import ru.aether.music.data.db.AetherDatabase
import ru.aether.music.data.repo.LibraryRepository
import ru.aether.music.data.repo.PlaylistRepository
import ru.aether.music.data.repo.SettingsRepository
import ru.aether.music.data.repo.StatsRepository

class AetherApp : Application() {
    lateinit var database: AetherDatabase
        private set
    lateinit var libraryRepository: LibraryRepository
        private set
    lateinit var playlistRepository: PlaylistRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var statsRepository: StatsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AetherDatabase.get(this)
        libraryRepository = LibraryRepository(this, database)
        playlistRepository = PlaylistRepository(database)
        settingsRepository = SettingsRepository(this)
        statsRepository = StatsRepository(database)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel),
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "aether_playback"
        lateinit var instance: AetherApp
            private set
    }
}
