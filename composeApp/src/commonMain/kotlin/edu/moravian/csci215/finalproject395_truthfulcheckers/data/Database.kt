package edu.moravian.csci215.finalproject395_truthfulcheckers.data

import androidx.room.*
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.GameStats
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.TriviaQuestion
import kotlinx.coroutines.flow.Flow

@Dao
interface TriviaDao {
    @Query("SELECT * FROM trivia_questions")
    fun getAllQuestions(): Flow<List<TriviaQuestion>>

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

@Database(entities = [TriviaQuestion::class, GameStats::class], version = 1)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun triviaDao(): TriviaDao
    abstract fun statsDao(): StatsDao
}

// Room 2.7.0-rc02 requires an expect object for the database constructor on multiplatform targets.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>
