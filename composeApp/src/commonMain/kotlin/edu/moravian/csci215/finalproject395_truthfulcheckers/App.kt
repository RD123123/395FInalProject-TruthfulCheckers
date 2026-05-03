package edu.moravian.csci215.finalproject395_truthfulcheckers

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import edu.moravian.csci215.finalproject395_truthfulcheckers.audio.SoundManager
import edu.moravian.csci215.finalproject395_truthfulcheckers.screens.*
import edu.moravian.csci215.finalproject395_truthfulcheckers.theme.TruthfulCheckersTheme
import edu.moravian.csci215.finalproject395_truthfulcheckers.theme.getStrings
import edu.moravian.csci215.finalproject395_truthfulcheckers.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.imageResource
import org.koin.compose.koinInject
import truthfulcheckers.composeapp.generated.resources.Res
import truthfulcheckers.composeapp.generated.resources.spritesheet

enum class TruthfulCheckersScreen {
    Home, GameMode, Setup, MainGame, Results, Instructions, Settings, OnlineBeta
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val navController = rememberNavController()
    val viewModel: GameViewModel = koinInject()
    val soundManager: SoundManager = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val scope = rememberCoroutineScope()
    
    val strings = getStrings(uiState.selectedLanguage)
    
    val currentRoute = backStackEntry?.destination?.route ?: TruthfulCheckersScreen.Home.name
    val currentScreen = try { 
        TruthfulCheckersScreen.valueOf(currentRoute) 
    } catch (e: Exception) { 
        TruthfulCheckersScreen.Home 
    }

    val screenTitle = when (currentScreen) {
        TruthfulCheckersScreen.Home -> strings.appName
        TruthfulCheckersScreen.GameMode -> strings.selectMode
        TruthfulCheckersScreen.Setup -> strings.gameSetup
        TruthfulCheckersScreen.MainGame -> strings.appName
        TruthfulCheckersScreen.Results -> strings.gameOver
        TruthfulCheckersScreen.Instructions -> strings.howToPlay
        TruthfulCheckersScreen.Settings -> strings.settings
        TruthfulCheckersScreen.OnlineBeta -> "Online Beta"
    }

    // Fixed music logic: Only start/stop when transitioning between game-related and menu-related screens
    LaunchedEffect(currentScreen) {
        val isGameRelated = currentScreen == TruthfulCheckersScreen.MainGame || currentScreen == TruthfulCheckersScreen.Results
        if (isGameRelated) {
            // Only start if not already managed (the SoundManager handles duplicate play requests differently depending on implementation)
            soundManager.startBackgroundMusic()
        } else if (currentScreen != TruthfulCheckersScreen.Setup) {
            // We keep it playing during Setup if it was already started, but stop for others
            soundManager.stopBackgroundMusic()
        }
    }

    TruthfulCheckersTheme(themeName = uiState.selectedTheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    // Hide the standard TopAppBar on the MainGame screen to save vertical space
                    if (currentScreen != TruthfulCheckersScreen.MainGame) {
                        CenterAlignedTopAppBar(
                            title = { Text(screenTitle) },
                            navigationIcon = {
                                val canPop = navController.previousBackStackEntry != null
                                if (canPop) {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = strings.back)
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = TruthfulCheckersScreen.Home.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    composable(route = TruthfulCheckersScreen.Home.name) {
                        HomeScreen(
                            viewModel = viewModel,
                            onStartClick = { 
                                scope.launch { navigateWithLoading(navController, TruthfulCheckersScreen.GameMode.name, viewModel) }
                            },
                            onInstructionsClick = { 
                                navController.navigate(TruthfulCheckersScreen.Instructions.name)
                            },
                            onSettingsClick = { 
                                navController.navigate(TruthfulCheckersScreen.Settings.name)
                            }
                        )
                    }

                    composable(route = TruthfulCheckersScreen.Instructions.name) {
                        InstructionsScreen(viewModel = viewModel)
                    }

                    composable(route = TruthfulCheckersScreen.Settings.name) {
                        SettingsScreen(viewModel = viewModel)
                    }

                    composable(route = TruthfulCheckersScreen.GameMode.name) {
                        GameModeScreen(
                            viewModel = viewModel,
                            onModeSelected = { vsAi ->
                                viewModel.setVsAi(vsAi)
                                scope.launch { navigateWithLoading(navController, TruthfulCheckersScreen.Setup.name, viewModel) }
                            },
                            onOnlineSelected = {
                                navController.navigate(TruthfulCheckersScreen.OnlineBeta.name)
                            }
                        )
                    }

                    composable(route = TruthfulCheckersScreen.OnlineBeta.name) {
                        OnlineBetaScreen(viewModel = viewModel)
                    }

                    composable(route = TruthfulCheckersScreen.Setup.name) {
                        SetupScreen(
                            viewModel = viewModel,
                            onStartGame = { p1, p2, _, diff ->
                                viewModel.setPlayerNames(p1, p2)
                                viewModel.setDifficulty(diff)
                                viewModel.resetGame()
                                scope.launch { navigateWithLoading(navController, TruthfulCheckersScreen.MainGame.name, viewModel) }
                            }
                        )
                    }

                    composable(route = TruthfulCheckersScreen.MainGame.name) {
                        MainGameScreen(
                            viewModel = viewModel,
                            onGameEnd = { navController.navigate(TruthfulCheckersScreen.Results.name) }
                        )
                    }

                    composable(route = TruthfulCheckersScreen.Results.name) {
                        ResultsScreen(
                            winnerName = if (uiState.winner == edu.moravian.csci215.finalproject395_truthfulcheckers.models.PlayerColor.RED) uiState.player1Name else uiState.player2Name,
                            winnerColor = uiState.winner,
                            selectedLanguage = uiState.selectedLanguage,
                            onPlayAgain = {
                                viewModel.resetGame()
                                navController.navigate(TruthfulCheckersScreen.MainGame.name) {
                                    popUpTo(TruthfulCheckersScreen.Home.name)
                                }
                            },
                            onHomeClick = {
                                navController.navigate(TruthfulCheckersScreen.Home.name) {
                                    popUpTo(TruthfulCheckersScreen.Home.name) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                LoadingOverlay()
            }
        }
    }
}

suspend fun navigateWithLoading(navController: androidx.navigation.NavController, route: String, viewModel: GameViewModel) {
    viewModel.setLoading(true)
    delay(800)
    navController.navigate(route)
    viewModel.setLoading(false)
}

@Composable
fun LoadingOverlay() {
    val spriteBitmap = imageResource(Res.drawable.spritesheet)
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(120.dp).rotate(rotation)) {
            val spriteWidth = spriteBitmap.width / 3
            val spriteHeight = spriteBitmap.height / 4
            drawImage(
                image = spriteBitmap,
                srcOffset = IntOffset(2 * spriteWidth, 0),
                srcSize = IntSize(spriteWidth, spriteHeight),
                dstSize = IntSize(size.width.toInt(), size.height.toInt())
            )
        }
    }
}
