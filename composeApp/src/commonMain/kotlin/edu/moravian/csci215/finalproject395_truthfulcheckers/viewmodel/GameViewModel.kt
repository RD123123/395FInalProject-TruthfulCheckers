package edu.moravian.csci215.finalproject395_truthfulcheckers.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.GameRepository
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.abs

data class GameUiState(
    val board: List<List<Piece?>> = emptyList(),
    val currentPlayer: PlayerColor = PlayerColor.RED,
    val selectedPosition: Position? = null,
    val validMoves: List<Position> = emptyList(),
    val currentQuestion: TriviaQuestion? = null,
    val showQuestion: Boolean = false,
    val winner: PlayerColor? = null,
    val pendingMove: Pair<Position, Position>? = null,
    val isConfusedState: PlayerColor? = null,
    val isAiThinking: Boolean = false,
    val difficulty: String = "Medium",
    val isLoading: Boolean = false,
    val isCoinFlipping: Boolean = false,
    val firstPlayerMessage: String? = null
)

class GameViewModel(private val repository: GameRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        resetGame()
        viewModelScope.launch {
            repository.fetchQuestionsFromServer()
        }
    }

    fun setDifficulty(diff: String) {
        _uiState.update { it.copy(difficulty = diff) }
    }

    fun setLoading(loading: Boolean) {
        _uiState.update { it.copy(isLoading = loading) }
    }

    fun resetGame() {
        viewModelScope.launch {
            val initialBoard = List(8) { row ->
                List(8) { col ->
                    when {
                        (row + col) % 2 != 0 && row < 3 -> Piece(PlayerColor.BLUE)
                        (row + col) % 2 != 0 && row > 4 -> Piece(PlayerColor.RED)
                        else -> null
                    }
                }
            }
            
            // Coin flip logic
            _uiState.update { it.copy(isCoinFlipping = true, firstPlayerMessage = null, winner = null) }
            delay(2000) // Flip animation time
            
            val goesFirst = if ((0..1).random() == 0) PlayerColor.RED else PlayerColor.BLUE
            val message = if (goesFirst == PlayerColor.RED) "Red Wins Flip!" else "Blue Wins Flip!"
            
            _uiState.update { 
                it.copy(
                    board = calculateEmotions(initialBoard, goesFirst, null),
                    currentPlayer = goesFirst,
                    firstPlayerMessage = message,
                    isCoinFlipping = false
                ) 
            }
            
            delay(1500) // Show message for a moment
            _uiState.update { it.copy(firstPlayerMessage = null) }
            
            if (goesFirst == PlayerColor.BLUE) {
                runAi()
            }
        }
    }

    private fun calculateEmotions(board: List<List<Piece?>>, currentPlayer: PlayerColor, confusedPlayer: PlayerColor?): List<List<Piece?>> {
        return board.mapIndexed { r, row ->
            row.mapIndexed { c, piece ->
                piece?.let { p ->
                    val pos = Position(r, c)
                    val emotion = when {
                        p.color == confusedPlayer -> Emotion.CONFUSED
                        currentPlayer == p.color && canPieceJump(pos, board) -> Emotion.MAD
                        isPieceThreatened(pos, board) -> Emotion.SCARED
                        else -> Emotion.HAPPY
                    }
                    p.copy(emotion = emotion)
                }
            }
        }
    }

    private fun canPieceJump(pos: Position, board: List<List<Piece?>>): Boolean {
        val piece = board[pos.row][pos.col] ?: return false
        val directions = getMoveDirections(piece)
        
        for (dr in directions) {
            for (dc in listOf(-1, 1)) {
                val enemyR = pos.row + dr
                val enemyC = pos.col + dc
                val jumpR = pos.row + (dr * 2)
                val jumpC = pos.col + (dc * 2)
                
                if (jumpR in 0..7 && jumpC in 0..7) {
                    val enemyPiece = board[enemyR][enemyC]
                    if (enemyPiece != null && enemyPiece.color != piece.color && board[jumpR][jumpC] == null) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun getMoveDirections(piece: Piece): List<Int> {
        return if (piece.isKing) listOf(-1, 1) else if (piece.color == PlayerColor.RED) listOf(-1) else listOf(1)
    }

    private fun isPieceThreatened(pos: Position, board: List<List<Piece?>>): Boolean {
        val piece = board[pos.row][pos.col] ?: return false
        for (r in 0..7) {
            for (c in 0..7) {
                val opponent = board[r][c]
                if (opponent != null && opponent.color != piece.color) {
                    val oppPos = Position(r, c)
                    val oppDirs = getMoveDirections(opponent)
                    for (dr in oppDirs) {
                        for (dc in listOf(-1, 1)) {
                            val midR = r + dr
                            val midC = c + dc
                            val endR = r + dr * 2
                            val endC = c + dc * 2
                            if (midR == pos.row && midC == pos.col && endR in 0..7 && endC in 0..7 && board[endR][endC] == null) {
                                return true
                            }
                        }
                    }
                }
            }
        }
        return false
    }

    fun onCellClick(position: Position) {
        if (_uiState.value.isAiThinking || _uiState.value.winner != null || _uiState.value.isCoinFlipping) return

        val state = _uiState.value
        val piece = state.board[position.row][position.col]

        if (state.selectedPosition == null) {
            if (piece?.color == state.currentPlayer) {
                val moves = getValidMoves(position, state.board)
                _uiState.update { it.copy(selectedPosition = position, validMoves = moves) }
            }
        } else {
            if (state.validMoves.contains(position)) {
                _uiState.update { it.copy(pendingMove = state.selectedPosition to position) }
                triggerQuestion()
            } else {
                _uiState.update { it.copy(selectedPosition = null, validMoves = emptyList()) }
            }
        }
    }

    private fun getValidMoves(pos: Position, board: List<List<Piece?>>): List<Position> {
        val piece = board[pos.row][pos.col] ?: return emptyList()
        val moves = mutableListOf<Position>()
        val directions = getMoveDirections(piece)

        for (dr in directions) {
            for (dc in listOf(-1, 1)) {
                val nextR = pos.row + dr
                val nextC = pos.col + dc
                if (nextR in 0..7 && nextC in 0..7 && board[nextR][nextC] == null) {
                    moves.add(Position(nextR, nextC))
                }
                val jumpR = pos.row + dr * 2
                val jumpC = pos.col + dc * 2
                if (jumpR in 0..7 && jumpC in 0..7 && board[jumpR][jumpC] == null) {
                    val midPiece = board[nextR][nextC]
                    if (midPiece != null && midPiece.color != piece.color) {
                        moves.add(Position(jumpR, jumpC))
                    }
                }
            }
        }
        return moves
    }

    private fun triggerQuestion() {
        viewModelScope.launch {
            val questions = repository.cachedQuestions.first()
            if (questions.isNotEmpty()) {
                _uiState.update { it.copy(currentQuestion = questions.random(), showQuestion = true) }
            } else {
                executeMove()
            }
        }
    }

    fun onAnswerQuestion(answer: Boolean) {
        val state = _uiState.value
        val isCorrect = answer == state.currentQuestion?.correctAnswer
        _uiState.update { it.copy(showQuestion = false) }

        if (isCorrect) {
            executeMove()
        } else {
            val nextPlayer = if (state.currentPlayer == PlayerColor.RED) PlayerColor.BLUE else PlayerColor.RED
            _uiState.update { it.copy(
                isConfusedState = state.currentPlayer,
                board = calculateEmotions(state.board, nextPlayer, state.currentPlayer),
                currentPlayer = nextPlayer,
                selectedPosition = null,
                validMoves = emptyList(),
                pendingMove = null
            ) }
            
            viewModelScope.launch {
                delay(3000)
                _uiState.update { 
                    it.copy(isConfusedState = null, board = calculateEmotions(it.board, it.currentPlayer, null))
                }
                checkAiTurn()
            }
        }
    }

    private fun executeMove() {
        val state = _uiState.value
        val (from, to) = state.pendingMove ?: return
        
        val newBoard = state.board.map { it.toMutableList() }
        val piece = newBoard[from.row][from.col] ?: return
        
        newBoard[to.row][to.col] = piece
        newBoard[from.row][from.col] = null

        if (abs(to.row - from.row) == 2) {
            val midR = (from.row + to.row) / 2
            val midC = (from.col + to.col) / 2
            newBoard[midR][midC] = null
        }
        
        var updatedPiece = piece
        if ((piece.color == PlayerColor.RED && to.row == 0) || (piece.color == PlayerColor.BLUE && to.row == 7)) {
            updatedPiece = piece.copy(isKing = true)
            newBoard[to.row][to.col] = updatedPiece
        }

        val nextPlayer = if (state.currentPlayer == PlayerColor.RED) PlayerColor.BLUE else PlayerColor.RED
        
        _uiState.update { it.copy(
            board = calculateEmotions(newBoard, nextPlayer, null),
            currentPlayer = nextPlayer,
            selectedPosition = null,
            validMoves = emptyList(),
            pendingMove = null,
            isConfusedState = null
        ) }
        
        checkWinCondition(newBoard)
        checkAiTurn()
    }

    private fun checkAiTurn() {
        val state = _uiState.value
        if (state.winner == null && state.currentPlayer == PlayerColor.BLUE) { 
            runAi()
        }
    }

    private fun runAi() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiThinking = true) }
            delay(1500)
            
            val state = _uiState.value
            val allMoves = mutableListOf<Pair<Position, Position>>()
            
            for (r in 0..7) {
                for (c in 0..7) {
                    val piece = state.board[r][c]
                    if (piece?.color == PlayerColor.BLUE) {
                        val from = Position(r, c)
                        getValidMoves(from, state.board).forEach { to ->
                            allMoves.add(from to to)
                        }
                    }
                }
            }

            if (allMoves.isEmpty()) {
                _uiState.update { it.copy(winner = PlayerColor.RED, isAiThinking = false) }
                return@launch
            }

            val selectedMove = when (state.difficulty) {
                "Easy" -> allMoves.random()
                "Hard" -> {
                    val jumpMoves = allMoves.filter { abs(it.first.row - it.second.row) == 2 }
                    if (jumpMoves.isNotEmpty()) jumpMoves.random() else allMoves.random()
                }
                else -> {
                    val jumpMoves = allMoves.filter { abs(it.first.row - it.second.row) == 2 }
                    if (jumpMoves.isNotEmpty() && (0..1).random() == 0) jumpMoves.random() else allMoves.random()
                }
            }

            _uiState.update { it.copy(pendingMove = selectedMove, isAiThinking = false) }
            if ((0..10).random() > 1) { 
                executeMove()
            } else {
                onAnswerQuestion(false)
            }
        }
    }

    private fun checkWinCondition(board: List<List<Piece?>>) {
        val pieces = board.flatten().filterNotNull()
        val redCount = pieces.count { it.color == PlayerColor.RED }
        val blueCount = pieces.count { it.color == PlayerColor.BLUE }

        if (redCount == 0) _uiState.update { it.copy(winner = PlayerColor.BLUE) }
        else if (blueCount == 0) _uiState.update { it.copy(winner = PlayerColor.RED) }
    }
}
