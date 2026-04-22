package edu.moravian.csci215.finalproject395_truthfulcheckers.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.AppDatabase
import org.koin.dsl.module

actual fun platformModule() = module {
    single {
        val dbFile = get<Context>().getDatabasePath("truthful_checkers.db")
        Room.databaseBuilder<AppDatabase>(
            context = get<Context>(),
            name = dbFile.absolutePath
        ).setDriver(AndroidSQLiteDriver())
            .build()
    }
    single { get<AppDatabase>().triviaDao() }
    single { get<AppDatabase>().statsDao() }
}
