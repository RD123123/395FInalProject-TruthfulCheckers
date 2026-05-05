package edu.moravian.csci215.finalproject395_truthfulcheckers.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.AppDatabase
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.AppDatabaseConstructor
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.FirebaseOnlineGameRepository
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.OnlineGameRepository
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSHomeDirectory

/**
 * The iOS-specific implementation of the Koin Dependency Injection module.
 * Fulfills the `expect` declaration in the shared code by providing Apple-native
 * implementations for the Room Database and Firebase.
 */
actual fun platformModule(): Module =
    module {
        /**
         * Builds the local Room database using the Bundled SQLite driver for iOS.
         * Complies with Apple's strict sandboxing rules by explicitly routing the database
         * file into the application's isolated Documents directory.
         */
        single {
            val dbFile = NSHomeDirectory() + "/Documents/truthful_checkers.db"

            Room
                .databaseBuilder<AppDatabase>(
                    name = dbFile,
                    factory = { AppDatabaseConstructor.initialize() },
                ).setDriver(BundledSQLiteDriver())
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
