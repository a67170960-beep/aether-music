package ru.aether.music

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class EqualizerActivity : AppCompatActivity() {
    private var eq: Equalizer? = null
    private var bass: BassBoost? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple)
        findViewById<TextView>(R.id.header).text = "Equalizer"
        val content = findViewById<LinearLayout>(R.id.content)

        try {
            // session 0 = output mix; works on many devices
            eq = Equalizer(0, 0).apply { enabled = true }
            bass = BassBoost(0, 0).apply { enabled = true; setStrength(0) }
        } catch (e: Exception) {
            Toast.makeText(this, "EQ unavailable: ${e.message}", Toast.LENGTH_LONG).show()
        }

        val equalizer = eq
        if (equalizer != null) {
            val min = equalizer.bandLevelRange[0]
            val max = equalizer.bandLevelRange[1]
            for (i in 0 until equalizer.numberOfBands) {
                val band = i.toShort()
                val freq = equalizer.getCenterFreq(band) / 1000
                val label = TextView(this).apply {
                    text = "${freq} Hz"
                    setTextColor(0xFFAAAAAA.toInt())
                    textSize = 12f
                }
                content.addView(label)
                val bar = SeekBar(this).apply {
                    max = (max - min).toInt()
                    progress = (equalizer.getBandLevel(band) - min).toInt()
                    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                            if (fromUser) equalizer.setBandLevel(band, (p + min).toShort())
                        }
                        override fun onStartTrackingTouch(sb: SeekBar?) {}
                        override fun onStopTrackingTouch(sb: SeekBar?) {}
                    })
                }
                content.addView(bar)
            }
        }

        val bassLabel = TextView(this).apply {
            text = "Bass boost"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 14f
            setPadding(0, 24, 0, 8)
        }
        content.addView(bassLabel)
        val bassBar = SeekBar(this).apply {
            max = 1000
            progress = 0
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                    if (fromUser) bass?.setStrength(p.toShort())
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }
        content.addView(bassBar)

        val presets = listOf("Flat", "Rock", "Pop", "Jazz", "Classical", "Bass", "Vocal")
        presets.forEach { name ->
            val b = MaterialButton(this).apply {
                text = name
                setOnClickListener { applyPreset(name) }
            }
            content.addView(b)
        }
    }

    private fun applyPreset(name: String) {
        val equalizer = eq ?: return
        val min = equalizer.bandLevelRange[0]
        val max = equalizer.bandLevelRange[1]
        val mid = ((min + max) / 2).toShort()
        for (i in 0 until equalizer.numberOfBands) {
            val band = i.toShort()
            val level = when {
                name == "Flat" -> mid
                name == "Bass" && i < 3 -> max
                name == "Vocal" && i in 3..6 -> (mid + (max - mid) / 2).toShort()
                name == "Rock" -> if (i < 2 || i > equalizer.numberOfBands - 3) (mid + (max - mid) / 3).toShort() else mid
                else -> (mid + ((i % 3) - 1) * (max - min) / 8).toShort()
            }
            equalizer.setBandLevel(band, level.coerceIn(min, max))
        }
        Toast.makeText(this, name, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        eq?.release()
        bass?.release()
        super.onDestroy()
    }
}
