package edu.moravian.csci215.finalproject395_truthfulcheckers.data

import edu.moravian.csci215.finalproject395_truthfulcheckers.models.OnlineGameState
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.Piece
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.TriviaCategory
import kotlinx.coroutines.flow.Flow

interface OnlineGameRepository {
    fun getGameState(roomCode: String): Flow<OnlineGameState?>

    suspend fun createRoom(
        roomCode: String,
        playerName: String,
        board: List<List<Piece?>>,
        category: TriviaCategory?,
        difficulty: String,
    ): Result<Unit>

    suspend fun joinRoom(
        roomCode: String,
        playerName: String,
    ): Result<Unit>

    suspend fun updateGameState(
        roomCode: String,
        state: OnlineGameState,
    ): Result<Unit>

    suspend fun closeRoom(roomCode: String): Result<Unit>

    suspend fun deleteRoom(roomCode: String): Result<Unit>

    suspend fun leaveRoom(roomCode: String)
}
