package edu.moravian.csci215.finalproject395_truthfulcheckers.data

import edu.moravian.csci215.finalproject395_truthfulcheckers.models.GameStats
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.TriviaQuestion
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
data class OpenTriviaResponse(
    val results: List<TriviaResult>
)

@Serializable
data class TriviaResult(
    val question: String,
    val correct_answer: String,
    val category: String,
    val difficulty: String
)

class GameRepository(
    private val triviaDao: TriviaDao,
    private val statsDao: StatsDao,
    private val httpClient: HttpClient
) {
    val allStats: Flow<List<GameStats>> = statsDao.getAllStats()
    val cachedQuestions: Flow<List<TriviaQuestion>> = triviaDao.getAllQuestions()

    suspend fun fetchQuestionsFromServer() {
        try {
            val response: OpenTriviaResponse = httpClient.get("https://opentdb.com/api.php?amount=10&type=boolean").body()
            val questions = response.results.map {
                TriviaQuestion(
                    question = it.question, // Note: In a real app, decode HTML entities
                    correctAnswer = it.correct_answer.lowercase() == "true",
                    category = it.category,
                    difficulty = it.difficulty
                )
            }
            triviaDao.insertQuestions(questions)
        } catch (e: Exception) {
            // Handle error or rely on cache
        }
    }

    suspend fun saveGameStats(stats: GameStats) {
        statsDao.insertStat(stats)
    }
}
