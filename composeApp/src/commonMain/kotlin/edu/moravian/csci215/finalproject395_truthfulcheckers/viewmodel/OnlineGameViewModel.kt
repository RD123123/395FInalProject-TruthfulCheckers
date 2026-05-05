package edu.moravian.csci215.finalproject395_truthfulcheckers.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.moravian.csci215.finalproject395_truthfulcheckers.audio.SoundManager
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.GameRepository
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.OnlineGameRepository
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.Emotion
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.OnlineGameState
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.OnlineRoomStatus
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.PendingMove
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.Piece
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.PlayerColor
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.Position
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.TriviaCategory
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.TriviaQuestion
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.abs

/**
 * Represents the local UI state for an online multiplayer match.
 *
 * @property gameState The synchronized online state fetched from the remote database.
 * @property myColor The assigned color for the local player (Red for host, Blue for guest).
 * @property selectedPosition The coordinates of the piece the local player currently has tapped.
 * @property validMoves A list of valid destination coordinates for the locally selected piece.
 * @property localQuestion The trivia question currently being presented to the local player.
 * @property showLocalQuestion True if the trivia overlay should be visible on this specific device.
 * @property isLoading True when waiting for network requests (creating/joining rooms, fetching trivia).
 * @property errorMessage Holds any connection or room-related error messages to display.
 * @property isRoomCreated True once the host has successfully generated a room on the server.
 * @property isRoomJoined True once a guest has successfully connected to an existing room.
 * @property isExiting True when the match is forcefully closed or disconnected, triggering a navigation pop.
 */
data class OnlineUiState(
    val gameState: OnlineGameState = OnlineGameState(),
    val myColor: PlayerColor? = null,
    val selectedPosition: Position? = null,
    val validMoves: List<Position> = emptyList(),
    val localQuestion: TriviaQuestion? = null,
    val showLocalQuestion: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRoomCreated: Boolean = false,
    val isRoomJoined: Boolean = false,
    val isExiting: Boolean = false,
)

/**
 * The ViewModel responsible for managing real-time online multiplayer matches.
 * Handles room creation, joining, state synchronization via the OnlineGameRepository,
 * and local move validation before pushing updates to the server.
 */
