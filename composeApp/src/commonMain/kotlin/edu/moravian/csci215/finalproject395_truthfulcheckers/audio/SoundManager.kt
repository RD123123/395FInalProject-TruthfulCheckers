package edu.moravian.csci215.finalproject395_truthfulcheckers.audio

expect class AudioPlayer() {
    fun playMusic(fileName: String, loop: Boolean = true)
    fun stopMusic()
    fun playSound(fileName: String)
}

object SoundManager {
    private val player = AudioPlayer()
    
    var isMusicEnabled: Boolean = true
        set(value) {
            field = value
            if (!value) stopBackgroundMusic()
        }
    
    var isSoundEnabled: Boolean = true

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
}
