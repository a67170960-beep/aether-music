package ru.aether.music

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

class MainActivity : AppCompatActivity() {
    private lateinit var list: RecyclerView
    private lateinit var status: TextView
    private lateinit var nowTitle: TextView
    private lateinit var nowArtist: TextView
    private lateinit var seek: SeekBar
    private lateinit var btnPlay: ImageButton

    private var tracks: List<Track> = emptyList()
    private lateinit var adapter: TrackAdapter

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
            handler.postDelayed(this, 500)
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
        nowTitle = findViewById(R.id.nowTitle)
        nowArtist = findViewById(R.id.nowArtist)
        seek = findViewById(R.id.seek)
        btnPlay = findViewById(R.id.btnPlay)

        adapter = TrackAdapter { index, _ -> playAt(index) }
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        findViewById<MaterialButton>(R.id.btnScan).setOnClickListener { ensurePermissionAndScan() }
        findViewById<ImageButton>(R.id.btnPrev).setOnClickListener { controller?.seekToPrevious() }
        findViewById<ImageButton>(R.id.btnNext).setOnClickListener { controller?.seekToNext() }
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

        connectController()
        ensurePermissionAndScan()
    }

    private fun ensurePermissionAndScan() {
        val need = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) need += Manifest.permission.READ_MEDIA_AUDIO
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) need += Manifest.permission.POST_NOTIFICATIONS
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            need += Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (need.isNotEmpty()) permissionLauncher.launch(need.toTypedArray())
        else scanLibrary()
    }

    private fun scanLibrary() {
        status.text = "Scanning…"
        val result = mutableListOf<Track>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC}!=0"
        contentResolver.query(
            collection, projection, selection, null,
            "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        )?.use { c ->
            val idC = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            while (c.moveToNext()) {
                val id = c.getLong(idC)
                val uri = android.content.ContentUris.withAppendedId(collection, id).toString()
                result += Track(
                    id = id,
                    title = c.getString(titleC) ?: "Unknown",
                    artist = c.getString(artistC) ?: "Unknown",
                    uri = uri,
                    durationMs = c.getLong(durC)
                )
            }
        }
        tracks = result
        adapter.submit(tracks)
        status.text = "Tracks: ${tracks.size}"
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
            })
            handler.post(tick)
        }, MoreExecutors.directExecutor())
    }

    private fun playAt(index: Int) {
        val c = controller ?: return
        if (tracks.isEmpty()) return
        val items = tracks.map { t ->
            MediaItem.Builder()
                .setMediaId(t.id.toString())
                .setUri(t.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(t.title)
                        .setArtist(t.artist)
                        .build()
                )
                .build()
        }
        c.setMediaItems(items, index.coerceIn(0, items.lastIndex), 0L)
        c.prepare()
        c.play()
        updatePlayButton()
    }

    private fun updatePlayButton() {
        val playing = controller?.isPlaying == true
        btnPlay.setImageResource(
            if (playing) android.R.drawable.ic_media_pause
            else android.R.drawable.ic_media_play
        )
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controller = null
        super.onDestroy()
    }
}
