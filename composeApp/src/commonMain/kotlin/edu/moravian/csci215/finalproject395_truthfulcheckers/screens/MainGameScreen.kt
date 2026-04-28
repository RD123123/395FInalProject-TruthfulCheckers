package edu.moravian.csci215.finalproject395_truthfulcheckers.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.*
import edu.moravian.csci215.finalproject395_truthfulcheckers.viewmodel.GameViewModel
import org.jetbrains.compose.resources.imageResource
import truthfulcheckers.composeapp.generated.resources.Res
import truthfulcheckers.composeapp.generated.resources.spritesheet

@Composable
fun MainGameScreen(viewModel: GameViewModel, onGameEnd: () -> Unit) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.winner) {
        if (state.winner != null) {
            onGameEnd()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val isLandscape = maxWidth > maxHeight
        
        if (state.isCoinFlipping || state.firstPlayerMessage != null) {
            CoinFlipOverlay(state.isCoinFlipping, state.firstPlayerMessage)
        } else {
            if (isLandscape) {
                Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    PlayerPanel(
                        name = "Player 2",
                        color = Color.Blue,
                        isCurrentTurn = state.currentPlayer == PlayerColor.BLUE,
                        modifier = Modifier.weight(1f)
                    )

                    Box(modifier = Modifier.fillMaxHeight().aspectRatio(1f).padding(8.dp)) {
                        Board(
                            board = state.board,
                            selectedPosition = state.selectedPosition,
                            validMoves = state.validMoves,
                            onCellClick = { viewModel.onCellClick(it) }
                        )
                    }

                    PlayerPanel(
                        name = "Player 1",
                        color = Color.Red,
                        isCurrentTurn = state.currentPlayer == PlayerColor.RED,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    PlayerPanel(
                        name = "Player 2 (Blue)",
                        color = Color.Blue,
                        isCurrentTurn = state.currentPlayer == PlayerColor.BLUE
                    )
                    
                    Spacer(Modifier.weight(1f))

                    Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                        Board(
                            board = state.board,
                            selectedPosition = state.selectedPosition,
                            validMoves = state.validMoves,
                            onCellClick = { viewModel.onCellClick(it) }
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    PlayerPanel(
                        name = "Player 1 (Red)",
                        color = Color.Red,
                        isCurrentTurn = state.currentPlayer == PlayerColor.RED
                    )
                }
            }
        }

        if (state.showQuestion) {
            QuestionOverlay(
                question = state.currentQuestion,
                onAnswer = { viewModel.onAnswerQuestion(it) }
            )
        }
    }
}

@Composable
fun CoinFlipOverlay(isFlipping: Boolean, message: String?) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val spriteBitmap = imageResource(Res.drawable.spritesheet)
            val infiniteTransition = rememberInfiniteTransition()
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )

            if (isFlipping) {
                Text("Flipping Coin...", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(32.dp))
                Canvas(modifier = Modifier.size(200.dp).rotate(rotation)) {
                    val spriteWidth = spriteBitmap.width / 3
                    val spriteHeight = spriteBitmap.height / 4
                    drawImage(
                        image = spriteBitmap,
                        srcOffset = IntOffset(2 * spriteWidth, 0),
                        srcSize = IntSize(spriteWidth, spriteHeight),
                        dstSize = IntSize(size.width.toInt(), size.height.toInt())
                    )
                }
            } else if (message != null) {
                Text(message, style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun PlayerPanel(name: String, color: Color, isCurrentTurn: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentTurn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isCurrentTurn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = if (isCurrentTurn) androidx.compose.foundation.BorderStroke(4.dp, color) else null
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.headlineMedium,
                color = if (isCurrentTurn) MaterialTheme.colorScheme.onPrimary else color
            )
            if (isCurrentTurn) {
                Text(
                    text = "YOUR TURN",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun Board(
    board: List<List<Piece?>>,
    selectedPosition: Position?,
    validMoves: List<Position>,
    onCellClick: (Position) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .border(4.dp, MaterialTheme.colorScheme.outline)
            .background(Color.Black)
    ) {
        for (row in 0..7) {
            Row(modifier = Modifier.weight(1f)) {
                for (col in 0..7) {
                    val isDark = (row + col) % 2 != 0
                    val position = Position(row, col)
                    val isSelected = selectedPosition == position
                    val isValidTarget = validMoves.contains(position)
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(if (isDark) Color(0xFF4E342E) else Color(0xFFD7CCC8))
                            .border(if (isSelected) 4.dp else 0.dp, Color.Yellow)
                            .clickable { onCellClick(position) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isValidTarget) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(0.4f)
                                    .background(Color.Yellow.copy(alpha = 0.5f), CircleShape)
                                    .border(2.dp, Color.Yellow, CircleShape)
                            )
                        }
                        
                        board[row][col]?.let { piece ->
                            CheckerPiece(piece)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CheckerPiece(piece: Piece) {
    val spriteBitmap = imageResource(Res.drawable.spritesheet)
    val spriteRow = when (piece.emotion) {
        Emotion.HAPPY -> 0
        Emotion.SCARED -> 1
        Emotion.MAD -> 2
        Emotion.CONFUSED -> 3
    }
    val spriteCol = if (piece.color == PlayerColor.RED) 0 else 1

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spriteWidth = spriteBitmap.width / 3
            val spriteHeight = spriteBitmap.height / 4
            
            drawImage(
                image = spriteBitmap,
                srcOffset = IntOffset(spriteCol * spriteWidth, spriteRow * spriteHeight),
                srcSize = IntSize(spriteWidth, spriteHeight),
                dstSize = IntSize(size.width.toInt(), size.height.toInt())
            )
        }
        
        if (piece.isKing) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(Color.Yellow, CircleShape)
                    .padding(2.dp)
            ) {
                Text("K", color = Color.Black, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        }
    }
}

@Composable
fun QuestionOverlay(question: TriviaQuestion?, onAnswer: (Boolean) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .padding(24.dp)
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "TRIVIA GATE",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Divider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
                Text(
                    text = question?.question ?: "Loading trivia...",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(48.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { onAnswer(true) },
                        modifier = Modifier.weight(1f).height(80.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        )
                    ) {
                        Text("TRUE", style = MaterialTheme.typography.titleLarge)
                    }
                    Button(
                        onClick = { onAnswer(false) },
                        modifier = Modifier.weight(1f).height(80.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336),
                            contentColor = Color.White
                        )
                    ) {
                        Text("FALSE", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}
