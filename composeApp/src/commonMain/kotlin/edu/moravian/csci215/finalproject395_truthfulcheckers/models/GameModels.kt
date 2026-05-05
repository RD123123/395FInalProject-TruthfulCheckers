package edu.moravian.csci215.finalproject395_truthfulcheckers.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents the two possible sides in a game of Checkers.
 * Red typically moves first and starts at the top of the board (rows 0-2).
 * Blue moves second and starts at the bottom of the board (rows 5-7).
 */
@Serializable
enum class PlayerColor {
    RED,
    BLUE,
}

/**
 * Defines the visual "mood" of a piece on the board.
 * - HAPPY: Safe, no immediate threats.
 * - SCARED: Vulnerable to being jumped by an opponent.
 * - MAD: Has an offensive jump available.
 * - CONFUSED: The player controlling this piece just answered a trivia question incorrectly.
 */
@Serializable
enum class Emotion {
    HAPPY,
    SCARED,
    MAD,
    CONFUSED,
}

/**
 * Represents a single checker piece on the board.
 * @property color Determines who owns the piece.
 * @property king If true, the piece can move and jump both forwards and backwards.
 * @property emotion The current visual state of the piece.
 */
@Serializable
data class Piece(
    val color: PlayerColor = PlayerColor.RED,
    val king: Boolean = false,
    val emotion: Emotion = Emotion.HAPPY,
)

/**
 * A simple coordinate representation for the 8x8 grid.
 */
@Serializable
data class Position(
    val row: Int = 0,
    val col: Int = 0,
)

/**
 * Represents an intended move that is awaiting verification.
 * In Truthful Checkers, a move cannot be finalized until the trivia question is answered.
 */
@Serializable
data class PendingMove(
    val from: Position = Position(),
    val to: Position = Position(),
)

/**
 * Room Entity mapping to a cached OpenTDB trivia question.
 * @property isUsed Ensures questions are not repeated during a single play session.
 */
@Serializable
@Entity(tableName = "trivia_questions")
data class TriviaQuestion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String = "",
    val correctAnswer: Boolean = false,
    val category: String = "General",
    val categoryId: Int = 0,
    val difficulty: String = "Medium",
    val isUsed: Boolean = false,
)

/**
 * A standard metadata container for OpenTDB categories.
 */
@Serializable
data class TriviaCategory(
    val id: Int = 9, // 9 represents "General Knowledge" in the OpenTDB API
    val name: String = "General Knowledge",
)

/**
 * Room Entity for recording match history and player performance.
 */
@Entity(tableName = "game_stats")
data class GameStats(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val winnerName: String,
    val totalMoves: Int,
    val accuracy: Double,
    val date: Long = 0L,
)

/**
 * Room Entity representing a snapshot of an active game.
 * Allows users to close the app and resume their local match later.
 * @property id Forced to '1' to ensure only a single active session exists at any time.
 * @property boardData A JSON-serialized representation of the 8x8 List<List<Piece?>> grid.
 */
@Entity(tableName = "game_sessions")
data class GameSession(
    @PrimaryKey val id: Int = 1,
    val player1Name: String,
    val player2Name: String,
    val currentPlayer: PlayerColor,
    val selectedCategoryName: String,
    val selectedCategoryId: Int,
    val isVsAi: Boolean = true,
    val boardData: String,
)

/**
 * Defines the lifecycle of an online multiplayer lobby.
 */
@Serializable
enum class OnlineRoomStatus {
    WAITING, // Room created, waiting for Blue to join
    ACTIVE, // Both players connected, game in progress
    FINISHED, // Game over (win/tie/forfeit)
    CLOSED, // Host disbanded the lobby
}

/**
 * The synchronized payload representing the entire state of an online match.
 * Pushed to and pulled from the remote database (e.g., Firebase) to keep both devices in sync.
 *
 * @property boardData JSON string of the current board layout.
 * @property answeringPlayer The color of the player currently locked in the trivia screen.
 * @property blueJoined True once the guest has successfully connected.
 * @property isMultiJumpActive Forces the current player to continue jumping if possible.
 */
@Serializable
data class OnlineGameState(
    val roomCode: String = "",
    val boardData: String = "",
    val currentPlayer: PlayerColor = PlayerColor.RED,
    val winner: PlayerColor? = null,
    val tie: Boolean = false,
    val pendingMove: PendingMove? = null,
    val answeringPlayer: PlayerColor? = null,
    val redPlayerName: String = "",
    val bluePlayerName: String = "",
    val blueJoined: Boolean = false,
    val selectedCategoryId: Int = 9,
    val selectedCategoryName: String = "General Knowledge",
    val difficulty: String = "Medium",
    val status: OnlineRoomStatus = OnlineRoomStatus.WAITING,
    val forfeitBy: PlayerColor? = null,
    val lastUpdate: Long = 0L,
    val isMultiJumpActive: Boolean = false,
    val selectedMultiJumpPosition: Position? = null,
)
