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
    val color: PlayerColor,
    val isKing: Boolean = false,
    val emotion: Emotion = Emotion.HAPPY
)

@Serializable
data class Position(val row: Int, val col: Int)

@Serializable
@Entity(tableName = "trivia_questions")
data class TriviaQuestion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String,
    val correctAnswer: Boolean,
    val category: String = "General",
    val categoryId: Int = 0,
    val difficulty: String = "Medium",
    val isUsed: Boolean = false
)

@Serializable
data class TriviaCategory(
    val id: Int,
    val name: String
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
