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
        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.header).text = "Настройки"
        val content = findViewById<LinearLayout>(R.id.content)
        val store = AetherApp.instance.library
        fun title(t: String) = content.addView(TextView(this).apply {
            text = t; setTextColor(0xFFF2F2F5.toInt()); textSize = 16f; setPadding(0, 16, 0, 8)
        })
        title("Стиль стекла (без неона)")
        listOf("frost" to "Матовое", "ultra" to "Ультра", "clear" to "Прозрачное").forEach { (k, label) ->
            content.addView(MaterialButton(this).apply {
                text = if (store.glassStyle() == k) "$label ✓" else label
                setOnClickListener {
                    store.setGlassStyle(k)
                    Toast.makeText(this@SettingsActivity, label, Toast.LENGTH_SHORT).show()
                    recreate()
                }
            })
        }
        title("Библиотека")
        content.addView(MaterialButton(this).apply {
            text = "Очистить избранное"
            setOnClickListener {
                getSharedPreferences("aether", MODE_PRIVATE).edit().remove("favs").apply()
                Toast.makeText(this@SettingsActivity, "Готово", Toast.LENGTH_SHORT).show()
            }
        })
        title("О приложении")
        content.addView(TextView(this).apply {
            text = "Aether Music 1.2\nЛокальный плеер · матовое стекло\nБез облака · Media3\nИнтерфейс на русском"
            setTextColor(0xFFAAAAAA.toInt()); textSize = 13f
        })
    }
}
