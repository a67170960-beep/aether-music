package ru.aether.music

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StatsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple)
        findViewById<TextView>(R.id.header).text = "Statistics"
        val content = findViewById<LinearLayout>(R.id.content)
        val store = AetherApp.instance.library
        val tracks = store.tracks

        fun line(t: String) {
            content.addView(TextView(this).apply {
                text = t
                setTextColor(0xFFE0E0E0.toInt())
                textSize = 14f
                setPadding(0, 6, 0, 6)
            })
        }

        line("Tracks in library: ${tracks.size}")
        line("Favorites: ${store.favorites().size}")
        line("Total listen (logged): ${store.totalListenMs() / 60000} min")
        line("")
        line("Most played")
        store.mostPlayed().take(15).forEach {
            line("• ${it.title} — ${it.playCount} plays")
        }
        if (store.mostPlayed().isEmpty()) line("Play something to fill stats.")
    }
}
