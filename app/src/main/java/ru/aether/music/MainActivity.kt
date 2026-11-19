package ru.aether.music

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var list: RecyclerView
    private lateinit var status: TextView
    private lateinit var greet: TextView
    private lateinit var nowTitle: TextView
    private lateinit var nowArtist: TextView
    private lateinit var seek: SeekBar
    private lateinit var btnPlay: ImageButton
    private lateinit var searchBox: View
    private lateinit var searchInput: EditText
    private lateinit var tabs: TabLayout

    private lateinit var adapter: TrackAdapter
    private var allTracks: List<Track> = emptyList()
    private var shown: List<Track> = emptyList()
    private var tab = 0

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private val handler = Handler(Looper.getMainLooper())
    private val tick = object : Runnable {
        override fun run() {
            val c = controller
            if (c != null && c.duration > 0) {
                seek.max = c.duration.toInt().coerceAtLeast(1)
                seek.progress = c.currentPosition.toInt().coerceAtLeast(0)
            }
            handler.postDelayed(this, 400)
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { scanLibrary() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        list = findViewById(R.id.list)
        status = findViewById(R.id.status)
        greet = findViewById(R.id.greet)
        nowTitle = findViewById(R.id.nowTitle)
        nowArtist = findViewById(R.id.nowArtist)
        seek = findViewById(R.id.seek)
        btnPlay = findViewById(R.id.btnPlay)
        searchBox = findViewById(R.id.searchBox)
        searchInput = findViewById(R.id.searchInput)
        tabs = findViewById(R.id.tabs)

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        greet.text = when {
            hour < 6 -> "Good night"
            hour < 12 -> "Good morning"
            hour < 18 -> "Good afternoon"
            else -> "Good evening"
        }

        adapter = TrackAdapter(
            onClick = { index, _ -> playAt(index) },
            onFav = { t ->
                AetherApp.instance.library.toggleFavorite(t.id)
                refreshShown()
            }
        )
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        tabs.addTab(tabs.newTab().setText(R.string.tab_library))
        tabs.addTab(tabs.newTab().setText(R.string.tab_search))
        tabs.addTab(tabs.newTab().setText(R.string.tab_favorites))
        tabs.addTab(tabs.newTab().setText(R.string.tab_more))
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(t: TabLayout.Tab) {
                tab = t.position
                searchBox.visibility = if (tab == 1) View.VISIBLE else View.GONE
                if (tab == 3) showMoreMenu()
                refreshShown()
            }
            override fun onTabUnselected(t: TabLayout.Tab) {}
            override fun onTabReselected(t: TabLayout.Tab) {
                if (t.position == 3) showMoreMenu()
            }
        })

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = refreshShown()
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        })

        findViewById<ImageButton>(R.id.btnPrev).setOnClickListener { controller?.seekToPreviousMediaItem() }
        findViewById<ImageButton>(R.id.btnNext).setOnClickListener { controller?.seekToNextMediaItem() }
        findViewById<ImageButton>(R.id.btnShuffle).setOnClickListener {
            val c = controller ?: return@setOnClickListener
            c.shuffleModeEnabled = !c.shuffleModeEnabled
            Toast.makeText(this, if (c.shuffleModeEnabled) "Shuffle on" else "Shuffle off", Toast.LENGTH_SHORT).show()
        }
        findViewById<ImageButton>(R.id.btnRepeat).setOnClickListener {
            val c = controller ?: return@setOnClickListener
            c.repeatMode = when (c.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
            val msg = when (c.repeatMode) {
                Player.REPEAT_MODE_ALL -> "Repeat all"
                Player.REPEAT_MODE_ONE -> "Repeat one"
                else -> "Repeat off"
            }
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
        btnPlay.setOnClickListener {
            val c = controller ?: return@setOnClickListener
            if (c.isPlaying) c.pause() else c.play()
            updatePlayButton()
        }
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) controller?.seekTo(progress.toLong())
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
        findViewById<View>(R.id.playerBar).setOnClickListener {
            startActivity(Intent(this, PlayerActivity::class.java))
        }

        connectController()
        ensurePermissionAndScan()
    }

    private fun showMoreMenu() {
        val items = arrayOf(
            "Equalizer",
            "Playlists",
            "Statistics",
            "Settings",
            "Rescan library",
            "Sleep timer 15 min",
            "Sleep timer 30 min",
            "Sleep timer 60 min",
            "Cancel sleep timer"
        )
        AlertDialog.Builder(this)
            .setTitle("More")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> startActivity(Intent(this, EqualizerActivity::class.java))
                    1 -> startActivity(Intent(this, PlaylistsActivity::class.java))
                    2 -> startActivity(Intent(this, StatsActivity::class.java))
                    3 -> startActivity(Intent(this, SettingsActivity::class.java))
                    4 -> scanLibrary()
                    5 -> scheduleSleep(15)
                    6 -> scheduleSleep(30)
                    7 -> scheduleSleep(60)
                    8 -> {
                        handler.removeCallbacksAndMessages("sleep")
                        Toast.makeText(this, "Sleep cancelled", Toast.LENGTH_SHORT).show()
                    }
                }
            }.show()
    }

    private fun scheduleSleep(min: Int) {
        handler.removeCallbacksAndMessages("sleep")
        handler.postDelayed({
            controller?.pause()
            Toast.makeText(this, "Sleep timer", Toast.LENGTH_SHORT).show()
        }, min * 60_000L)
        // tag workaround: use token
        Toast.makeText(this, "Sleep in $min min", Toast.LENGTH_SHORT).show()
    }

    private fun ensurePermissionAndScan() {
        val need = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED) need += Manifest.permission.READ_MEDIA_AUDIO
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) need += Manifest.permission.POST_NOTIFICATIONS
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) need += Manifest.permission.READ_EXTERNAL_STORAGE
        if (need.isNotEmpty()) permissionLauncher.launch(need.toTypedArray()) else scanLibrary()
    }

    private fun scanLibrary() {
        status.text = "Scanning…"
        val store = AetherApp.instance.library
        val result = mutableListOf<Track>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION
        )
        contentResolver.query(
            collection, projection, "${MediaStore.Audio.Media.IS_MUSIC}!=0", null,
            "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        )?.use { c ->
            val idC = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            while (c.moveToNext()) {
                val id = c.getLong(idC)
                val uri = android.content.ContentUris.withAppendedId(collection, id).toString()
                result += Track(
                    id = id,
                    title = c.getString(titleC) ?: "Unknown",
                    artist = c.getString(artistC) ?: "Unknown",
                    album = c.getString(albumC) ?: "",
                    uri = uri,
                    durationMs = c.getLong(durC),
                    favorite = store.isFavorite(id),
                    playCount = store.playCount(id)
                )
            }
        }
        allTracks = result
        store.tracks = result
        status.text = "Tracks: ${allTracks.size} · glass ${store.glassMode()}"
        refreshShown()
    }

    private fun refreshShown() {
        shown = when (tab) {
            1 -> {
                val q = searchInput.text?.toString()?.trim().orEmpty().lowercase()
                if (q.isEmpty()) allTracks
                else allTracks.filter {
                    it.title.lowercase().contains(q) ||
                        it.artist.lowercase().contains(q) ||
                        it.album.lowercase().contains(q)
                }
            }
            2 -> allTracks.filter { it.favorite }
            else -> allTracks
        }
        adapter.submit(shown)
    }

    private fun connectController() {
        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, token).buildAsync()
        controllerFuture?.addListener({
            controller = runCatching { controllerFuture?.get() }.getOrNull()
            controller?.addListener(object : Player.Listener {
                override fun onEvents(player: Player, events: Player.Events) {
                    val meta = player.mediaMetadata
                    nowTitle.text = meta.title?.toString() ?: "Nothing playing"
                    nowArtist.text = meta.artist?.toString() ?: ""
                    updatePlayButton()
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    updatePlayButton()
                }
            })
            handler.post(tick)
        }, MoreExecutors.directExecutor())
    }

    private fun playAt(index: Int) {
        val c = controller ?: return
        if (shown.isEmpty()) return
        val items = shown.map { t ->
            MediaItem.Builder()
                .setMediaId(t.id.toString())
                .setUri(t.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(t.title)
                        .setArtist(t.artist)
                        .setAlbumTitle(t.album)
                        .build()
                )
                .build()
        }
        val t = shown[index]
        AetherApp.instance.library.bumpPlay(t.id)
        c.setMediaItems(items, index.coerceIn(0, items.lastIndex), 0L)
        c.prepare()
        c.play()
        updatePlayButton()
    }

    private fun updatePlayButton() {
        val playing = controller?.isPlaying == true
        btnPlay.setImageResource(
            if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        )
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controller = null
        super.onDestroy()
    }
}
