package edu.moravian.csci215.finalproject395_truthfulcheckers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// ---> THIS IS THE MAGIC LINE that links your new files <---
import edu.moravian.csci215.finalproject395_truthfulcheckers.screens.*

// 1. Define your routes
enum class TruthfulCheckersScreen {
    Home, GameMode, Setup, MainGame, Question, Results
}

@Composable
fun App() {
    MaterialTheme {
        // 2. Initialize the NavController
        val navController = rememberNavController()

        // 3. Set up the NavHost
        NavHost(
            navController = navController,
            startDestination = TruthfulCheckersScreen.Home.name,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
        ) {

            composable(route = TruthfulCheckersScreen.Home.name) {
                HomeScreen(onStartClick = { navController.navigate(TruthfulCheckersScreen.GameMode.name) })
            }

            composable(route = TruthfulCheckersScreen.GameMode.name) {
                GameModeScreen(onModeSelected = { navController.navigate(TruthfulCheckersScreen.Setup.name) })
            }

            composable(route = TruthfulCheckersScreen.Setup.name) {
                SetupScreen(onFinishSetup = { navController.navigate(TruthfulCheckersScreen.MainGame.name) })
            }

            composable(route = TruthfulCheckersScreen.MainGame.name) {
                MainGameScreen(
                    onTriggerQuestion = { navController.navigate(TruthfulCheckersScreen.Question.name) },
                    onGameEnd = { navController.navigate(TruthfulCheckersScreen.Results.name) }
                )
            }

            composable(route = TruthfulCheckersScreen.Question.name) {
                QuestionScreen(onAnswerSubmitted = { navController.popBackStack() })
            }

            composable(route = TruthfulCheckersScreen.Results.name) {
                ResultsScreen(onPlayAgain = {
                    navController.navigate(TruthfulCheckersScreen.Home.name) {
                        popUpTo(TruthfulCheckersScreen.Home.name) { inclusive = true }
                    }
                })
            }
        }
    }
}