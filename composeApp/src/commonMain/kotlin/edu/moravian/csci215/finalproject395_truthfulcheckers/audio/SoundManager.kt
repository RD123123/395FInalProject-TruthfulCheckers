package edu.moravian.csci215.finalproject395_truthfulcheckers.audio

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Expected platform-specific audio engine.
 * The `actual` implementations must be provided in the `androidMain`
 * and `iosMain` source sets using Android's MediaPlayer and iOS's AVAudioPlayer.
 */
expect class AudioPlayer() {
    /** Plays a looping background track or a single sound file. */
    fun playMusic(
        fileName: String,
        loop: Boolean = true,
    )

    /** Halts the currently playing background music. */
    fun stopMusic()

    /** Plays a short sound effect once. */
    fun playSound(fileName: String)
}

/**
 * A shared state manager for application audio.
 * Controls the playback of background music and sound effects based on user preferences.
 * Designed to be injected as a singleton via Koin.
 *
 * @property player The platform-specific audio engine injected at runtime.
 */
class SoundManager(
    private val player: AudioPlayer,
) {
    /** Global toggle for background music. UI observes this state. */
    var isMusicEnabled by mutableStateOf(true)

    /** Global toggle for short sound effects. UI observes this state. */
    var isSoundEnabled by mutableStateOf(true)

    /** Starts the main background track if the user has music enabled. */
    fun startBackgroundMusic() {
        if (isMusicEnabled) {
            player.playMusic("background_music.wav")
        }
    }

    /** Forcefully stops the background track regardless of settings. */
    fun stopBackgroundMusic() {
        player.stopMusic()
    }

    /** Plays a click sound for standard board interactions. */
    fun playMoveSound() {
        if (isSoundEnabled) {
            player.playSound("click.wav")
        }
    }

    /** Plays a heavier clack sound when a piece is captured. */
    fun playCaptureSound() {
        if (isSoundEnabled) {
            player.playSound("clack.wav")
        }
    }

    /** * Toggles the background music state and immediately applies the change.
     * @param enabled True to turn music on, false to mute.
     */
    fun toggleMusic(enabled: Boolean) {
        isMusicEnabled = enabled
        if (!enabled) {
            stopBackgroundMusic()
        } else {
            startBackgroundMusic()
        }
    }

    /**
     * Toggles the sound effects state. Takes effect on the next triggered sound.
     * @param enabled True to allow sound effects, false to mute.
     */
    fun toggleSound(enabled: Boolean) {
        isSoundEnabled = enabled
    }
}
