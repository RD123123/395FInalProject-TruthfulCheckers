package edu.moravian.csci215.finalproject395_truthfulcheckers.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.AppDatabase
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.AppDatabaseConstructor
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.FirebaseOnlineGameRepository
import edu.moravian.csci215.finalproject395_truthfulcheckers.data.OnlineGameRepository
import org.koin.dsl.module

actual fun platformModule() = module {
    single {
        val context: Context = get()
        val dbFile = context.getDatabasePath("truthful_checkers.db")
        Room.databaseBuilder<AppDatabase>(
            context = context,
            name = dbFile.absolutePath,
            factory = { AppDatabaseConstructor.initialize() }
        ).setDriver(AndroidSQLiteDriver())
            .fallbackToDestructiveMigration(true)
            .build()
    }
    
    single { get<AppDatabase>().triviaDao() }
    single { get<AppDatabase>().statsDao() }
    single { get<AppDatabase>().gameSessionDao() }

    single<OnlineGameRepository> { FirebaseOnlineGameRepository() }
}
