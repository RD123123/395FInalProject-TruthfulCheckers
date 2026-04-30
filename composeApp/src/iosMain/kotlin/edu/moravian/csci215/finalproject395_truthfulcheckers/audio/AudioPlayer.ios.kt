package edu.moravian.csci215.finalproject395_truthfulcheckers.audio

import platform.AVFoundation.AVAudioPlayer
import platform.Foundation.NSBundle
import platform.Foundation.NSURL
import platform.AVFoundation.AVAudioPlayerDelegateProtocol
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
            musicPlayer?.numberOfLoops = if (loop) -1 else 0
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
