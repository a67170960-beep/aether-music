package ru.aether.music
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class PlaylistsActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("aether_playlists", MODE_PRIVATE) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple)
        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.header).text = "Плейлисты"
        render()
    }
    private fun names() = prefs.getStringSet("names", mutableSetOf("Избранное", "Тренировка", "Ночь"))!!.toMutableSet()
    private fun render() {
        val content = findViewById<LinearLayout>(R.id.content)
        content.removeAllViews()
        content.addView(MaterialButton(this).apply {
            text = "Новый плейлист"
            setOnClickListener {
                val input = EditText(this@PlaylistsActivity)
                AlertDialog.Builder(this@PlaylistsActivity).setTitle("Название").setView(input)
                    .setPositiveButton("Создать") { _, _ ->
                        val n = input.text.toString().trim()
                        if (n.isNotEmpty()) {
                            val s = names(); s.add(n)
                            prefs.edit().putStringSet("names", s).apply(); render()
                        }
                    }.setNegativeButton("Отмена", null).show()
            }
        })
        names().sorted().forEach { name ->
            content.addView(TextView(this).apply {
                text = "▢  $name"; setTextColor(0xFFF2F2F5.toInt()); textSize = 16f; setPadding(8, 16, 8, 16)
                setOnClickListener { Toast.makeText(this@PlaylistsActivity, name, Toast.LENGTH_SHORT).show() }
            })
        }
    }
}
