package edu.moravian.csci215.finalproject395_truthfulcheckers.audio

// ---> THESE TWO IMPORTS ARE THE ONLY FIX <---
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioPlayerDelegateProtocol

import platform.Foundation.NSBundle
import platform.Foundation.NSURL
import platform.darwin.NSObject
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual class AudioPlayer {
    private var musicPlayer: AVAudioPlayer? = null

    actual fun playMusic(fileName: String, loop: Boolean) {
        stopMusic()
        val resourceName = fileName.substringBeforeLast(".")
        val resourceType = fileName.substringAfterLast(".")

        val url = NSBundle.mainBundle.URLForResource(resourceName, resourceType) ?: return

        try {
            musicPlayer = AVAudioPlayer(contentsOfURL = url, error = null)
            musicPlayer?.numberOfLoops = if (loop) -1L else 0L // Keep these as Longs for iOS
            musicPlayer?.prepareToPlay()
            musicPlayer?.play()
        } catch (e: Exception) {
            println("Error playing music: ${e.message}")
        }
    }

    actual fun stopMusic() {
        musicPlayer?.stop()
        musicPlayer = null
    }

    actual fun playSound(fileName: String) {
        val resourceName = fileName.substringBeforeLast(".")
        val resourceType = fileName.substringAfterLast(".")

        val url = NSBundle.mainBundle.URLForResource(resourceName, resourceType) ?: return

        try {
            val soundPlayer = AVAudioPlayer(contentsOfURL = url, error = null)
            soundPlayer.prepareToPlay()
            soundPlayer.play()
        } catch (e: Exception) {
            println("Error playing sound: ${e.message}")
        }
    }
}