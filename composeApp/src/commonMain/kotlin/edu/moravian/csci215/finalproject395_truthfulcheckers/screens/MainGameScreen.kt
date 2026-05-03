package edu.moravian.csci215.finalproject395_truthfulcheckers.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.*
import edu.moravian.csci215.finalproject395_truthfulcheckers.theme.AppStrings
import edu.moravian.csci215.finalproject395_truthfulcheckers.theme.getStrings
import edu.moravian.csci215.finalproject395_truthfulcheckers.viewmodel.GameViewModel
import org.jetbrains.compose.resources.imageResource
import truthfulcheckers.composeapp.generated.resources.Res
import truthfulcheckers.composeapp.generated.resources.spritesheet

@Composable
fun MainGameScreen(viewModel: GameViewModel, onGameEnd: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val strings = getStrings(state.selectedLanguage)

    LaunchedEffect(state.winner, state.isTie) {
        if (state.winner != null || state.isTie) {
            onGameEnd()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val isLandscape = maxWidth > maxHeight
        
        if (state.isCoinFlipping || state.firstPlayerMessage != null) {
            CoinFlipOverlay(state.isCoinFlipping, state.firstPlayerMessage, strings)
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                TurnInfoBar(
                    currentPlayer = state.currentPlayer,
                    player1Name = state.player1Name,
                    player2Name = state.player2Name,
                    remainingTime = state.remainingTime, 
                    onForfeit = { viewModel.forfeit() },
                    strings = strings
                )

                if (isLandscape) {
                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PlayerInfoVertical(
                            name = state.player2Name,
                            color = Color.Blue,
                            isCurrentTurn = state.currentPlayer == PlayerColor.BLUE,
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
                                board = state.board,
                                selectedPosition = state.selectedPosition,
                                validMoves = state.validMoves,
                                boardStyle = state.selectedBoardStyle,
                                onCellClick = { viewModel.onCellClick(it) }
                            )
                        }

                        PlayerInfoVertical(
                            name = state.player1Name,
                            color = Color.Red,
                            isCurrentTurn = state.currentPlayer == PlayerColor.RED,
                            strings = strings,
                            modifier = Modifier.width(120.dp)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PlayerInfoHorizontal(
                            name = state.player2Name,
                            color = Color.Blue,
                            isCurrentTurn = state.currentPlayer == PlayerColor.BLUE,
                            strings = strings
                        )
                        
                        Spacer(Modifier.weight(1f))

                        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                            Board(
                                board = state.board,
                                selectedPosition = state.selectedPosition,
                                validMoves = state.validMoves,
                                boardStyle = state.selectedBoardStyle,
                                onCellClick = { viewModel.onCellClick(it) }
                            )
                        }

                        Spacer(Modifier.weight(1f))

                        PlayerInfoHorizontal(
                            name = state.player1Name,
                            color = Color.Red,
                            isCurrentTurn = state.currentPlayer == PlayerColor.RED,
                            strings = strings
                        )
                    }
                }
            }
        }

        if (state.showQuestion) {
            QuestionOverlay(
                question = state.currentQuestion,
                errorMessage = state.errorMessage,
                onAnswer = { viewModel.onAnswerQuestion(it) },
                onCancel = { viewModel.cancelMove() },
                isLandscape = isLandscape,
                strings = strings
            )
        }
    }
}

@Composable
fun PlayerInfoVertical(name: String, color: Color, isCurrentTurn: Boolean, strings: AppStrings, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentTurn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isCurrentTurn) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = name, style = MaterialTheme.typography.titleMedium, maxLines = 2, textAlign = TextAlign.Center)
            if (isCurrentTurn) {
                Spacer(Modifier.height(8.dp))
                Text(text = strings.yourTurn, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PlayerInfoHorizontal(name: String, color: Color, isCurrentTurn: Boolean, strings: AppStrings) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentTurn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isCurrentTurn) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = name, style = MaterialTheme.typography.titleLarge)
            if (isCurrentTurn) {
                Text(text = strings.yourTurn, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TurnInfoBar(currentPlayer: PlayerColor, player1Name: String, player2Name: String, remainingTime: Int?, onForfeit: () -> Unit, strings: AppStrings) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(60.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (currentPlayer == PlayerColor.RED) player1Name else player2Name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                if (remainingTime != null) {
                    Text(
                        text = "${remainingTime}s",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (remainingTime <= 5) Color.Yellow else MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Button(
                onClick = onForfeit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFC62828),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .height(44.dp)
                    .widthIn(min = 120.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
            ) {
                Text(
                    text = strings.forfeit.uppercase(),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    ),
                    textAlign = TextAlign.Center
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
    boardStyle: String,
    onCellClick: (Position) -> Unit
) {
    val darkTileColor = when (boardStyle) {
        "Modern" -> Color(0xFF37474F)
        "High Contrast" -> Color.Black
        else -> Color(0xFF4E342E)
    }
    val lightTileColor = when (boardStyle) {
        "Modern" -> Color(0xFFCFD8DC)
        "High Contrast" -> Color.White
        else -> Color(0xFFD7CCC8)
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        for (row in 0..7) {
            Row(modifier = Modifier.weight(1f)) {
                for (col in 0..7) {
                    val position = Position(row, col)
                    val piece = board[row][col]
                    val isSelected = selectedPosition == position
                    val isValidTarget = validMoves.contains(position)
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(if ((row + col) % 2 != 0) darkTileColor else lightTileColor)
                            .border(if (isSelected) 4.dp else 0.dp, Color.Yellow)
                            .clickable { onCellClick(position) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isValidTarget) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(0.8f)
                                    .background(Color.Yellow.copy(alpha = 0.4f), CircleShape)
                            )
                        }
                        
                        piece?.let { 
                            CheckerPiece(it)
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

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
            Text(
                text = "👑",
                fontSize = 24.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-12).dp)
            )
        }
    }
}

@Composable
fun CoinFlipOverlay(isFlipping: Boolean, message: String?, strings: AppStrings) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isFlipping) {
                val infiniteTransition = rememberInfiniteTransition()
                val rotation by infiniteTransition.animateFloat(0f, 360f, infiniteRepeatable(tween(500, easing = LinearEasing)))
                CircularProgressIndicator(modifier = Modifier.size(100.dp).rotate(rotation), color = Color.White)
                Spacer(Modifier.height(24.dp))
                Text(strings.flippingCoin, color = Color.White, style = MaterialTheme.typography.headlineMedium)
            } else if (message != null) {
                Text(message, color = Color.White, style = MaterialTheme.typography.displayMedium)
            }
        }
    }
}

@Composable
fun QuestionOverlay(
    question: TriviaQuestion?, 
    errorMessage: String?,
    onAnswer: (Boolean) -> Unit, 
    onCancel: () -> Unit, 
    isLandscape: Boolean, 
    strings: AppStrings
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(if (isLandscape) 0.6f else 0.9f).wrapContentHeight(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()), 
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.triviaGate, style = MaterialTheme.typography.headlineMedium)
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel Move", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                
                if (errorMessage != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color.Red.copy(alpha = 0.1f)).padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                        Spacer(Modifier.width(8.dp))
                        Text(errorMessage, style = MaterialTheme.typography.bodySmall, color = Color.Red)
                    }
                    Spacer(Modifier.height(16.dp))
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    text = question?.question ?: "Loading...", 
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { onAnswer(true) }, 
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White)
                    ) { Text(strings.trueText) }
                    Button(
                        onClick = { onAnswer(false) }, 
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336), contentColor = Color.White)
                    ) { Text(strings.falseText) }
                }
            }
        }
    }
}
