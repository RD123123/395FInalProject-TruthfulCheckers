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

actual fun platformModule(): Module = module {
    single {
        val dbFile = NSHomeDirectory() + "/Documents/truthful_checkers.db"

        Room.databaseBuilder<AppDatabase>(
            name = dbFile,
            factory = { AppDatabaseConstructor.initialize() }
        )
            .setDriver(BundledSQLiteDriver())
            .fallbackToDestructiveMigration(true)
            .build()
    }

    single { get<AppDatabase>().triviaDao() }
    single { get<AppDatabase>().statsDao() }
    single { get<AppDatabase>().gameSessionDao() }

    single<OnlineGameRepository> { FirebaseOnlineGameRepository() }
}