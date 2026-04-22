package edu.moravian.csci215.finalproject395_truthfulcheckers.di

import edu.moravian.csci215.finalproject395_truthfulcheckers.data.GameRepository
import edu.moravian.csci215.finalproject395_truthfulcheckers.viewmodel.GameViewModel
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

fun commonModule() = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                })
            }
        }
    }
    
    // Note: Database and DAOs should be provided per-platform or via a factory
    // For this skeleton, we'll assume they are provided in the platform-specific modules
    
    single { GameRepository(get(), get(), get()) }
    factory { GameViewModel(get()) }
}

expect fun platformModule(): Module
