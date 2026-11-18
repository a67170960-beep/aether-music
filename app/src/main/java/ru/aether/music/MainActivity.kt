package ru.aether.music

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier.fillMaxSize
import androidx.core.content.ContextCompat
import ru.aether.music.player.PlayerController
import ru.aether.music.ui.AetherRoot
import ru.aether.music.ui.theme.AetherTheme

class MainActivity : ComponentActivity() {
    lateinit var playerController: PlayerController
        private set

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* scan can be triggered from UI */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playerController = PlayerController(this)
        requestMediaPermissions()
        setContent {
            AetherTheme {
                Surface(Modifier.fillMaxSize()) {
                    LaunchedEffect(Unit) { playerController.connect() }
                    AetherRoot(playerController)
                }
            }
        }
    }

    private fun requestMediaPermissions() {
        val needed = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) needed += Manifest.permission.READ_MEDIA_AUDIO
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) needed += Manifest.permission.POST_NOTIFICATIONS
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) needed += Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (needed.isNotEmpty()) permissionLauncher.launch(needed.toTypedArray())
    }

    override fun onStart() {
        super.onStart()
        playerController.connect()
    }

    override fun onStop() {
        // keep service; controller stays for mini player while activity visible
        super.onStop()
    }

    override fun onDestroy() {
        if (isFinishing) playerController.release()
        super.onDestroy()
    }
}
