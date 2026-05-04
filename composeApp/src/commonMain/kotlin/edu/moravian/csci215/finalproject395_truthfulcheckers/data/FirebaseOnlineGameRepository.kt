package edu.moravian.csci215.finalproject395_truthfulcheckers.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.database.database
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.OnlineGameState
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.OnlineRoomStatus
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.Piece
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.TriviaCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FirebaseOnlineGameRepository : OnlineGameRepository {

    private val database = Firebase
        .database("https://truthfulcheckers-default-rtdb.firebaseio.com")
        .reference("rooms")

    override fun getGameState(roomCode: String): Flow<OnlineGameState?> {
        return database
            .child(roomCode)
            .valueEvents
            .map { snapshot ->
                try {
                    if (!snapshot.exists) {
                        null
                    } else {
                        snapshot.value<OnlineGameState>()
                    }
                } catch (e: Exception) {
                    println("Firebase KMP deserialization error: ${e.message}")
                    null
                }
            }
    }

    override suspend fun createRoom(
        roomCode: String,
        playerName: String,
        board: List<List<Piece?>>,
        category: TriviaCategory?,
        difficulty: String
    ): Result<Unit> = runCatching {
        val roomRef = database.child(roomCode)
        val existingSnapshot = roomRef.valueEvents.first()

        if (existingSnapshot.exists) {
            throw Exception("Room code already exists. Try again.")
        }

        val initialState = OnlineGameState(
            roomCode = roomCode,
            boardData = Json.encodeToString(board),
            redPlayerName = playerName.ifBlank { "Player 1" },
            bluePlayerName = "",
            blueJoined = false,
            selectedCategoryId = category?.id ?: 9,
            selectedCategoryName = category?.name ?: "General Knowledge",
            difficulty = difficulty,
            status = OnlineRoomStatus.WAITING,
            lastUpdate = currentTimeMillis()
        )

        roomRef.setValue(initialState)
    }

    override suspend fun joinRoom(
        roomCode: String,
        playerName: String
    ): Result<Unit> = runCatching {
        val roomRef = database.child(roomCode)
        val snapshot = roomRef.valueEvents.first()

        if (!snapshot.exists) {
            throw Exception("Room not found")
        }

        val state = snapshot.value<OnlineGameState>()

        if (
            state.status == OnlineRoomStatus.CLOSED ||
            state.status == OnlineRoomStatus.FINISHED
        ) {
            throw Exception("Room is closed")
        }

        if (state.blueJoined) {
            throw Exception("Room is full")
        }

        roomRef.updateChildren(
            mapOf(
                "bluePlayerName" to playerName.ifBlank { "Player 2" },
                "blueJoined" to true,
                "status" to OnlineRoomStatus.ACTIVE.name,
                "lastUpdate" to currentTimeMillis()
            )
        )
    }

    override suspend fun updateGameState(
        roomCode: String,
        state: OnlineGameState
    ): Result<Unit> = runCatching {
        database
            .child(roomCode)
            .setValue(state.copy(lastUpdate = currentTimeMillis()))
    }

    override suspend fun closeRoom(roomCode: String): Result<Unit> = runCatching {
        database
            .child(roomCode)
            .child("status")
            .setValue(OnlineRoomStatus.CLOSED.name)
    }

    override suspend fun deleteRoom(roomCode: String): Result<Unit> = runCatching {
        database
            .child(roomCode)
            .removeValue()
    }

    override suspend fun leaveRoom(roomCode: String) {
        closeRoom(roomCode)
    }

    private fun currentTimeMillis(): Long {
        return Clock.System.now().toEpochMilliseconds()
    }
}