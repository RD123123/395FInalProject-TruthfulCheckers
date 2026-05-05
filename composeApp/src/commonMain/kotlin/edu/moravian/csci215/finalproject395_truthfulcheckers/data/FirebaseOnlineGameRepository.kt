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

/**
 * A Kotlin Multiplatform implementation of the [OnlineGameRepository] using Firebase Realtime Database.
 * Utilizes the GitLive Firebase SDK to provide real-time synchronization between Android and iOS clients.
 */
class FirebaseOnlineGameRepository : OnlineGameRepository {
    // Connects to the specific Firebase instance and targets the "rooms" node
    private val database =
        Firebase
            .database("https://truthfulcheckers-default-rtdb.firebaseio.com")
            .reference("rooms")

    /**
     * Subscribes to real-time updates for a specific room.
     * Emits a new [OnlineGameState] every time the opponent makes a move or updates the lobby.
     *
     * @param roomCode The 6-digit identifier for the lobby.
     * @return A Flow emitting the current state, or null if the room is deleted/missing.
     */
    override fun getGameState(roomCode: String): Flow<OnlineGameState?> =
        database
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

    /**
     * Initializes a new multiplayer lobby as the "Red" host player.
     * Fails if the generated room code already exists on the server.
     */
    override suspend fun createRoom(
        roomCode: String,
        playerName: String,
        board: List<List<Piece?>>,
        category: TriviaCategory?,
        difficulty: String,
    ): Result<Unit> =
        runCatching {
            val roomRef = database.child(roomCode)
            val existingSnapshot = roomRef.valueEvents.first()

            if (existingSnapshot.exists) {
                throw Exception("Room code already exists. Try again.")
            }

            val initialState =
                OnlineGameState(
                    roomCode = roomCode,
                    boardData = Json.encodeToString(board),
                    redPlayerName = playerName.ifBlank { "Player 1" },
                    bluePlayerName = "",
                    blueJoined = false,
                    selectedCategoryId = category?.id ?: 9,
                    selectedCategoryName = category?.name ?: "General Knowledge",
                    difficulty = difficulty,
                    status = OnlineRoomStatus.WAITING,
                    lastUpdate = currentTimeMillis(),
                )

            roomRef.setValue(initialState)
        }

    /**
     * Connects to an existing lobby as the "Blue" guest player.
     * Fails if the room does not exist, is already full, or has been closed.
     */
    override suspend fun joinRoom(
        roomCode: String,
        playerName: String,
    ): Result<Unit> =
        runCatching {
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
                    "lastUpdate" to currentTimeMillis(),
                ),
            )
        }

    /**
     * Pushes the latest local board modifications to the cloud so the opponent can see them.
     */
    override suspend fun updateGameState(
        roomCode: String,
        state: OnlineGameState,
    ): Result<Unit> =
        runCatching {
            database
                .child(roomCode)
                .setValue(state.copy(lastUpdate = currentTimeMillis()))
        }

    /**
     * Marks a room as closed, signaling to all connected clients that the host has left.
     */
    override suspend fun closeRoom(roomCode: String): Result<Unit> =
        runCatching {
            database
                .child(roomCode)
                .child("status")
                .setValue(OnlineRoomStatus.CLOSED.name)
        }

    /**
     * Hard-deletes the room from Firebase to prevent database bloat.
     */
    override suspend fun deleteRoom(roomCode: String): Result<Unit> =
        runCatching {
            database
                .child(roomCode)
                .removeValue()
        }

    /**
     * Wrapper for closing a room when a user explicitly taps a "Leave" button.
     */
    override suspend fun leaveRoom(roomCode: String) {
        closeRoom(roomCode)
    }

    /**
     * Helper to get a unified timestamp across iOS and Android.
     */
    private fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
}