class OnlineGameViewModel(
    private val onlineRepo: OnlineGameRepository,
    private val gameRepo: GameRepository,
    private val soundManager: SoundManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnlineUiState())
    val uiState: StateFlow<OnlineUiState> = _uiState.asStateFlow()

    private var roomJob: Job? = null
    private var cleanupJob: Job? = null

    /**
     * Safely decodes the JSON board string from the synchronized remote state into a usable List.
     */
    val currentBoard: List<List<Piece?>>
        get() =
            try {
                Json.decodeFromString(_uiState.value.gameState.boardData)
            } catch (_: Exception) {
                createInitialBoard()
            }

    // --- Room Management ---

    /**
     * Initializes a new multiplayer lobby on the server.
     * The host is automatically assigned the RED pieces.
     *
     * @param playerName The host's display name.
     * @param category The specific trivia category chosen for the match.
     * @param difficulty The selected trivia difficulty.
     */
    fun createRoom(
        playerName: String,
        category: TriviaCategory? = null,
        difficulty: String = "Medium",
    ) {
        val roomCode = generateRoomCode()
        val initialBoard = calculateEmotions(createInitialBoard(), PlayerColor.RED, null)

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                isRoomCreated = false,
                isRoomJoined = false,
            )
        }

        viewModelScope.launch {
            onlineRepo
                .createRoom(
                    roomCode = roomCode,
                    playerName = playerName.replace("\n", ""),
                    board = initialBoard,
                    category = category,
                    difficulty = difficulty,
                ).onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRoomCreated = true,
                            myColor = PlayerColor.RED,
                        )
                    }
                    observeRoom(roomCode)
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to create room: ${e.message}",
                        )
                    }
                }
        }
    }

    /**
     * Attempts to connect a guest player to an existing lobby via a 6-digit code.
     * The guest is automatically assigned the BLUE pieces.
     */
    fun joinRoom(
        roomCode: String,
        playerName: String,
    ) {
        val cleanedCode = roomCode.trim()

        if (cleanedCode.length != 6) {
            _uiState.update { it.copy(errorMessage = "Room code must be 6 digits") }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                isRoomCreated = false,
                isRoomJoined = false,
            )
        }

        viewModelScope.launch {
            onlineRepo
                .joinRoom(cleanedCode, playerName.replace("\n", ""))
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRoomJoined = true,
                            myColor = PlayerColor.BLUE,
                        )
                    }
                    observeRoom(cleanedCode)
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to join room: ${e.message}",
                        )
                    }
                }
        }
    }

    /**
     * Subscribes to the remote database to listen for real-time state changes made by the opponent.
     */
    private fun observeRoom(roomCode: String) {
        roomJob?.cancel()

        roomJob =
            viewModelScope.launch {
                onlineRepo.getGameState(roomCode).collect { state ->
                    if (state == null) {
                        if (_uiState.value.isRoomJoined || _uiState.value.isRoomCreated) {
                            _uiState.update {
                                it.copy(
                                    errorMessage = "Room was closed",
                                    isExiting = true,
                                )
                            }
                        }
                        return@collect
                    }

                    val myColor = _uiState.value.myColor
                    val board =
                        try {
                            Json.decodeFromString<List<List<Piece?>>>(state.boardData)
                        } catch (_: Exception) {
                            createInitialBoard()
                        }

                    // Automatically enforce multi-jump sequences across the network
                    val shouldAutoSelectMultiJump =
                        myColor != null &&
                            state.currentPlayer == myColor &&
                            state.isMultiJumpActive &&
                            state.selectedMultiJumpPosition != null

                    val forcedPosition = state.selectedMultiJumpPosition
                    val forcedMoves =
                        if (shouldAutoSelectMultiJump) {
                            getJumpMovesOnly(forcedPosition, board)
                        } else {
                            _uiState.value.validMoves
                        }

                    _uiState.update {
                        it.copy(
                            gameState = state,
                            selectedPosition = if (shouldAutoSelectMultiJump) forcedPosition else it.selectedPosition,
                            validMoves = if (shouldAutoSelectMultiJump) forcedMoves else it.validMoves,
                        )
                    }

                    if (state.status == OnlineRoomStatus.CLOSED) {
                        _uiState.update {
                            it.copy(
                                errorMessage = "The room has been closed.",
                                isExiting = true,
                            )
                        }
                    }

                    if (state.status == OnlineRoomStatus.FINISHED || state.winner != null || state.tie) {
                        scheduleRoomDelete(state.roomCode)
                    }
                }
            }
    }

    /** Cleans up dead lobbies from the remote server after a delay. */
    private fun scheduleRoomDelete(roomCode: String) {
        if (roomCode.isBlank()) return
        cleanupJob?.cancel()

        cleanupJob =
            viewModelScope.launch {
                delay(10_000)
                onlineRepo.deleteRoom(roomCode)
            }
    }

    /** Generates a random 6-digit code for room matchmaking. */
    private fun generateRoomCode(): String = (100000..999999).random().toString()

    private fun createInitialBoard(): List<List<Piece?>> =
        List(8) { row ->
            List(8) { col ->
                when {
                    (row + col) % 2 != 0 && row < 3 -> Piece(PlayerColor.BLUE)
                    (row + col) % 2 != 0 && row > 4 -> Piece(PlayerColor.RED)
                    else -> null
                }
            }
        }

    // --- Core Gameplay Interactions ---

    /**
     * Handles local board taps. Validates that it is the local player's turn
     * before allowing selection or triggering a trivia question.
     */
    fun onCellClick(position: Position) {
        val ui = _uiState.value
        val state = ui.gameState
        val myColor = ui.myColor ?: return
        val board = currentBoard

        // Security checks to prevent out-of-turn or invalid moves
        if (state.currentPlayer != myColor) return
        if (state.winner != null || state.tie) return
        if (state.status != OnlineRoomStatus.ACTIVE) return
        if (!state.blueJoined) return
        if (ui.showLocalQuestion) return
        if (!isValidBoardPosition(position, board)) return

        // Multi-jump enforcement
        if (state.isMultiJumpActive) {
            val forced = state.selectedMultiJumpPosition ?: return

            if (ui.selectedPosition == null) {
                if (position == forced) {
                    _uiState.update {
                        it.copy(
                            selectedPosition = forced,
                            validMoves = getJumpMovesOnly(forced, board),
                        )
                    }
                }
                return
            }

            if (ui.validMoves.contains(position)) {
                triggerQuestion(forced, position)
            }
            return
        }

        val piece = board[position.row][position.col]

        // Standard selection logic
        if (ui.selectedPosition == null) {
            if (piece?.color == myColor) {
                _uiState.update {
                    it.copy(
                        selectedPosition = position,
                        validMoves = getValidMoves(position, board),
                    )
                }
            }
        } else {
            if (ui.validMoves.contains(position)) {
                val from = ui.selectedPosition
                triggerQuestion(from, position)
            } else {
                _uiState.update {
                    it.copy(
                        selectedPosition = null,
                        validMoves = emptyList(),
                    )
                }
            }
        }
    }

    /**
     * Locks in a pending move and synchronizes the 'answeringPlayer' state with the server
     * while the local player attempts to answer the trivia question.
     */
    private fun triggerQuestion(
        from: Position,
        to: Position,
    ) {
        viewModelScope.launch {
            val state = _uiState.value.gameState
            val myColor = _uiState.value.myColor ?: return@launch

            if (state.currentPlayer != myColor) return@launch

            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                )
            }

            val question = gameRepo.getQuestion(state.selectedCategoryId)

            _uiState.update { it.copy(isLoading = false) }

            if (question == null) {
                _uiState.update { it.copy(errorMessage = "Could not load trivia question.") }
                return@launch
            }

            updateRemoteState(
                state.copy(
                    pendingMove = PendingMove(from, to),
                    answeringPlayer = myColor,
                ),
            )

            _uiState.update {
                it.copy(
                    localQuestion = question,
                    showLocalQuestion = true,
                )
            }
        }
    }

    /**
     * Resolves the pending move based on the trivia result.
     * Incorrect answers pass the turn to the opponent over the network.
     */
    fun onAnswerQuestion(answer: Boolean) {
        val ui = _uiState.value
        val state = ui.gameState
        val myColor = ui.myColor ?: return

        if (state.currentPlayer != myColor) return
        if (state.answeringPlayer != myColor) return

        val question = ui.localQuestion ?: return
        val isCorrect = answer == question.correctAnswer

        viewModelScope.launch {
            gameRepo.markQuestionAsUsed(question.id)
        }

        _uiState.update {
            it.copy(
                showLocalQuestion = false,
                localQuestion = null,
            )
        }

        if (isCorrect) {
            val move = state.pendingMove ?: return
            executeMove(move)
        } else {
            val nextPlayer = opposite(state.currentPlayer)
            val updatedBoard = calculateEmotions(currentBoard, nextPlayer, state.currentPlayer)

            updateRemoteState(
                state.copy(
                    boardData = Json.encodeToString(updatedBoard),
                    currentPlayer = nextPlayer,
                    pendingMove = null,
                    answeringPlayer = null,
                    isMultiJumpActive = false,
                    selectedMultiJumpPosition = null,
                ),
            )

            _uiState.update {
                it.copy(
                    selectedPosition = null,
                    validMoves = emptyList(),
                )
            }
        }
    }

    /** Cancels a pending move locally and frees up the network state. */
    fun cancelMove() {
        val state = _uiState.value.gameState
        val myColor = _uiState.value.myColor ?: return

        if (state.answeringPlayer != myColor) return

        updateRemoteState(
            state.copy(
                pendingMove = null,
                answeringPlayer = null,
            ),
        )

        _uiState.update {
            it.copy(
                showLocalQuestion = false,
                localQuestion = null,
                selectedPosition = null,
                validMoves = emptyList(),
            )
        }
    }

    /**
     * Executes the validated move, updates piece positions, plays audio,
     * handles capturing, and pushes the finalized board to the server.
     */
    private fun executeMove(move: PendingMove) {
        val state = _uiState.value.gameState
        val board = currentBoard

        if (!isValidBoardPosition(move.from, board)) return
        if (!isValidBoardPosition(move.to, board)) return

        val from = move.from
        val to = move.to
        val newBoard = board.map { it.toMutableList() }

        val piece = newBoard[from.row][from.col] ?: return

        newBoard[to.row][to.col] = piece
        newBoard[from.row][from.col] = null

        var captured = false

        if (abs(to.row - from.row) == 2) {
            val midR = (from.row + to.row) / 2
            val midC = (from.col + to.col) / 2

            if (midR in 0..7 && midC in 0..7) {
                newBoard[midR][midC] = null
            }

            soundManager.playCaptureSound()
            captured = true
        } else {
            soundManager.playMoveSound()
        }

        // Kinging logic
        if ((piece.color == PlayerColor.RED && to.row == 0) ||
            (piece.color == PlayerColor.BLUE && to.row == 7)
        ) {
            newBoard[to.row][to.col] = piece.copy(king = true)
        }

        val winner = getWinner(newBoard)

        if (winner != null) {
            val finalBoard = calculateEmotions(newBoard, state.currentPlayer, null)

            updateRemoteState(
                state.copy(
                    boardData = Json.encodeToString(finalBoard),
                    pendingMove = null,
                    answeringPlayer = null,
                    isMultiJumpActive = false,
                    selectedMultiJumpPosition = null,
                    winner = winner,
                    status = OnlineRoomStatus.FINISHED,
                ),
            )

            clearLocalSelection()
            return
        }

        if (captured) {
            val nextJumps = getJumpMovesOnly(to, newBoard)

            if (nextJumps.isNotEmpty()) {
                val emotionBoard = calculateEmotions(newBoard, state.currentPlayer, null)

                updateRemoteState(
                    state.copy(
                        boardData = Json.encodeToString(emotionBoard),
                        pendingMove = null,
                        answeringPlayer = null,
                        isMultiJumpActive = true,
                        selectedMultiJumpPosition = to,
                        currentPlayer = state.currentPlayer,
                    ),
                )

                _uiState.update {
                    it.copy(
                        selectedPosition = to,
                        validMoves = nextJumps,
                    )
                }

                return
            }
        }

        val nextPlayer = opposite(state.currentPlayer)
        val emotionBoard = calculateEmotions(newBoard, nextPlayer, null)

        updateRemoteState(
            state.copy(
                boardData = Json.encodeToString(emotionBoard),
                currentPlayer = nextPlayer,
                pendingMove = null,
                answeringPlayer = null,
                isMultiJumpActive = false,
                selectedMultiJumpPosition = null,
            ),
        )

        clearLocalSelection()
    }

    /**
     * Surrenders the match to the opponent and broadcasts the finished state over the network.
     */
    fun forfeit() {
        val state = _uiState.value.gameState
        val myColor = _uiState.value.myColor ?: return
        val winner = opposite(myColor)

        updateRemoteState(
            state.copy(
                winner = winner,
                forfeitBy = myColor,
                status = OnlineRoomStatus.FINISHED,
                pendingMove = null,
                answeringPlayer = null,
                isMultiJumpActive = false,
                selectedMultiJumpPosition = null,
            ),
        )

        scheduleRoomDelete(state.roomCode)
    }

    /** Closes network connections and triggers room deletion. */
    fun leaveRoom() {
        val roomCode = _uiState.value.gameState.roomCode

        roomJob?.cancel()
        cleanupJob?.cancel()

        if (roomCode.isNotBlank()) {
            viewModelScope.launch {
                onlineRepo.closeRoom(roomCode)
                delay(3_000)
                onlineRepo.deleteRoom(roomCode)
            }
        }

        _uiState.update { OnlineUiState() }
    }

    /** Helper to push local state modifications up to the remote database. */
    private fun updateRemoteState(newState: OnlineGameState) {
        viewModelScope.launch {
            onlineRepo
                .updateGameState(newState.roomCode, newState)
                .onFailure { e ->
                    _uiState.update {
                        it.copy(errorMessage = e.message ?: "Failed to update online game.")
                    }
                }
        }
    }

    private fun clearLocalSelection() {
        _uiState.update {
            it.copy(
                selectedPosition = null,
                validMoves = emptyList(),
            )
        }
    }

    private fun getWinner(board: List<List<Piece?>>): PlayerColor? {
        val pieces = board.flatten().filterNotNull()
        val redCount = pieces.count { it.color == PlayerColor.RED }
        val blueCount = pieces.count { it.color == PlayerColor.BLUE }

        return when {
            redCount == 0 -> PlayerColor.BLUE
            blueCount == 0 -> PlayerColor.RED
            else -> null
        }
    }

    /**
     * Determines the visual "Emotion" state of each piece to update the UI models.
     */
    private fun calculateEmotions(
        board: List<List<Piece?>>,
        currentPlayer: PlayerColor,
        confusedPlayer: PlayerColor?,
    ): List<List<Piece?>> =
        board.mapIndexed { row, boardRow ->
            boardRow.mapIndexed { col, piece ->
                piece?.let {
                    val position = Position(row, col)
                    val emotion =
                        when {
                            it.color == confusedPlayer -> Emotion.CONFUSED
                            it.color == currentPlayer && canPieceJump(position, board) -> Emotion.MAD
                            isPieceThreatened(position, board) -> Emotion.SCARED
                            else -> Emotion.HAPPY
                        }

                    it.copy(emotion = emotion)
                }
            }
        }

    private fun canPieceJump(
        pos: Position,
        board: List<List<Piece?>>,
    ): Boolean = getJumpMovesOnly(pos, board).isNotEmpty()

    private fun isPieceThreatened(
        pos: Position,
        board: List<List<Piece?>>,
    ): Boolean {
        if (!isValidBoardPosition(pos, board)) return false
        val piece = board[pos.row][pos.col] ?: return false

        for (row in 0..7) {
            for (col in 0..7) {
                val opponent = board[row][col]

                if (opponent != null && opponent.color != piece.color) {
                    val jumps = getJumpMovesOnly(Position(row, col), board)

                    if (jumps.any { jumpTo ->
                            val midR = (row + jumpTo.row) / 2
                            val midC = (col + jumpTo.col) / 2
                            midR == pos.row && midC == pos.col
                        }
                    ) {
                        return true
                    }
                }
            }
        }

        return false
    }

    private fun opposite(color: PlayerColor): PlayerColor = if (color == PlayerColor.RED) PlayerColor.BLUE else PlayerColor.RED

    private fun isValidBoardPosition(
        pos: Position,
        board: List<List<Piece?>>,
    ): Boolean =
        board.size == 8 &&
            pos.row in 0..7 &&
            pos.col in 0..7 &&
            board[pos.row].size == 8

    /**
     * Generates a list of valid coordinates a piece can move to, prioritizing jumps.
     */
    private fun getValidMoves(
        pos: Position,
        board: List<List<Piece?>>,
    ): List<Position> {
        if (!isValidBoardPosition(pos, board)) return emptyList()

        val piece = board[pos.row][pos.col] ?: return emptyList()
        val jumps = getJumpMovesOnly(pos, board)

        if (jumps.isNotEmpty()) return jumps

        val moves = mutableListOf<Position>()
        val directions =
            if (piece.king) {
                listOf(-1, 1)
            } else if (piece.color == PlayerColor.RED) {
                listOf(-1)
            } else {
                listOf(1)
            }

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

    /**
     * Determines if an offensive capture move is available for the given piece.
     */
    private fun getJumpMovesOnly(
        pos: Position,
        board: List<List<Piece?>>,
    ): List<Position> {
        if (!isValidBoardPosition(pos, board)) return emptyList()

        val piece = board[pos.row][pos.col] ?: return emptyList()
        val moves = mutableListOf<Position>()
        val directions =
            if (piece.king) {
                listOf(-1, 1)
            } else if (piece.color == PlayerColor.RED) {
                listOf(-1)
            } else {
                listOf(1)
            }

        for (dr in directions) {
            for (dc in listOf(-1, 1)) {
                val enemyR = pos.row + dr
                val enemyC = pos.col + dc
                val jumpR = pos.row + dr * 2
                val jumpC = pos.col + dc * 2

                if (
                    enemyR in 0..7 &&
                    enemyC in 0..7 &&
                    jumpR in 0..7 &&
                    jumpC in 0..7 &&
                    board[jumpR][jumpC] == null
                ) {
                    val enemyPiece = board[enemyR][enemyC]

                    if (enemyPiece != null && enemyPiece.color != piece.color) {
                        moves.add(Position(jumpR, jumpC))
                    }
                }
            }
        }

        return moves
    }

    override fun onCleared() {
        super.onCleared()
        roomJob?.cancel()
        cleanupJob?.cancel()
    }
}
