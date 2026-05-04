package edu.moravian.csci215.finalproject395_truthfulcheckers

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import edu.moravian.csci215.finalproject395_truthfulcheckers.audio.SoundManager
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.PlayerColor
import edu.moravian.csci215.finalproject395_truthfulcheckers.screens.*
import edu.moravian.csci215.finalproject395_truthfulcheckers.theme.TruthfulCheckersTheme
import edu.moravian.csci215.finalproject395_truthfulcheckers.theme.getStrings
import edu.moravian.csci215.finalproject395_truthfulcheckers.viewmodel.GameViewModel
import edu.moravian.csci215.finalproject395_truthfulcheckers.viewmodel.OnlineGameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.imageResource
import org.koin.compose.koinInject
import truthfulcheckers.composeapp.generated.resources.Res
import truthfulcheckers.composeapp.generated.resources.spritesheet

enum class TruthfulCheckersScreen {
    Home,
    GameMode,
    Setup,
    MainGame,
    Results,
    Instructions,
    Settings,
    OnlineMenu,
    CreateRoom,
    JoinRoom,
    OnlineGame
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val navController = rememberNavController()

    val viewModel: GameViewModel = koinInject()
    val onlineViewModel: OnlineGameViewModel = koinInject()
    val soundManager: SoundManager = koinInject()

    val uiState by viewModel.uiState.collectAsState()
    val onlineState by onlineViewModel.uiState.collectAsState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val scope = rememberCoroutineScope()

    val strings = getStrings(uiState.selectedLanguage)

    var lastNonSettingsRoute by remember {
        mutableStateOf(TruthfulCheckersScreen.Home.name)
    }

    val currentRoute = backStackEntry?.destination?.route ?: TruthfulCheckersScreen.Home.name

