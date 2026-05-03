package edu.moravian.csci215.finalproject395_truthfulcheckers.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.moravian.csci215.finalproject395_truthfulcheckers.audio.SoundManager
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.GameRepository
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.*
import edu.moravian.csci215.finalproject395_truthfulcheckers.theme.getStrings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.abs
import kotlin.random.Random

data class GameUiState(
    val board: List<List<Piece?>> = List(8) { List(8) { null } },
    val currentPlayer: PlayerColor = PlayerColor.RED,
    val selectedPosition: Position? = null,
    val validMoves: List<Position> = emptyList(),
    val currentQuestion: TriviaQuestion? = null,
    val showQuestion: Boolean = false,
    val winner: PlayerColor? = null,
    val isTie: Boolean = false,
    val pendingMove: Pair<Position, Position>? = null,
    val isConfusedState: PlayerColor? = null,
    val isAiThinking: Boolean = false,
    val isVsAi: Boolean = true,
    val difficulty: String = "Medium",
    val isLoading: Boolean = false,
    val isCoinFlipping: Boolean = false,
    val firstPlayerMessage: String? = null,
    val selectedTheme: String = "Warm Tan",
    val selectedBoardStyle: String = "Classic",
    val turnTimerSetting: String = "Off",
    val remainingTime: Int? = null,
    val isMultiJumpActive: Boolean = false,
    val player1Name: String = "Player 1",
    val player2Name: String = "AI Bot",
    val drawCounter: Int = 0,
    val selectedLanguage: String = "English",
    val categories: List<TriviaCategory> = emptyList(),
    val selectedCategory: TriviaCategory? = null,
    val errorMessage: String? = null
)

