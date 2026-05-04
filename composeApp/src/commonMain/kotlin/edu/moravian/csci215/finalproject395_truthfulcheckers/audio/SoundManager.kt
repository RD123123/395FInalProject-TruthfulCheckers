package edu.moravian.csci215.finalproject395_truthfulcheckers.audio

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

expect class AudioPlayer() {
    fun playMusic(fileName: String, loop: Boolean = true)
    fun stopMusic()
    fun playSound(fileName: String)
}

class SoundManager(private val player: AudioPlayer) {
    var isMusicEnabled by mutableStateOf(true)
    var isSoundEnabled by mutableStateOf(true)

    fun startBackgroundMusic() {
        if (isMusicEnabled) {
            player.playMusic("background_music.wav")
        }
    }

    fun stopBackgroundMusic() {
        player.stopMusic()
    }

    fun playMoveSound() {
        if (isSoundEnabled) {
            player.playSound("click.wav")
        }
    }

    fun playCaptureSound() {
        if (isSoundEnabled) {
            player.playSound("clack.wav")
        }
    }

    fun playWrongAnswerSound() {
        if (isSoundEnabled) {
            // Using clack as a fallback if a specific 'wrong' sound isn't provided
            player.playSound("clack.wav")
        }
    }

    fun toggleMusic(enabled: Boolean) {
        isMusicEnabled = enabled
        if (!enabled) stopBackgroundMusic()
        else startBackgroundMusic()
    }

    fun toggleSound(enabled: Boolean) {
        isSoundEnabled = enabled
    }
}
