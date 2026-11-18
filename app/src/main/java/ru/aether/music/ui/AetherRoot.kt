package ru.aether.music.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier.fillMaxSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import ru.aether.music.AetherApp
import ru.aether.music.data.repo.GlassMode
import ru.aether.music.player.PlayerController
import ru.aether.music.ui.components.LiquidBackground
import ru.aether.music.ui.components.LiquidGlassPanel
import ru.aether.music.ui.equalizer.EqualizerScreen
import ru.aether.music.ui.home.HomeScreen
import ru.aether.music.ui.library.LibraryScreen
import ru.aether.music.ui.player.FullPlayerScreen
import ru.aether.music.ui.player.MiniPlayerBar
import ru.aether.music.ui.search.SearchScreen
import ru.aether.music.ui.settings.SettingsScreen
import ru.aether.music.ui.stats.StatsScreen
import ru.aether.music.ui.playlists.PlaylistsScreen

@Composable
fun AetherRoot(player: PlayerController) {
    val nav = rememberNavController()
    val app = AetherApp.instance
    val vm: AetherViewModel = viewModel(
        factory = AetherViewModel.factory(app, player)
    )
    val glassMode by vm.glassMode.collectAsState(initial = GlassMode.EXTREME)
    val playerState by player.state.collectAsState()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route ?: "home"
    val scope = rememberCoroutineScope()

    val tabs = listOf("home", "library", "search", "playlists", "settings")
    val showBottom = route in tabs || route == "equalizer" || route == "stats"

    Box(Modifier.fillMaxSize()) {
        LiquidBackground(glassMode)
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (showBottom && route != "player") {
                    LiquidGlassPanel(
                        modifier = Modifier,
                        radius = 0.dp,
                        mode = glassMode,
                        intensity = 0.8f
                    ) {
                        NavigationBar(containerColor = Color.Transparent) {
                            NavItem("home", "Home", Icons.Default.Home, route, nav)
                            NavItem("library", "Library", Icons.Default.LibraryMusic, route, nav)
                            NavItem("search", "Search", Icons.Default.Search, route, nav)
                            NavItem("playlists", "Lists", Icons.Default.LibraryMusic, route, nav)
                            NavItem("settings", "Settings", Icons.Default.Settings, route, nav)
                        }
                    }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                NavHost(navController = nav, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            vm = vm,
                            glassMode = glassMode,
                            onOpenPlayer = { nav.navigate("player") },
                            onPlayTrack = { list, index -> player.playTracks(list, index) }
                        )
                    }
                    composable("library") {
                        LibraryScreen(
                            vm = vm,
                            glassMode = glassMode,
                            onPlay = { list, i -> player.playTracks(list, i) }
                        )
                    }
                    composable("search") {
                        SearchScreen(
                            vm = vm,
                            glassMode = glassMode,
                            onPlay = { list, i -> player.playTracks(list, i) }
                        )
                    }
                    composable("playlists") {
                        PlaylistsScreen(vm = vm, glassMode = glassMode)
                    }
                    composable("settings") {
                        SettingsScreen(
                            vm = vm,
                            glassMode = glassMode,
                            onEqualizer = { nav.navigate("equalizer") },
                            onStats = { nav.navigate("stats") }
                        )
                    }
                    composable("equalizer") {
                        EqualizerScreen(glassMode = glassMode, onBack = { nav.popBackStack() })
                    }
                    composable("stats") {
                        StatsScreen(vm = vm, glassMode = glassMode, onBack = { nav.popBackStack() })
                    }
                    composable("player") {
                        FullPlayerScreen(
                            player = player,
                            glassMode = glassMode,
                            onBack = { nav.popBackStack() },
                            onToggleFavorite = { id, fav ->
                                scope.launch { vm.setFavorite(id, fav) }
                            }
                        )
                    }
                }

                if (playerState.current != null && route != "player") {
                    MiniPlayerBar(
                        state = playerState,
                        glassMode = glassMode,
                        onOpen = { nav.navigate("player") },
                        onPlayPause = { player.playPause() },
                        onNext = { player.next() },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = if (showBottom) 8.dp else 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    route: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    current: String,
    nav: androidx.navigation.NavHostController
) {
    NavigationBarItem(
        selected = current == route,
        onClick = { nav.navigate(route) { launchSingleTop = true } },
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label) }
    )
}
