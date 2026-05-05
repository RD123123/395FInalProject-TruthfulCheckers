package edu.moravian.csci215.finalproject395_truthfulcheckers

import androidx.compose.ui.window.ComposeUIViewController
import edu.moravian.csci215.finalproject395_truthfulcheckers.di.commonModule
import edu.moravian.csci215.finalproject395_truthfulcheckers.di.platformModule
import org.koin.core.context.startKoin
import platform.UIKit.UIViewController // <-- This explicitly tells Kotlin how to talk to iOS

private var isKoinStarted = false

/**
 * The main entry point for the iOS application.
 * This function is called directly from the Swift AppDelegate to launch the Compose UI.
 */
@Suppress("FunctionName", "unused") // Silences Kotlin's linting rules so iOS can read this properly
fun MainViewController(): UIViewController = ComposeUIViewController {

    // Safely boot up Koin for Dependency Injection
    if (!isKoinStarted) {
        startKoin {
            modules(commonModule(), platformModule())
        }
        isKoinStarted = true
    }

    // Launch the shared Compose UI
    App()
}