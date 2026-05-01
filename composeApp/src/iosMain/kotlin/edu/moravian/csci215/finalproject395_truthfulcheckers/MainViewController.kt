package edu.moravian.csci215.finalproject395_truthfulcheckers

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.core.context.startKoin
import edu.moravian.csci215.finalproject395_truthfulcheckers.di.platformModule
import edu.moravian.csci215.finalproject395_truthfulcheckers.di.commonModule // <--- Import the common module

// A safety flag so Koin only starts once
private var isKoinStarted = false

fun MainViewController() = ComposeUIViewController {
    if (!isKoinStarted) {
        startKoin {
            // Load BOTH modules so the GameViewModel is available!
            modules(commonModule(), platformModule())
        }
        isKoinStarted = true
    }

    // Now that Koin is fully running, it is safe to load the UI
    App()
}