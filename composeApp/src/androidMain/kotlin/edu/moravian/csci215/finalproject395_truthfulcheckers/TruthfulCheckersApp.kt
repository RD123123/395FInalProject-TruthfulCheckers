package edu.moravian.csci215.finalproject395_truthfulcheckers

import android.app.Application
import edu.moravian.csci215.finalproject395_truthfulcheckers.di.commonModule
import edu.moravian.csci215.finalproject395_truthfulcheckers.di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TruthfulCheckersApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TruthfulCheckersApp)
            modules(commonModule(), platformModule())
        }
    }
}
