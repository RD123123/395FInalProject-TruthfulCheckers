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
            player.playMusic("background_music.mp3")
        }
    }

    fun stopBackgroundMusic() {
        player.stopMusic()
    }

    fun playMoveSound() {
        if (isSoundEnabled) {
            player.playSound("click.mp3")
        }
    }

    fun playCaptureSound() {
        if (isSoundEnabled) {
            player.playSound("clack.mp3")
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
