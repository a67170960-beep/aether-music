package ru.aether.music
import android.content.ComponentName
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

class PlayerActivity : AppCompatActivity() {
    private var future: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var seek: SeekBar
    private lateinit var pos: TextView
    private lateinit var dur: TextView
    private lateinit var btnPlay: ImageButton
    private val tick = object : Runnable {
        override fun run() {
            val c = controller
            if (c != null && c.duration > 0) {
                seek.max = c.duration.toInt()
                seek.progress = c.currentPosition.toInt()
                pos.text = formatMs(c.currentPosition)
                dur.text = formatMs(c.duration)
            }
            handler.postDelayed(this, 400)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        seek = findViewById(R.id.seek); pos = findViewById(R.id.pos); dur = findViewById(R.id.dur); btnPlay = findViewById(R.id.btnPlay)
        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<ImageButton>(R.id.btnPrev).setOnClickListener { controller?.seekToPreviousMediaItem() }
        findViewById<ImageButton>(R.id.btnNext).setOnClickListener { controller?.seekToNextMediaItem() }
        btnPlay.setOnClickListener {
            val c = controller ?: return@setOnClickListener
            if (c.isPlaying) c.pause() else c.play(); updateBtn()
        }
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) { if (fromUser) controller?.seekTo(p.toLong()) }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
        future = MediaController.Builder(this, SessionToken(this, ComponentName(this, PlaybackService::class.java))).buildAsync()
        future?.addListener({
            controller = runCatching { future?.get() }.getOrNull()
            controller?.addListener(object : Player.Listener {
                override fun onEvents(player: Player, events: Player.Events) {
                    findViewById<TextView>(R.id.title).text = player.mediaMetadata.title?.toString() ?: "Ничего не играет"
                    findViewById<TextView>(R.id.artist).text = player.mediaMetadata.artist?.toString() ?: ""
                    updateBtn()
                }
            })
            handler.post(tick)
        }, MoreExecutors.directExecutor())
    }
    private fun updateBtn() {
        btnPlay.setImageResource(if (controller?.isPlaying == true) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play)
    }
    override fun onDestroy() {
        handler.removeCallbacks(tick); future?.let { MediaController.releaseFuture(it) }; super.onDestroy()
    }
}
