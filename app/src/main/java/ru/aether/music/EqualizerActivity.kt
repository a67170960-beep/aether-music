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
        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.header).text = "Эквалайзер"
        val content = findViewById<LinearLayout>(R.id.content)
        try {
            eq = Equalizer(0, 0).apply { enabled = true }
            bass = BassBoost(0, 0).apply { enabled = true; setStrength(0) }
        } catch (e: Exception) {
            Toast.makeText(this, "Эквалайзер недоступен: ${e.message}", Toast.LENGTH_LONG).show()
        }
        val equalizer = eq
        if (equalizer != null) {
            val levelMin = equalizer.bandLevelRange[0]
            val levelMax = equalizer.bandLevelRange[1]
            for (i in 0 until equalizer.numberOfBands) {
                val band = i.toShort()
                val freq = equalizer.getCenterFreq(band) / 1000
                content.addView(TextView(this).apply {
                    text = "$freq Гц"; setTextColor(0xFFAAAAAA.toInt()); textSize = 12f
                })
                content.addView(SeekBar(this).apply {
                    this.max = (levelMax - levelMin).toInt()
                    this.progress = (equalizer.getBandLevel(band) - levelMin).toInt()
                    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                            if (fromUser) equalizer.setBandLevel(band, (p + levelMin).toShort())
                        }
                        override fun onStartTrackingTouch(sb: SeekBar?) {}
                        override fun onStopTrackingTouch(sb: SeekBar?) {}
                    })
                })
            }
        }
        content.addView(TextView(this).apply {
            text = "Усиление баса"; setTextColor(0xFFF2F2F5.toInt()); textSize = 14f; setPadding(0, 24, 0, 8)
        })
        content.addView(SeekBar(this).apply {
            this.max = 1000
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                    if (fromUser) bass?.setStrength(p.toShort())
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        })
        listOf("Ровный", "Рок", "Поп", "Джаз", "Классика", "Бас", "Вокал").forEach { name ->
            content.addView(MaterialButton(this).apply {
                text = name; setOnClickListener { applyPreset(name) }
            })
        }
    }
    private fun applyPreset(name: String) {
        val equalizer = eq ?: return
        val levelMin = equalizer.bandLevelRange[0]
        val levelMax = equalizer.bandLevelRange[1]
        val mid = ((levelMin + levelMax) / 2).toShort()
        for (i in 0 until equalizer.numberOfBands) {
            val band = i.toShort()
            val level: Short = when {
                name == "Ровный" -> mid
                name == "Бас" && i < 3 -> levelMax
                name == "Вокал" && i in 3..6 -> (mid + (levelMax - mid) / 2).toShort()
                name == "Рок" -> if (i < 2 || i > equalizer.numberOfBands - 3) (mid + (levelMax - mid) / 3).toShort() else mid
                else -> (mid + ((i % 3) - 1) * (levelMax - levelMin) / 8).toShort()
            }
            equalizer.setBandLevel(band, level.coerceIn(levelMin, levelMax))
        }
        Toast.makeText(this, name, Toast.LENGTH_SHORT).show()
    }
    override fun onDestroy() { eq?.release(); bass?.release(); super.onDestroy() }
}
