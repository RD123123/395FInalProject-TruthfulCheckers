package edu.moravian.csci215.finalproject395_truthfulcheckers.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class PlayerColor {
    RED, BLUE
}

@Serializable
enum class Emotion {
    HAPPY, SCARED, MAD, CONFUSED
}

@Serializable
data class Piece(
    val color: PlayerColor = PlayerColor.RED,
    val king: Boolean = false,
    val emotion: Emotion = Emotion.HAPPY
)

@Serializable
data class Position(
    val row: Int = 0,
    val col: Int = 0
)

@Serializable
data class PendingMove(
    val from: Position = Position(),
    val to: Position = Position()
)

@Serializable
@Entity(tableName = "trivia_questions")
data class TriviaQuestion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String = "",
    val correctAnswer: Boolean = false,
    val category: String = "General",
    val categoryId: Int = 0,
    val difficulty: String = "Medium",
    val isUsed: Boolean = false
)

@Serializable
data class TriviaCategory(
    val id: Int = 9,
    val name: String = "General Knowledge"
)

@Entity(tableName = "game_stats")
data class GameStats(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val winnerName: String,
    val totalMoves: Int,
    val accuracy: Double,
    val date: Long = 0L
)

@Entity(tableName = "game_sessions")
data class GameSession(
    @PrimaryKey val id: Int = 1,
    val player1Name: String,
    val player2Name: String,
    val currentPlayer: PlayerColor,
    val selectedCategoryName: String,
    val selectedCategoryId: Int,
    val isVsAi: Boolean = true,
    val boardData: String
)

@Serializable
enum class OnlineRoomStatus {
    WAITING,
    ACTIVE,
    FINISHED,
    CLOSED
}

@Serializable
data class OnlineGameState(
    val roomCode: String = "",
    val boardData: String = "",
    val currentPlayer: PlayerColor = PlayerColor.RED,
    val winner: PlayerColor? = null,
    val tie: Boolean = false,

    // Shared pending move only. The question itself should be local to the player answering.
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