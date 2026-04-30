package edu.moravian.csci215.finalproject395_truthfulcheckers.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.AppDatabase
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.AppDatabaseConstructor
import org.koin.dsl.module
import platform.Foundation.NSHomeDirectory

actual fun platformModule() = module {
    single {
        val dbFile = NSHomeDirectory() + "/truthful_checkers.db"
        Room.databaseBuilder<AppDatabase>(
            name = dbFile,
            factory = { AppDatabaseConstructor.initialize() }
        ).setDriver(BundledSQLiteDriver())
            .build()
    }
    single { get<AppDatabase>().triviaDao() }
    single { get<AppDatabase>().statsDao() }
}