    LaunchedEffect(currentRoute) {
        if (currentRoute != TruthfulCheckersScreen.Settings.name) {
            lastNonSettingsRoute = currentRoute
        }
    }

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
        TruthfulCheckersScreen.OnlineMenu -> strings.onlineMultiplayer
        TruthfulCheckersScreen.CreateRoom -> strings.createRoom
        TruthfulCheckersScreen.JoinRoom -> strings.joinRoom
        TruthfulCheckersScreen.OnlineGame -> strings.onlineMultiplayer
    }

    LaunchedEffect(currentScreen) {
        val isGameRelated =
            currentScreen == TruthfulCheckersScreen.MainGame ||
                    currentScreen == TruthfulCheckersScreen.Results ||
                    currentScreen == TruthfulCheckersScreen.OnlineGame

        if (isGameRelated) {
            soundManager.startBackgroundMusic()
        } else if (currentScreen != TruthfulCheckersScreen.Setup) {
            soundManager.stopBackgroundMusic()
        }
    }

    TruthfulCheckersTheme(themeName = uiState.selectedTheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(screenTitle) },
                        navigationIcon = {
                            val canPop = navController.previousBackStackEntry != null

                            if (canPop) {
                                IconButton(
                                    onClick = {
                                        if (currentScreen == TruthfulCheckersScreen.OnlineGame) {
                                            onlineViewModel.leaveRoom()
                                        }

                                        if (currentScreen == TruthfulCheckersScreen.Settings) {
                                            navController.navigate(lastNonSettingsRoute) {
                                                popUpTo(TruthfulCheckersScreen.Settings.name) {
                                                    inclusive = true
                                                }
                                                launchSingleTop = true
                                            }
                                        } else {
                                            navController.popBackStack()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = strings.back
                                    )
                                }
                            }
                        },
                        actions = {
                            if (currentScreen != TruthfulCheckersScreen.Settings) {
                                IconButton(
                                    onClick = {
                                        navController.navigate(TruthfulCheckersScreen.Settings.name) {
                                            launchSingleTop = true
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = strings.settings
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
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
                            viewModel = viewModel,
                            onStartClick = {
                                scope.launch {
                                    navigateWithLoading(
                                        navController = navController,
                                        route = TruthfulCheckersScreen.GameMode.name,
                                        viewModel = viewModel
                                    )
                                }
                            },
                            onInstructionsClick = {
                                navController.navigate(TruthfulCheckersScreen.Instructions.name)
                            },
                            onSettingsClick = {
                                navController.navigate(TruthfulCheckersScreen.Settings.name) {
                                    launchSingleTop = true
                                }
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
                                scope.launch {
                                    navigateWithLoading(
                                        navController = navController,
                                        route = TruthfulCheckersScreen.Setup.name,
                                        viewModel = viewModel
                                    )
                                }
                            },
                            onOnlineSelected = {
                                navController.navigate(TruthfulCheckersScreen.OnlineMenu.name)
                            }
                        )
                    }

                    composable(route = TruthfulCheckersScreen.OnlineMenu.name) {
                        OnlineMenuScreen(
                            selectedLanguage = uiState.selectedLanguage,
                            onCreateRoom = {
                                navController.navigate(TruthfulCheckersScreen.CreateRoom.name)
                            },
                            onJoinRoom = {
                                navController.navigate(TruthfulCheckersScreen.JoinRoom.name)
                            }
                        )
                    }

                    composable(route = TruthfulCheckersScreen.CreateRoom.name) {
                        CreateRoomScreen(
                            gameViewModel = viewModel,
                            onlineViewModel = onlineViewModel,
                            onRoomCreated = {
                                navController.navigate(TruthfulCheckersScreen.OnlineGame.name) {
                                    popUpTo(TruthfulCheckersScreen.OnlineMenu.name) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable(route = TruthfulCheckersScreen.JoinRoom.name) {
                        JoinRoomScreen(
                            selectedLanguage = uiState.selectedLanguage,
                            viewModel = onlineViewModel,
                            onRoomJoined = {
                                navController.navigate(TruthfulCheckersScreen.OnlineGame.name) {
                                    popUpTo(TruthfulCheckersScreen.OnlineMenu.name) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable(route = TruthfulCheckersScreen.OnlineGame.name) {
                        OnlineMainGameScreen(
                            gameViewModel = viewModel,
                            onlineViewModel = onlineViewModel,
                            onGameEnd = {
                                navController.navigate(TruthfulCheckersScreen.Results.name) {
                                    launchSingleTop = true
                                }
                            },
                            onLeave = {
                                onlineViewModel.leaveRoom()
                                navController.navigate(TruthfulCheckersScreen.Home.name) {
                                    popUpTo(TruthfulCheckersScreen.Home.name) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable(route = TruthfulCheckersScreen.Setup.name) {
                        SetupScreen(
                            viewModel = viewModel,
                            onStartGame = { p1, p2, _, diff ->
                                viewModel.setPlayerNames(p1, p2)
                                viewModel.setDifficulty(diff)
                                viewModel.resetGame()

                                scope.launch {
                                    navigateWithLoading(
                                        navController = navController,
                                        route = TruthfulCheckersScreen.MainGame.name,
                                        viewModel = viewModel
                                    )
                                }
                            }
                        )
                    }

                    composable(route = TruthfulCheckersScreen.MainGame.name) {
                        MainGameScreen(
                            viewModel = viewModel,
                            onGameEnd = {
                                navController.navigate(TruthfulCheckersScreen.Results.name) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable(route = TruthfulCheckersScreen.Results.name) {
                        val winnerColor =
                            if (onlineState.gameState.roomCode.isNotBlank() &&
                                (onlineState.gameState.winner != null || onlineState.gameState.tie)
                            ) {
                                onlineState.gameState.winner
                            } else {
                                uiState.winner
                            }

                        val winnerName =
                            if (onlineState.gameState.roomCode.isNotBlank() &&
                                (onlineState.gameState.winner != null || onlineState.gameState.tie)
                            ) {
                                when (winnerColor) {
                                    PlayerColor.RED -> onlineState.gameState.redPlayerName
                                    PlayerColor.BLUE -> onlineState.gameState.bluePlayerName
                                    null -> ""
                                }
                            } else {
                                when (winnerColor) {
                                    PlayerColor.RED -> uiState.player1Name
                                    PlayerColor.BLUE -> uiState.player2Name
                                    null -> ""
                                }
                            }

                        ResultsScreen(
                            winnerName = winnerName,
                            winnerColor = winnerColor,
                            selectedLanguage = uiState.selectedLanguage,
                            onPlayAgain = {
                                if (onlineState.gameState.roomCode.isNotBlank()) {
                                    onlineViewModel.leaveRoom()
                                    navController.navigate(TruthfulCheckersScreen.OnlineMenu.name) {
                                        popUpTo(TruthfulCheckersScreen.Home.name)
                                    }
                                } else {
                                    viewModel.resetGame()
                                    navController.navigate(TruthfulCheckersScreen.MainGame.name) {
                                        popUpTo(TruthfulCheckersScreen.Home.name)
                                    }
                                }
                            },
                            onHomeClick = {
                                if (onlineState.gameState.roomCode.isNotBlank()) {
                                    onlineViewModel.leaveRoom()
                                }

                                navController.navigate(TruthfulCheckersScreen.Home.name) {
                                    popUpTo(TruthfulCheckersScreen.Home.name) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
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

suspend fun navigateWithLoading(
    navController: NavController,
    route: String,
    viewModel: GameViewModel
) {
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