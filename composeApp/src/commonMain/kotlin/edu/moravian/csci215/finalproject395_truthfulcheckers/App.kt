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
import edu.moravian.csci215.finalproject395_truthfulcheckers.screens.*
import edu.moravian.csci215.finalproject395_truthfulcheckers.theme.TruthfulCheckersTheme
import edu.moravian.csci215.finalproject395_truthfulcheckers.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.imageResource
import org.koin.compose.koinInject
import truthfulcheckers.composeapp.generated.resources.Res
import truthfulcheckers.composeapp.generated.resources.spritesheet

enum class TruthfulCheckersScreen(val title: String) {
    Home("Truthful Checkers"),
    GameMode("Select Mode"),
    Setup("Game Setup"),
    MainGame("Battle Board"),
    Results("Game Over")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val navController = rememberNavController()
    val viewModel: GameViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val scope = rememberCoroutineScope()
    
    val currentScreen = TruthfulCheckersScreen.entries.find { 
        it.name == backStackEntry?.destination?.route 
    } ?: TruthfulCheckersScreen.Home

    TruthfulCheckersTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(currentScreen.title) },
                        navigationIcon = {
                            if (navController.previousBackStackEntry != null) {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                            onStartClick = { 
                                scope.launch { navigateWithLoading(navController, TruthfulCheckersScreen.GameMode.name, viewModel) }
                            },
                            onInstructionsClick = { /* Show instructions */ },
                            onSettingsClick = { /* Navigate to settings */ }
                        )
                    }

                    composable(route = TruthfulCheckersScreen.GameMode.name) {
                        GameModeScreen(
                            onModeSelected = { _ ->
                                scope.launch { navigateWithLoading(navController, TruthfulCheckersScreen.Setup.name, viewModel) }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(route = TruthfulCheckersScreen.Setup.name) {
                        SetupScreen(
                            onStartGame = { _, _, _, diff ->
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
                            winner = uiState.winner,
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

            // Global Loading Overlay
            if (uiState.isLoading) {
                LoadingOverlay()
            }
        }
    }
}

suspend fun navigateWithLoading(navController: androidx.navigation.NavController, route: String, viewModel: GameViewModel) {
    viewModel.setLoading(true)
    delay(800) // Brief delay to show the animation
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
            
            // Draw Column 2, Row 0 (Mixed/Loading face)
            drawImage(
                image = spriteBitmap,
                srcOffset = IntOffset(2 * spriteWidth, 0),
                srcSize = IntSize(spriteWidth, spriteHeight),
                dstSize = IntSize(size.width.toInt(), size.height.toInt())
            )
        }
    }
}