class GameViewModel(
    private val repository: GameRepository,
    private val soundManager: SoundManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var aiJob: Job? = null

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val cats = repository.getCategories()
            _uiState.update { it.copy(categories = cats) }
            if (cats.isNotEmpty() && _uiState.value.selectedCategory == null) {
                _uiState.update { it.copy(selectedCategory = cats.first()) }
            }
        }
    }

    fun setVsAi(vsAi: Boolean) {
        _uiState.update { it.copy(
            isVsAi = vsAi,
            player2Name = if (vsAi) "AI Bot" else "Player 2"
        ) }
    }

    fun setCategory(category: TriviaCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setLanguage(lang: String) {
        _uiState.update { it.copy(selectedLanguage = lang) }
    }

    fun setPlayerNames(p1: String, p2: String) {
        _uiState.update { it.copy(
            player1Name = p1.ifBlank { "Player 1" },
            player2Name = p2.ifBlank { if (it.isVsAi) "AI Bot" else "Player 2" }
        ) }
    }

    fun setTheme(theme: String) {
        _uiState.update { it.copy(selectedTheme = theme) }
    }

    fun setBoardStyle(style: String) {
        _uiState.update { it.copy(selectedBoardStyle = style) }
    }

    fun setTurnTimer(timer: String) {
        _uiState.update { it.copy(turnTimerSetting = timer) }
    }

    fun setDifficulty(diff: String) {
        _uiState.update { it.copy(difficulty = diff) }
    }

    fun setLoading(loading: Boolean) {
        _uiState.update { it.copy(isLoading = loading) }
    }

    fun resetGame() {
        stopTimer()
        aiJob?.cancel() // Kill any active AI logic before starting a new game
        
        viewModelScope.launch {
            val strings = getStrings(_uiState.value.selectedLanguage)
            val initialBoard = List(8) { row ->
                List(8) { col ->
                    when {
                        (row + col) % 2 != 0 && row < 3 -> Piece(PlayerColor.BLUE)
                        (row + col) % 2 != 0 && row > 4 -> Piece(PlayerColor.RED)
                        else -> null
                    }
                }
            }
            
            soundManager.startBackgroundMusic()
            
            _uiState.update { 
                it.copy(
                    isCoinFlipping = true, 
                    firstPlayerMessage = null, 
                    winner = null, 
                    isTie = false,
                    remainingTime = null,
                    isMultiJumpActive = false,
                    isAiThinking = false,
                    selectedPosition = null,
                    validMoves = emptyList(),
                    pendingMove = null,
                    currentQuestion = null,
                    drawCounter = 0,
                    errorMessage = null
                ) 
            }
            
            delay(2000) 
            
            // True random flip logic to avoid P2 bias
            val goesFirst = if (Random.nextBoolean()) PlayerColor.RED else PlayerColor.BLUE
            val winnerName = if (goesFirst == PlayerColor.RED) _uiState.value.player1Name else _uiState.value.player2Name
            val message = "$winnerName ${strings.winsFlip}"
            
            _uiState.update { 
                it.copy(
                    board = calculateEmotions(initialBoard, goesFirst, null),
                    currentPlayer = goesFirst,
                    firstPlayerMessage = message,
                    isCoinFlipping = false
                ) 
            }
            
            saveSession()
            delay(1500)
            _uiState.update { it.copy(firstPlayerMessage = null) }
            
            startTurnTimer()
            checkAiTurn()
        }
    }

    private fun saveSession() {
        val state = _uiState.value
        viewModelScope.launch {
            try {
                val session = GameSession(
                    player1Name = state.player1Name,
                    player2Name = state.player2Name,
                    currentPlayer = state.currentPlayer,
                    selectedCategoryName = state.selectedCategory?.name ?: "General",
                    selectedCategoryId = state.selectedCategory?.id ?: 9,
                    isVsAi = state.isVsAi,
                    boardData = Json.encodeToString(state.board)
                )
                repository.saveSession(session)
            } catch (e: Exception) { }
        }
    }

    private fun startTurnTimer() {
        stopTimer()
        val setting = _uiState.value.turnTimerSetting
        if (setting == "Off") return

        val seconds = setting.split(" ")[0].toIntOrNull() ?: return
        _uiState.update { it.copy(remainingTime = seconds) }

        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingTime != null && _uiState.value.remainingTime!! > 0) {
                delay(1000)
                _uiState.update { it.copy(remainingTime = it.remainingTime?.minus(1)) }
            }
            if (_uiState.value.remainingTime == 0) {
                onTimeOut()
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.update { it.copy(remainingTime = null) }
    }

    private fun onTimeOut() {
        onAnswerQuestion(false)
    }

    private fun calculateEmotions(board: List<List<Piece?>>, currentPlayer: PlayerColor, confusedPlayer: PlayerColor?): List<List<Piece?>> {
        if (board.isEmpty()) return board
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
        if (board.isEmpty() || pos.row >= board.size || pos.col >= board[0].size) return false
        return getJumpMovesOnly(pos, board).isNotEmpty()
    }

    private fun getJumpMovesOnly(pos: Position, board: List<List<Piece?>>): List<Position> {
        if (board.isEmpty() || pos.row >= board.size || pos.col >= board[0].size) return emptyList()
        val piece = board[pos.row][pos.col] ?: return emptyList()
        val moves = mutableListOf<Position>()
        val directions = if (piece.isKing) listOf(-1, 1) else if (piece.color == PlayerColor.RED) listOf(-1) else listOf(1)
        
        for (dr in directions) {
            for (dc in listOf(-1, 1)) {
                val enemyR = pos.row + dr
                val enemyC = pos.col + dc
                val jumpR = pos.row + (dr * 2)
                val jumpC = pos.col + (dc * 2)
                
                if (jumpR in 0..7 && jumpC in 0..7 && board[jumpR][jumpC] == null) {
                    val enemyPiece = board[enemyR][enemyC]
                    if (enemyPiece != null && enemyPiece.color != piece.color) {
                        moves.add(Position(jumpR, jumpC))
                    }
                }
            }
        }
        return moves
    }

    private fun getMoveDirections(piece: Piece): List<Int> {
        return if (piece.isKing) listOf(-1, 1) else if (piece.color == PlayerColor.RED) listOf(-1) else listOf(1)
    }

    private fun isPieceThreatened(pos: Position, board: List<List<Piece?>>): Boolean {
        if (board.isEmpty() || pos.row >= board.size || pos.col >= board[0].size) return false
        val piece = board[pos.row][pos.col] ?: return false
        for (r in 0..7) {
            for (c in 0..7) {
                val opponent = board[r][c]
                if (opponent != null && opponent.color != piece.color) {
                    val oppJumps = getJumpMovesOnly(Position(r, c), board)
                    if (oppJumps.any { jumpTo -> 
                        val midR = (r + jumpTo.row) / 2
                        val midC = (c + jumpTo.col) / 2
                        midR == pos.row && midC == pos.col
                    }) return true
                }
            }
        }
        return false
    }

    fun onCellClick(position: Position) {
        if (_uiState.value.isAiThinking || _uiState.value.winner != null || _uiState.value.isCoinFlipping) return

        val state = _uiState.value
        if (state.board.isEmpty()) return
        
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
                if (!state.isMultiJumpActive) {
                    _uiState.update { it.copy(selectedPosition = null, validMoves = emptyList()) }
                }
            }
        }
    }

    private fun getValidMoves(pos: Position, board: List<List<Piece?>>): List<Position> {
        if (board.isEmpty() || pos.row >= board.size || pos.col >= board[0].size) return emptyList()
        val piece = board[pos.row][pos.col] ?: return emptyList()
        val jumps = getJumpMovesOnly(pos, board)
        if (jumps.isNotEmpty()) return jumps
        
        val moves = mutableListOf<Position>()
        val directions = getMoveDirections(piece)
        for (dr in directions) {
            for (dc in listOf(-1, 1)) {
                val nextR = pos.row + dr
                val nextC = pos.col + dc
                if (nextR in 0..7 && nextC in 0..7 && board[nextR][nextC] == null) {
                    moves.add(Position(nextR, nextC))
                }
            }
        }
        return moves
    }

    private fun triggerQuestion() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val catId = _uiState.value.selectedCategory?.id ?: 9
            val question = repository.getQuestion(catId)
            
            if (question != null) {
                _uiState.update { it.copy(currentQuestion = question, showQuestion = true, isLoading = false) }
            } else {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Could not fetch trivia questions. Check your connection."
                ) }
                _uiState.update { it.copy(
                    currentQuestion = TriviaQuestion(question = "Is the sky blue?", correctAnswer = true), 
                    showQuestion = true
                ) }
            }
        }
    }

    fun onAnswerQuestion(answer: Boolean) {
        val state = _uiState.value
        val question = state.currentQuestion
        val isCorrect = answer == question?.correctAnswer
        
        if (question != null) {
            viewModelScope.launch {
                repository.markQuestionAsUsed(question.id)
            }
        }

        _uiState.update { it.copy(showQuestion = false) }

        if (isCorrect) {
            _uiState.update { it.copy(currentQuestion = null) } 
            executeMove()
        } else {
            stopTimer()
            _uiState.update { it.copy(currentQuestion = null) } 
            
            val nextPlayer = if (state.currentPlayer == PlayerColor.RED) PlayerColor.BLUE else PlayerColor.RED
            _uiState.update { it.copy(
                isConfusedState = state.currentPlayer,
                board = calculateEmotions(state.board, nextPlayer, state.currentPlayer),
                currentPlayer = nextPlayer,
                selectedPosition = null,
                validMoves = emptyList(),
                pendingMove = null,
                isMultiJumpActive = false
            ) }
            
            saveSession()
            
            viewModelScope.launch {
                delay(3000)
                _uiState.update { 
                    it.copy(isConfusedState = null, board = calculateEmotions(it.board, it.currentPlayer, null))
                }
                startTurnTimer()
                checkAiTurn()
            }
        }
    }

    fun cancelMove() {
        _uiState.update { it.copy(showQuestion = false, selectedPosition = null, validMoves = emptyList(), pendingMove = null) }
    }

    fun forfeit() {
        stopTimer()
        aiJob?.cancel()
        val loser = _uiState.value.currentPlayer
        val winner = if (loser == PlayerColor.RED) PlayerColor.BLUE else PlayerColor.RED
        _uiState.update { it.copy(winner = winner) }
        viewModelScope.launch { repository.clearSession() }
    }

    private fun executeMove() {
        stopTimer()
        val state = _uiState.value
        val (from, to) = state.pendingMove ?: return
        
        val newBoard = state.board.map { it.toMutableList() }
        val piece = newBoard[from.row][from.col] ?: return
        
        newBoard[to.row][to.col] = piece
        newBoard[from.row][from.col] = null

        var captured = false
        if (abs(to.row - from.row) == 2) {
            val midR = (from.row + to.row) / 2
            val midC = (from.col + to.col) / 2
            newBoard[midR][midC] = null
            soundManager.playCaptureSound()
            captured = true
            _uiState.update { it.copy(drawCounter = 0) } 
        } else {
            soundManager.playMoveSound()
            _uiState.update { it.copy(drawCounter = it.drawCounter + 1) }
        }
        
        var updatedPiece = piece
        if ((piece.color == PlayerColor.RED && to.row == 0) || (piece.color == PlayerColor.BLUE && to.row == 7)) {
            updatedPiece = piece.copy(isKing = true)
            newBoard[to.row][to.col] = updatedPiece
        }

        if (captured) {
            val nextJumps = getJumpMovesOnly(to, newBoard)
            if (nextJumps.isNotEmpty()) {
                _uiState.update { it.copy(
                    board = calculateEmotions(newBoard, it.currentPlayer, null),
                    selectedPosition = to,
                    validMoves = nextJumps,
                    pendingMove = null,
                    isMultiJumpActive = true
                ) }
                saveSession()
                startTurnTimer()
                return 
            }
        }

        if (_uiState.value.drawCounter >= 40) {
            _uiState.update { it.copy(isTie = true, winner = null) }
            viewModelScope.launch { repository.clearSession() }
            return
        }

        val nextPlayer = if (state.currentPlayer == PlayerColor.RED) PlayerColor.BLUE else PlayerColor.RED
        _uiState.update { it.copy(
            board = calculateEmotions(newBoard, nextPlayer, null),
            currentPlayer = nextPlayer,
            selectedPosition = null,
            validMoves = emptyList(),
            pendingMove = null,
            isConfusedState = null,
            isMultiJumpActive = false
        ) }
        
        saveSession()
        checkWinCondition(newBoard)
        startTurnTimer()
        checkAiTurn()
    }

    private fun checkAiTurn() {
        val state = _uiState.value
        // Only run AI if BLUE is the current player AND we are in VS AI mode
        if (state.isVsAi && state.winner == null && !state.isTie && state.currentPlayer == PlayerColor.BLUE) { 
            runAi()
        }
    }

    private fun runAi() {
        aiJob?.cancel() 
        aiJob = viewModelScope.launch {
            _uiState.update { it.copy(isAiThinking = true) }
            delay(1500)
            
            var state = _uiState.value
            while (state.isVsAi && state.currentPlayer == PlayerColor.BLUE && state.winner == null && !state.isTie) {
                val allMoves = mutableListOf<Pair<Position, Position>>()
                if (state.isMultiJumpActive && state.selectedPosition != null) {
                    getJumpMovesOnly(state.selectedPosition!!, state.board).forEach { to ->
                        allMoves.add(state.selectedPosition!! to to)
                    }
                } else {
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
                }

                if (allMoves.isEmpty()) {
                    if (!state.isMultiJumpActive) {
                        _uiState.update { it.copy(winner = PlayerColor.RED, isAiThinking = false) }
                        viewModelScope.launch { repository.clearSession() }
                    }
                    break
                }

                val jumps = allMoves.filter { abs(it.first.row - it.second.row) == 2 }
                val selectedMove = if (jumps.isNotEmpty()) jumps.random() else allMoves.random()

                _uiState.update { it.copy(pendingMove = selectedMove) }
                
                // AI executes the move automatically
                executeMove()
                state = _uiState.value // Re-sync state for loop condition
                if (state.isMultiJumpActive) delay(1000) 
            }
            _uiState.update { it.copy(isAiThinking = false) }
        }
    }

    private fun checkWinCondition(board: List<List<Piece?>>) {
        val pieces = board.flatten().filterNotNull()
        val redCount = pieces.count { it.color == PlayerColor.RED }
        val blueCount = pieces.count { it.color == PlayerColor.BLUE }

        if (redCount == 0) {
            _uiState.update { it.copy(winner = PlayerColor.BLUE) }
            viewModelScope.launch { repository.clearSession() }
        } else if (blueCount == 0) {
            _uiState.update { it.copy(winner = PlayerColor.RED) }
            viewModelScope.launch { repository.clearSession() }
        }
    }
}
