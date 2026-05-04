package edu.moravian.csci215.finalproject395_truthfulcheckers.di

import edu.moravian.csci215.finalproject395_truthfulcheckers.audio.AudioPlayer
import edu.moravian.csci215.finalproject395_truthfulcheckers.audio.SoundManager
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.GameRepository
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.OnlineGameRepository
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.TriviaDao
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.StatsDao
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.GameSessionDao
import edu.moravian.csci215.finalproject395_truthfulcheckers.viewmodel.GameViewModel
import edu.moravian.csci215.finalproject395_truthfulcheckers.viewmodel.OnlineGameViewModel
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

fun commonModule() = module {
    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }
    
    single {
        val json: Json = get()
        HttpClient {
            install(ContentNegotiation) {
                json(json)
            }
        }
    }
    
    single { AudioPlayer() }
    
    single { 
        val player: AudioPlayer = get()
        SoundManager(player) 
    }
    
    single { 
        val tDao: TriviaDao = get()
        val sDao: StatsDao = get()
        val gDao: GameSessionDao = get()
        val client: HttpClient = get()
        GameRepository(tDao, sDao, gDao, client)
    }
    
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

expect fun platformModule(): Module
