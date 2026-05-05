package edu.moravian.csci215.finalproject395_truthfulcheckers.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.AppDatabase
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.AppDatabaseConstructor
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.FirebaseOnlineGameRepository
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.OnlineGameRepository
import org.koin.dsl.module

/**
 * The Android-specific implementation of the Koin Dependency Injection module.
 * Fulfills the `expect` declaration in the shared code by providing Android-native
 * implementations for the Room Database and Firebase.
 */
actual fun platformModule() =
    module {
        /**
         * Builds the local Room database using the Android SQLite driver.
         * Uses destructive migration to prevent crashes if the database schema changes during development.
         */
        single {
            val context: Context = get()
            val dbFile = context.getDatabasePath("truthful_checkers.db")
            Room
                .databaseBuilder<AppDatabase>(
                    context = context,
                    name = dbFile.absolutePath,
                    factory = { AppDatabaseConstructor.initialize() },
                ).setDriver(AndroidSQLiteDriver())
                .fallbackToDestructiveMigration(true)
                .build()
        }

        // Provide individual DAOs from the constructed AppDatabase
        single { get<AppDatabase>().triviaDao() }
        single { get<AppDatabase>().statsDao() }
        single { get<AppDatabase>().gameSessionDao() }

        // Provide the Firebase implementation for online multiplayer
        single<OnlineGameRepository> { FirebaseOnlineGameRepository() }
    }
