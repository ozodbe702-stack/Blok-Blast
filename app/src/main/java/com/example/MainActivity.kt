package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.repository.GameRepository
import com.example.model.AppScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LevelsScreen
import com.example.ui.screens.StatsAndShopScreen
import com.example.ui.theme.ArcadeBgDeep
import com.example.ui.theme.ArcadeBgMid
import com.example.ui.theme.ArcadeSurface
import com.example.ui.theme.ArcadeSurfaceVariant
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonGold
import com.example.util.SoundEffectManager
import com.example.viewmodel.BlockBlastViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BlokBlastApp()
            }
        }
    }
}

@Composable
fun BlokBlastApp() {
    val context = LocalContext.current
    val repository = remember {
        val db = AppDatabase.getInstance(context)
        GameRepository(db.gameDao())
    }
    val soundManager = remember { SoundEffectManager(context) }

    val viewModel: BlockBlastViewModel = viewModel(
        factory = BlockBlastViewModel.Factory(repository, soundManager)
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val stats by viewModel.playerStats.collectAsStateWithLifecycle()
    val levelProgress by viewModel.levelProgressList.collectAsStateWithLifecycle()
    val history by viewModel.recentHistory.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = ArcadeBgDeep,
        bottomBar = {
            NavigationBar(
                containerColor = ArcadeSurface,
                contentColor = Color.White
            ) {
                val navItems = listOf(
                    Triple(AppScreen.HOME, "Asosiy", Icons.Filled.Home to Icons.Outlined.Home),
                    Triple(AppScreen.LEVELS, "Bosqichlar", Icons.Filled.GridView to Icons.Outlined.GridView),
                    Triple(AppScreen.GAME, "O'yin", Icons.Filled.SportsEsports to Icons.Outlined.SportsEsports),
                    Triple(AppScreen.STATS_SHOP, "Do'kon", Icons.Filled.EmojiEvents to Icons.Outlined.EmojiEvents)
                )

                navItems.forEach { (screen, label, icons) ->
                    val selected = uiState.currentScreen == screen
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.navigateTo(screen) },
                        icon = {
                            Icon(
                                imageVector = if (selected) icons.first else icons.second,
                                contentDescription = label
                            )
                        },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1A103C),
                            selectedTextColor = NeonGold,
                            indicatorColor = NeonGold,
                            unselectedIconColor = Color.White.copy(alpha = 0.7f),
                            unselectedTextColor = Color.White.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.testTag("nav_tab_${screen.name}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(ArcadeBgDeep, ArcadeBgMid, ArcadeSurfaceVariant.copy(alpha = 0.4f))
                    )
                )
                .padding(innerPadding)
        ) {
            when (uiState.currentScreen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        stats = stats,
                        levelProgress = levelProgress,
                        onStartAdventureLevel = { levelNum -> viewModel.startLevel(levelNum) },
                        onOpenLevels = { viewModel.navigateTo(AppScreen.LEVELS) },
                        onStartClassic = { viewModel.startClassicMode() },
                        onStartDaily = { viewModel.startDailyChallenge() }
                    )
                }

                AppScreen.LEVELS -> {
                    LevelsScreen(
                        levelProgressList = levelProgress,
                        onSelectLevel = { levelNum -> viewModel.startLevel(levelNum) },
                        onBackToHome = { viewModel.navigateTo(AppScreen.HOME) }
                    )
                }

                AppScreen.GAME -> {
                    GameScreen(
                        uiState = uiState,
                        stats = stats,
                        onSelectCandidate = { idx -> viewModel.selectCandidatePiece(idx) },
                        onUpdateDragHover = { idx, r, c -> viewModel.updateDragHover(idx, r, c) },
                        onClearDragHover = { viewModel.clearDragHover() },
                        onPlacePiece = { idx, r, c -> viewModel.placePiece(idx, r, c) },
                        onBoardCellTapped = { r, c -> viewModel.onBoardCellTapped(r, c) },
                        onPowerUpClicked = { powerUp -> viewModel.onPowerUpClicked(powerUp) },
                        onRetryGame = { viewModel.retryCurrentGame() },
                        onNextLevel = { viewModel.startNextLevel() },
                        onBackToHome = { viewModel.navigateTo(AppScreen.HOME) }
                    )
                }

                AppScreen.STATS_SHOP -> {
                    StatsAndShopScreen(
                        stats = stats,
                        history = history,
                        onBuyPowerUp = { powerUp -> viewModel.buyBoosterInShop(powerUp) },
                        onToggleSound = { viewModel.toggleSoundSetting() },
                        onToggleHaptic = { viewModel.toggleHapticSetting() },
                        onBackToHome = { viewModel.navigateTo(AppScreen.HOME) }
                    )
                }
            }
        }
    }
}
