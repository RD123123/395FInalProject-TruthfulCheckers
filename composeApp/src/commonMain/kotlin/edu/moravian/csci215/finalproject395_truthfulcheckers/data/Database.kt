package edu.moravian.csci215.finalproject395_truthfulcheckers.data

import androidx.room.*
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.GameSession
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.GameStats
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.PlayerColor
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.TriviaQuestion
import kotlinx.coroutines.flow.Flow

@Dao
interface TriviaDao {
    @Query("SELECT * FROM trivia_questions")
    fun getAllQuestions(): Flow<List<TriviaQuestion>>

    @Query("SELECT * FROM trivia_questions WHERE categoryId = :catId AND isUsed = 0")
    suspend fun getUnusedQuestionsByCategory(catId: Int): List<TriviaQuestion>

    @Query("SELECT COUNT(*) FROM trivia_questions WHERE categoryId = :catId AND isUsed = 0")
    suspend fun getUnusedCountByCategory(catId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<TriviaQuestion>)

    @Query("UPDATE trivia_questions SET isUsed = 1 WHERE id = :questionId")
    suspend fun markAsUsed(questionId: Int)

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

class Converters {
    @TypeConverter
    fun fromPlayerColor(value: PlayerColor): String = value.name

    @TypeConverter
    fun toPlayerColor(value: String): PlayerColor = PlayerColor.valueOf(value)
}

@Database(entities = [TriviaQuestion::class, GameStats::class, GameSession::class], version = 2)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun triviaDao(): TriviaDao
    abstract fun statsDao(): StatsDao
    abstract fun gameSessionDao(): GameSessionDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>
