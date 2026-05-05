package edu.moravian.csci215.finalproject395_truthfulcheckers.data

import edu.moravian.csci215.finalproject395_truthfulcheckers.models.GameSession
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.GameStats
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.TriviaCategory
import edu.moravian.csci215.finalproject395_truthfulcheckers.models.TriviaQuestion
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

/**
 * Data models for the Open Trivia Database (OpenTDB) API responses.
 */
@Serializable
data class OpenTriviaResponse(
    val response_code: Int,
    val results: List<TriviaResult>,
)

@Serializable
data class TriviaResult(
    val category: String,
    val type: String,
    val difficulty: String,
    val question: String,
    val correct_answer: String,
    val incorrect_answers: List<String>,
)

@Serializable
data class CategoryResponse(
    val trivia_categories: List<TriviaCategory>,
)

/**
 * The central repository managing data flow between the Ktor network client,
 * the Room local database, and the UI.
 * * This class implements a "Cache-First" strategy for trivia questions.
 */
class GameRepository(
    private val triviaDao: TriviaDao,
    private val statsDao: StatsDao,
    private val sessionDao: GameSessionDao,
    private val httpClient: HttpClient,
) {
    /** Observable stream of all game history stats. */
    val allStats: Flow<List<GameStats>> = statsDao.getAllStats()

    /**
     * Fetches available trivia categories from the network.
     * @return A list of [TriviaCategory] or an empty list if the request fails.
     */
    suspend fun getCategories(): List<TriviaCategory> {
        return try {
            val response: CategoryResponse = httpClient.get("https://opentdb.com/api_category.php").body()
            response.trivia_categories
        } catch (e: Exception) {
            // ADD THESE TWO LINES:
            println("NETWORK CRASH CAUGHT: ${e.message}")
            e.printStackTrace()

            emptyList()
        }
    }

    /**
     * Retrieves a trivia question for the given category.
     * Checks the local database first; if the cache is low, it fetches more from the API.
     */
    suspend fun getQuestion(categoryId: Int): TriviaQuestion? {
        val unusedCount = triviaDao.getUnusedCountByCategory(categoryId)

        // Fetch more questions from server if cache is running low
        if (unusedCount < 3) {
            fetchQuestionsFromServer(categoryId)
        }

        val unused = triviaDao.getUnusedQuestionsByCategory(categoryId)
        return unused.firstOrNull()
    }

    /** Marks a specific question as used in the database so it isn't repeated. */
    suspend fun markQuestionAsUsed(id: Int) {
        triviaDao.markAsUsed(id)
    }

    /**
     * Internal helper to fetch 10 boolean questions from the OpenTDB API
     * and persist them into the local Room database.
     */
    private suspend fun fetchQuestionsFromServer(categoryId: Int) {
        try {
            val url = "https://opentdb.com/api.php?amount=10&category=$categoryId&type=boolean"
            val response: OpenTriviaResponse = httpClient.get(url).body()

            if (response.response_code == 0) {
                val questions =
                    response.results.map {
                        TriviaQuestion(
                            question = decodeHtml(it.question),
                            correctAnswer = it.correct_answer.lowercase() == "true",
                            category = it.category,
                            categoryId = categoryId,
                            difficulty = it.difficulty,
                            isUsed = false,
                        )
                    }
                triviaDao.insertQuestions(questions)
            }
        } catch (e: Exception) {
            // Silently fail and rely on database cache
        }
    }

    suspend fun saveGameStats(stats: GameStats) {
        statsDao.insertStat(stats)
    }

    suspend fun saveSession(session: GameSession) {
        sessionDao.saveSession(session)
    }

    suspend fun getActiveSession(): GameSession? = sessionDao.getActiveSession()

    suspend fun clearSession() {
        sessionDao.clearSession()
    }

    /**
     * Manual HTML entity decoder to clean up strings from the OpenTDB API.
     * This is necessary as Kotlin Multiplatform lacks a unified native HTML decoder.
     */
    private fun decodeHtml(text: String): String =
        text
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&rsquo;", "'")
            .replace("&lsquo;", "'")
            .replace("&ldquo;", "\"")
            .replace("&rdquo;", "\"")
            .replace("&hellip;", "...")
            .replace("&ndash;", "-")
            .replace("&mdash;", "—")
}
