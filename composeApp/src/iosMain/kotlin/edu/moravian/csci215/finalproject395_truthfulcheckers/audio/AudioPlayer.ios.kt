package edu.moravian.csci215.finalproject395_truthfulcheckers.audio

import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioPlayerDelegateProtocol
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVAudioSessionCategoryOptionDefaultToSpeaker
import platform.AVFAudio.AVAudioSessionCategoryOptionMixWithOthers
import platform.AVFAudio.setActive
import platform.Foundation.NSBundle
import platform.Foundation.NSURL
import platform.darwin.NSObject
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual class AudioPlayer {
    private var musicPlayer: AVAudioPlayer? = null
    private val activeSounds = mutableSetOf<AVAudioPlayer>()

    private val soundDelegate = object : NSObject(), AVAudioPlayerDelegateProtocol {
        override fun audioPlayerDidFinishPlaying(player: AVAudioPlayer, successfully: Boolean) {
            activeSounds.remove(player)
        }
    }

    init {
        configureAudioSession()
    }

    private fun configureAudioSession() {
        try {
            val session = AVAudioSession.sharedInstance()
            // Playback category allows audio even if the physical mute switch is ON.
            // Explicitly using positional arguments to avoid parameter name mismatch in different Kotlin versions.
            val options = AVAudioSessionCategoryOptionMixWithOthers or AVAudioSessionCategoryOptionDefaultToSpeaker
            session.setCategory(AVAudioSessionCategoryPlayback, options, null)
            session.setActive(true, null)
        } catch (e: Exception) {
            println("Failed to set up iOS audio session: ${e.message}")
        }
    }

    private fun getResourceUrl(fileName: String): NSURL? {
        val name = fileName.substringBeforeLast(".")
        val type = fileName.substringAfterLast(".")
        
        // Search in standard bundle paths for Compose Multiplatform
        return NSBundle.mainBundle.URLForResource(name, type)
            ?: NSBundle.mainBundle.URLForResource("compose-resources/files/$name", type)
            ?: NSBundle.mainBundle.URLForResource("files/$name", type)
    }

    actual fun playMusic(fileName: String, loop: Boolean) {
        stopMusic()
        val url = getResourceUrl(fileName) ?: return

        try {
            musicPlayer = AVAudioPlayer(contentsOfURL = url, error = null)
            musicPlayer?.numberOfLoops = if (loop) -1L else 0L
            musicPlayer?.prepareToPlay()
            musicPlayer?.play()
        } catch (e: Exception) {
            println("Error playing music ($fileName): ${e.message}")
        }
    }

    actual fun stopMusic() {
        musicPlayer?.stop()
        musicPlayer = null
    }

    actual fun playSound(fileName: String) {
        val url = getResourceUrl(fileName) ?: return

        try {
            val soundPlayer = AVAudioPlayer(contentsOfURL = url, error = null)
            soundPlayer.delegate = soundDelegate
            activeSounds.add(soundPlayer)
            soundPlayer.prepareToPlay()
            soundPlayer.play()
        } catch (e: Exception) {
            println("Error playing sound ($fileName): ${e.message}")
        }
    }
}
