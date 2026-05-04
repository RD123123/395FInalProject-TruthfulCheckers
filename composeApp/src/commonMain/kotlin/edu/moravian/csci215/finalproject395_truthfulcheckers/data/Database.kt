package edu.moravian.csci215.finalproject395_truthfulcheckers.data

import androidx.room.*
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.GameSession
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.GameStats
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.TriviaQuestion
import kotlinx.coroutines.flow.Flow

@Dao
interface TriviaDao {
    @Query("SELECT * FROM trivia_questions")
    fun getAllQuestions(): Flow<List<TriviaQuestion>>

    @Query("SELECT COUNT(*) FROM trivia_questions WHERE categoryId = :categoryId AND isUsed = 0")
    suspend fun getUnusedCountByCategory(categoryId: Int): Int

    @Query("SELECT * FROM trivia_questions WHERE categoryId = :categoryId AND isUsed = 0")
    suspend fun getUnusedQuestionsByCategory(categoryId: Int): List<TriviaQuestion>

    @Query("UPDATE trivia_questions SET isUsed = 1 WHERE id = :id")
    suspend fun markAsUsed(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<TriviaQuestion>)

    @Query("DELETE FROM trivia_questions")
    suspend fun deleteAll()
}

@Dao
interface StatsDao {
    @Query("SELECT * FROM game_stats ORDER BY date DESC")
    fun getAllStats(): Flow<List<GameStats>>

    @Insert
    suspend fun insertStat(stat: GameStats)
}

@Dao
interface GameSessionDao {
    @Query("SELECT * FROM game_sessions WHERE id = 1")
    suspend fun getActiveSession(): GameSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: GameSession)

    @Query("DELETE FROM game_sessions")
    suspend fun clearSession()
}

@Database(
    entities = [TriviaQuestion::class, GameStats::class, GameSession::class],
    version = 9 // Bumped version to ensure schema reset
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun triviaDao(): TriviaDao
    abstract fun statsDao(): StatsDao
    abstract fun gameSessionDao(): GameSessionDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>
