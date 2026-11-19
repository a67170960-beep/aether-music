package ru.aether.music
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StatsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple)
        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.header).text = "Статистика"
        val content = findViewById<LinearLayout>(R.id.content)
        val store = AetherApp.instance.library
        fun line(t: String) = content.addView(TextView(this).apply {
            text = t; setTextColor(0xFFE8E8EC.toInt()); textSize = 14f; setPadding(0, 6, 0, 6)
        })
        line("Треков: ${store.tracks.size}")
        line("Избранное: ${store.favorites().size}")
        line("")
        line("Чаще всего")
        val top = store.mostPlayed().take(15)
        if (top.isEmpty()) line("Пока пусто — послушай треки")
        else top.forEach { line("• ${it.title} — ${it.playCount}") }
    }
}
