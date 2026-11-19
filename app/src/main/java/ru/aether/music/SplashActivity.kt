package ru.aether.music
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val content = findViewById<View>(R.id.splashContent)
        val blob = findViewById<View>(R.id.blob)
        content.alpha = 0f
        ObjectAnimator.ofFloat(content, View.ALPHA, 0f, 1f).setDuration(450).start()
        ObjectAnimator.ofFloat(blob, View.SCALE_X, 0.7f, 1f).apply {
            duration = 600; interpolator = DecelerateInterpolator(); start()
        }
        ObjectAnimator.ofFloat(blob, View.SCALE_Y, 0.7f, 1f).apply {
            duration = 600; interpolator = DecelerateInterpolator(); start()
        }
        val style = AetherApp.instance.library.glassStyle()
        findViewById<android.widget.TextView>(R.id.styleHint).text = when (style) {
            "ultra" -> "ультра-стекло"
            "clear" -> "прозрачное стекло"
            else -> "матовое стекло"
        }
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 850)
    }
}
