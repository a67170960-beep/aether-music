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
import com.google.android.material.button.MaterialButton
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
    private lateinit var searchInput: EditText
    private lateinit var tabs: TabLayout
    private lateinit var adapter: TrackAdapter

    private var allTracks: List<Track> = emptyList()
    private var shown: List<Track> = emptyList()
    private var tab = 0
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private val handler = Handler(Looper.getMainLooper())
    private val sleepRunnable = Runnable {
        controller?.pause()
        Toast.makeText(this, "Таймер сна", Toast.LENGTH_SHORT).show()
    }
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
        searchInput = findViewById(R.id.searchInput)
        tabs = findViewById(R.id.tabs)

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        greet.text = when {
            hour < 6 -> "Доброй ночи"
            hour < 12 -> "Доброе утро"
            hour < 18 -> "Добрый день"
            else -> "Добрый вечер"
        }

        adapter = TrackAdapter(
            onClick = { i, _ -> playAt(i) },
            onFav = { t ->
                AetherApp.instance.library.toggleFavorite(t.id)
                refreshShown()
            }
        )
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        listOf(R.string.tab_library, R.string.tab_search, R.string.tab_favorites, R.string.tab_more)
            .forEach { tabs.addTab(tabs.newTab().setText(it)) }
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(t: TabLayout.Tab) {
                tab = t.position
                searchInput.visibility = if (tab == 1) View.VISIBLE else View.GONE
                if (tab == 3) showMore()
                refreshShown()
            }
            override fun onTabUnselected(t: TabLayout.Tab) {}
            override fun onTabReselected(t: TabLayout.Tab) { if (t.position == 3) showMore() }
        })

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = refreshShown()
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        })

        findViewById<MaterialButton>(R.id.btnScan).setOnClickListener { ensurePermissionAndScan() }
        findViewById<ImageButton>(R.id.btnPrev).setOnClickListener { controller?.seekToPreviousMediaItem() }
        findViewById<ImageButton>(R.id.btnNext).setOnClickListener { controller?.seekToNextMediaItem() }
        findViewById<ImageButton>(R.id.btnShuffle).setOnClickListener {
            val c = controller ?: return@setOnClickListener
            c.shuffleModeEnabled = !c.shuffleModeEnabled
            Toast.makeText(this, if (c.shuffleModeEnabled) "Перемешивание вкл" else "Перемешивание выкл", Toast.LENGTH_SHORT).show()
        }
        findViewById<ImageButton>(R.id.btnRepeat).setOnClickListener {
            val c = controller ?: return@setOnClickListener
            c.repeatMode = when (c.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
            Toast.makeText(this, when (c.repeatMode) {
                Player.REPEAT_MODE_ALL -> "Повтор всего"
                Player.REPEAT_MODE_ONE -> "Повтор одного"
                else -> "Повтор выкл"
            }, Toast.LENGTH_SHORT).show()
        }
        btnPlay.setOnClickListener {
            val c = controller ?: return@setOnClickListener
            if (c.isPlaying) c.pause() else c.play()
            updatePlayButton()
        }
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                if (fromUser) controller?.seekTo(p.toLong())
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
        findViewById<View>(R.id.playerBar).setOnClickListener {
            startActivity(Intent(this, PlayerActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        connectController()
        ensurePermissionAndScan()
    }

    private fun showMore() {
        val items = arrayOf(
            "Эквалайзер", "Плейлисты", "Статистика", "Настройки",
            "Сканировать библиотеку",
            "Сон через 15 мин", "Сон через 30 мин", "Сон через 60 мин", "Отменить таймер сна",
            "Сортировка: название", "Сортировка: исполнитель", "Сортировка: недавно"
        )
        AlertDialog.Builder(this).setTitle("Ещё").setItems(items) { _, w ->
            when (w) {
                0 -> startActivity(Intent(this, EqualizerActivity::class.java))
                1 -> startActivity(Intent(this, PlaylistsActivity::class.java))
                2 -> startActivity(Intent(this, StatsActivity::class.java))
                3 -> startActivity(Intent(this, SettingsActivity::class.java))
                4 -> scanLibrary()
                5 -> scheduleSleep(15)
                6 -> scheduleSleep(30)
                7 -> scheduleSleep(60)
                8 -> { handler.removeCallbacks(sleepRunnable); Toast.makeText(this, "Таймер отменён", Toast.LENGTH_SHORT).show() }
                9 -> { AetherApp.instance.library.setSortMode("title"); scanLibrary() }
                10 -> { AetherApp.instance.library.setSortMode("artist"); scanLibrary() }
                11 -> { AetherApp.instance.library.setSortMode("date"); scanLibrary() }
            }
        }.show()
    }

    private fun scheduleSleep(min: Int) {
        handler.removeCallbacks(sleepRunnable)
        handler.postDelayed(sleepRunnable, min * 60_000L)
        Toast.makeText(this, "Сон через $min мин", Toast.LENGTH_SHORT).show()
    }

    private fun ensurePermissionAndScan() {
        val need = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED)
                need += Manifest.permission.READ_MEDIA_AUDIO
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                need += Manifest.permission.POST_NOTIFICATIONS
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            need += Manifest.permission.READ_EXTERNAL_STORAGE
        if (need.isNotEmpty()) permissionLauncher.launch(need.toTypedArray()) else scanLibrary()
    }

    private fun scanLibrary() {
        status.text = "Сканирование…"
        val store = AetherApp.instance.library
        val result = mutableListOf<Track>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DURATION
        )
        val sort = when (store.sortMode()) {
            "artist" -> "${MediaStore.Audio.Media.ARTIST} COLLATE NOCASE ASC"
            "date" -> "${MediaStore.Audio.Media.DATE_ADDED} DESC"
            else -> "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        }
        contentResolver.query(collection, projection, "${MediaStore.Audio.Media.IS_MUSIC}!=0", null, sort)?.use { c ->
            val idC = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            while (c.moveToNext()) {
                val id = c.getLong(idC)
                result += Track(
                    id, c.getString(titleC) ?: "Без названия",
                    c.getString(artistC) ?: "Неизвестный", c.getString(albumC) ?: "",
                    android.content.ContentUris.withAppendedId(collection, id).toString(),
                    c.getLong(durC), store.isFavorite(id), store.playCount(id)
                )
            }
        }
        allTracks = result
        store.tracks = result
        status.text = "Треков: ${allTracks.size}"
        refreshShown()
    }

    private fun refreshShown() {
        shown = when (tab) {
            1 -> {
                val q = searchInput.text?.toString()?.trim().orEmpty().lowercase()
                if (q.isEmpty()) allTracks else allTracks.filter {
                    it.title.lowercase().contains(q) || it.artist.lowercase().contains(q) || it.album.lowercase().contains(q)
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
                    nowTitle.text = player.mediaMetadata.title?.toString() ?: getString(R.string.nothing)
                    nowArtist.text = player.mediaMetadata.artist?.toString() ?: ""
                    updatePlayButton()
                }
                override fun onIsPlayingChanged(isPlaying: Boolean) = updatePlayButton()
            })
            handler.post(tick)
        }, MoreExecutors.directExecutor())
    }

    private fun playAt(index: Int) {
        val c = controller ?: return
        if (shown.isEmpty()) return
        val items = shown.map { t ->
            MediaItem.Builder().setMediaId(t.id.toString()).setUri(t.uri)
                .setMediaMetadata(MediaMetadata.Builder().setTitle(t.title).setArtist(t.artist).setAlbumTitle(t.album).build())
                .build()
        }
        AetherApp.instance.library.bumpPlay(shown[index].id)
        c.setMediaItems(items, index.coerceIn(0, items.lastIndex), 0L)
        c.prepare(); c.play(); updatePlayButton()
    }

    private fun updatePlayButton() {
        btnPlay.setImageResource(
            if (controller?.isPlaying == true) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        )
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        handler.removeCallbacks(sleepRunnable)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controller = null
        super.onDestroy()
    }
}
