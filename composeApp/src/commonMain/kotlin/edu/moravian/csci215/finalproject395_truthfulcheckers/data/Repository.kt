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

@Serializable
data class OpenTriviaResponse(
    val response_code: Int,
    val results: List<TriviaResult>
)

@Serializable
data class TriviaResult(
    val category: String,
    val type: String,
    val difficulty: String,
    val question: String,
    val correct_answer: String,
    val incorrect_answers: List<String>
)

@Serializable
data class CategoryResponse(
    val trivia_categories: List<TriviaCategory>
)

class GameRepository(
    private val triviaDao: TriviaDao,
    private val statsDao: StatsDao,
    private val sessionDao: GameSessionDao,
    private val httpClient: HttpClient
) {
    val allStats: Flow<List<GameStats>> = statsDao.getAllStats()

    suspend fun getCategories(): List<TriviaCategory> {
        return try {
            val response: CategoryResponse = httpClient.get("https://opentdb.com/api_category.php").body()
            response.trivia_categories
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getQuestion(categoryId: Int): TriviaQuestion? {
        // 1. Check database for unused questions in this category
        val unusedCount = triviaDao.getUnusedCountByCategory(categoryId)
        
        if (unusedCount < 3) {
            // 2. Fetch more if low on cache
            fetchQuestionsFromServer(categoryId)
        }

        // 3. Return one from DB
        val unused = triviaDao.getUnusedQuestionsByCategory(categoryId)
        return unused.firstOrNull()
    }

    suspend fun markQuestionAsUsed(id: Int) {
        triviaDao.markAsUsed(id)
    }

    private suspend fun fetchQuestionsFromServer(categoryId: Int) {
        try {
            val url = "https://opentdb.com/api.php?amount=10&category=$categoryId&type=boolean"
            val response: OpenTriviaResponse = httpClient.get(url).body()
            
            if (response.response_code == 0) {
                val questions = response.results.map {
                    TriviaQuestion(
                        question = decodeHtml(it.question),
                        correctAnswer = it.correct_answer.lowercase() == "true",
                        category = it.category,
                        categoryId = categoryId,
                        difficulty = it.difficulty,
                        isUsed = false
                    )
                }
                triviaDao.insertQuestions(questions)
            }
        } catch (e: Exception) {
            // Rely on existing cache if available
        }
    }

    suspend fun saveGameStats(stats: GameStats) {
        statsDao.insertStat(stats)
    }

    suspend fun saveSession(session: GameSession) {
        sessionDao.saveSession(session)
    }

    suspend fun getActiveSession(): GameSession? {
        return sessionDao.getActiveSession()
    }

    suspend fun clearSession() {
        sessionDao.clearSession()
    }

    private fun decodeHtml(text: String): String {
        return text
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
}
