package ru.aether.music

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple)
        findViewById<TextView>(R.id.header).text = "Settings"
        val content = findViewById<LinearLayout>(R.id.content)
        val store = AetherApp.instance.library

        fun addTitle(t: String) {
            content.addView(TextView(this).apply {
                text = t
                setTextColor(0xFFFFFFFF.toInt())
                textSize = 16f
                setPadding(0, 16, 0, 8)
            })
        }

        addTitle("Liquid Glass mode")
        listOf("ULTRA", "EXTREME", "FROSTED", "CRYSTAL", "MINIMAL", "PERFORMANCE").forEach { m ->
            content.addView(MaterialButton(this).apply {
                text = if (store.glassMode() == m) "$m ✓" else m
                setOnClickListener {
                    store.setGlassMode(m)
                    Toast.makeText(this@SettingsActivity, "Glass: $m", Toast.LENGTH_SHORT).show()
                    recreate()
                }
            })
        }

        addTitle("Library")
        content.addView(MaterialButton(this).apply {
            text = "Clear favorites"
            setOnClickListener {
                getSharedPreferences("aether", MODE_PRIVATE).edit().remove("favs").apply()
                Toast.makeText(this@SettingsActivity, "Favorites cleared", Toast.LENGTH_SHORT).show()
            }
        })

        addTitle("About")
        content.addView(TextView(this).apply {
            text = "Aether Music 1.1.0\nLocal-first Media3 player\nDark glass UI · no cloud upload\nViews build (stable CI)"
            setTextColor(0xFFAAAAAA.toInt())
            textSize = 13f
        })
    }
}
