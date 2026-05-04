package edu.moravian.csci215.finalproject395_truthfulcheckers.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.PlayerColor
import edu.moravian.csci215.finalproject395_truthfulcheckers.theme.getStrings
import edu.moravian.csci215.finalproject395_truthfulcheckers.viewmodel.GameViewModel
import edu.moravian.csci215.finalproject395_truthfulcheckers.viewmodel.OnlineGameViewModel

@Composable
fun OnlineMainGameScreen(
    gameViewModel: GameViewModel,
    onlineViewModel: OnlineGameViewModel,
    onGameEnd: () -> Unit,
    onLeave: () -> Unit
) {
    val appState by gameViewModel.uiState.collectAsState()
    val state by onlineViewModel.uiState.collectAsState()

    val strings = getStrings(appState.selectedLanguage)
    val gameState = state.gameState
    val board = onlineViewModel.currentBoard

    val myColor = state.myColor
    val isMyTurn = myColor != null && gameState.currentPlayer == myColor

    LaunchedEffect(gameState.winner, gameState.tie) {
        if (gameState.winner != null || gameState.tie) {
            onGameEnd()
        }
    }

    if (state.isExiting) {
        AlertDialog(
            onDismissRequest = onLeave,
            title = { Text(strings.roomClosed) },
            text = { Text(state.errorMessage ?: strings.roomClosedMessage) },
            confirmButton = {
                Button(onClick = onLeave) {
                    Text("OK")
                }
            }
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val isLandscape = maxWidth > maxHeight

        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${strings.room}: ${gameState.roomCode}",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = when (myColor) {
                                PlayerColor.RED -> "${strings.you}: RED"
                                PlayerColor.BLUE -> "${strings.you}: BLUE"
                                null -> strings.spectating
                            },
                            color = when (myColor) {
                                PlayerColor.RED -> Color.Red
                                PlayerColor.BLUE -> Color.Blue
                                null -> MaterialTheme.colorScheme.onSecondaryContainer
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "${strings.category}: ${gameState.selectedCategoryName}",
                        style = MaterialTheme.typography.labelMedium
                    )

                    Text(
                        text = if (isMyTurn) strings.yourTurn else strings.opponentsTurn,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isMyTurn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            TurnInfoBar(
                currentPlayer = gameState.currentPlayer,
                player1Name = gameState.redPlayerName,
                player2Name = if (gameState.blueJoined) gameState.bluePlayerName else strings.waiting,
                remainingTime = null,
                onForfeit = { onlineViewModel.forfeit() },
                strings = strings
            )

            if (!gameState.blueJoined) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text(strings.waitingForOpponent)
                        Text(
                            text = "${strings.shareCode}: ${gameState.roomCode}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                if (isLandscape) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PlayerInfoVertical(
                            name = gameState.bluePlayerName,
                            color = Color.Blue,
                            isCurrentTurn = gameState.currentPlayer == PlayerColor.BLUE,
                            strings = strings,
                            modifier = Modifier.width(120.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .aspectRatio(1f)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Board(
                                board = board,
                                selectedPosition = state.selectedPosition,
                                validMoves = state.validMoves,
                                boardStyle = appState.selectedBoardStyle,
                                onCellClick = { onlineViewModel.onCellClick(it) }
                            )
                        }

                        PlayerInfoVertical(
                            name = gameState.redPlayerName,
                            color = Color.Red,
                            isCurrentTurn = gameState.currentPlayer == PlayerColor.RED,
                            strings = strings,
                            modifier = Modifier.width(120.dp)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PlayerInfoHorizontal(
                            name = gameState.bluePlayerName,
                            color = Color.Blue,
                            isCurrentTurn = gameState.currentPlayer == PlayerColor.BLUE,
                            strings = strings
                        )

                        Spacer(Modifier.weight(1f))

                        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                            Board(
                                board = board,
                                selectedPosition = state.selectedPosition,
                                validMoves = state.validMoves,
                                boardStyle = appState.selectedBoardStyle,
                                onCellClick = { onlineViewModel.onCellClick(it) }
                            )
                        }

                        Spacer(Modifier.weight(1f))

                        PlayerInfoHorizontal(
                            name = gameState.redPlayerName,
                            color = Color.Red,
                            isCurrentTurn = gameState.currentPlayer == PlayerColor.RED,
                            strings = strings
                        )
                    }
                }
            }
        }

        if (state.showLocalQuestion) {
            QuestionOverlay(
                question = state.localQuestion,
                errorMessage = state.errorMessage,
                onAnswer = { onlineViewModel.onAnswerQuestion(it) },
                onCancel = { onlineViewModel.cancelMove() },
                isLandscape = isLandscape,
                strings = strings
            )
        }

        if (state.isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}