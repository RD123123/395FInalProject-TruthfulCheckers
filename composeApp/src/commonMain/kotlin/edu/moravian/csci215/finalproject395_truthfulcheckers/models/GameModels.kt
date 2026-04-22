package edu.moravian.csci215.finalproject395_truthfulcheckers.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

enum class PlayerColor {
    RED, BLUE
}

enum class Emotion {
    HAPPY, SCARED, MAD, CONFUSED
}

data class Piece(
    val color: PlayerColor,
    val isKing: Boolean = false,
    val emotion: Emotion = Emotion.HAPPY
)

data class Position(val row: Int, val col: Int)

@Serializable
@Entity(tableName = "trivia_questions")
data class TriviaQuestion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String,
    val correctAnswer: Boolean,
    val category: String = "General",
    val difficulty: String = "Medium"
)

@Entity(tableName = "game_stats")
data class GameStats(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val winnerName: String,
    val totalMoves: Int,
    val accuracy: Double,
    val date: Long = 0L
)
