package edu.moravian.csci215.finalproject395_truthfulcheckers.di

import edu.moravian.csci215.finalproject395_truthfulcheckers.audio.AudioPlayer
import edu.moravian.csci215.finalproject395_truthfulcheckers.audio.SoundManager
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.GameRepository
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.GameSessionDao
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.OnlineGameRepository
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.StatsDao
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.TriviaDao
import edu.moravian.csci215.finalproject395_truthfulcheckers.viewmodel.GameViewModel
import edu.moravian.csci215.finalproject395_truthfulcheckers.viewmodel.OnlineGameViewModel
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * The core Dependency Injection (DI) module for the Truthful Checkers application.
 * Defines the shared singletons and factories used across both iOS and Android platforms.
 * * Manages the instantiation of the Ktor HTTP client, audio engine, repositories,
 * and ViewModels to ensure decoupled and testable architecture.
 */
fun commonModule() =
    module {
        // Configures the JSON serializer to ignore unexpected API fields, preventing crashes.
        single {
            Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
        }

        // Provides a singleton Ktor HttpClient configured with the JSON serializer.
        single {
            val json: Json = get()
            HttpClient {
                install(ContentNegotiation) {
                    json(json)
                }
            }
        }

        // Core audio components
        single { AudioPlayer() }

        single {
            val player: AudioPlayer = get()
            SoundManager(player)
        }

        // Central data repository combining local DB DAOs and the remote Ktor client.
        single {
            val tDao: TriviaDao = get()
            val sDao: StatsDao = get()
            val gDao: GameSessionDao = get()
            val client: HttpClient = get()
            GameRepository(tDao, sDao, gDao, client)
        }

        // ViewModels
        single {
            val repo: GameRepository = get()
            val sm: SoundManager = get()
            GameViewModel(repo, sm)
        }

        single {
            val onlineRepo: OnlineGameRepository = get()
            val gameRepo: GameRepository = get()
            val sm: SoundManager = get()
            OnlineGameViewModel(onlineRepo, gameRepo, sm)
        }
    }

/**
 * Expected platform-specific DI module.
 * The actual implementations (in androidMain and iosMain) will provide
 * the platform-specific dependencies like the Room Database builder and Firebase instances.
 */
expect fun platformModule(): Module
