package edu.moravian.csci215.finalproject395_truthfulcheckers.audio

import kotlinx.cinterop.ExperimentalForeignApi
import org.jetbrains.compose.resources.ExperimentalResourceApi
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioPlayerDelegateProtocol
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionMixWithOthers
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSURL
import platform.darwin.NSObject
import truthfulcheckers.composeapp.generated.resources.Res

/**
 * The iOS-specific implementation of the cross-platform AudioPlayer.
 * * Utilizes Apple's native [AVAudioPlayer] API to handle playback via AVFoundation.
 * Manages audio sessions to ensure sounds play correctly even when the device
 * is subjected to system interruptions or silent switches.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalResourceApi::class)
actual class AudioPlayer {
    private var musicPlayer: AVAudioPlayer? = null

    // Tracks active sound effects to prevent premature garbage collection before the sound finishes
    private val activeSounds = mutableSetOf<AVAudioPlayer>()

    /**
     * Delegate object that listens for the completion of a sound effect.
     * Automatically removes the finished player from the active set to prevent memory leaks.
     */
    private val soundDelegate =
        object : NSObject(), AVAudioPlayerDelegateProtocol {
            override fun audioPlayerDidFinishPlaying(
                player: AVAudioPlayer,
                successfully: Boolean,
            ) {
                activeSounds.remove(player)
            }
        }

    init {
        configureAudioSession()
    }

    /**
     * Configures the global iOS audio session.
     * Sets the category to Playback so music plays even if the hardware silent switch is on,
     * and allows it to mix with other system audio.
     */
    private fun configureAudioSession() {
        try {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(
                AVAudioSessionCategoryPlayback,
                AVAudioSessionCategoryOptionMixWithOthers,
                null,
            )
            session.setActive(true, null)
        } catch (e: Exception) {
            println("iOS audio session configuration error: $e")
        }
    }

    /**
     * Resolves the internal Compose Resource URI into an Apple-compatible NSURL.
     * Hardcodes known asset paths as a safe fallback mechanism for KMP resource resolution.
     */
    private fun getResourceUrl(fileName: String): NSURL? {
        val uri =
            when (fileName) {
                "background_music.wav" -> {
                    Res.getUri("files/background_music.wav")
                }

                "click.wav" -> {
                    Res.getUri("files/click.wav")
                }

                "clack.wav" -> {
                    Res.getUri("files/clack.wav")
                }

                "background_music.mp3" -> {
                    Res.getUri("files/background_music.mp3")
                }

                "click.mp3" -> {
                    Res.getUri("files/click.mp3")
                }

                "clack.mp3" -> {
                    Res.getUri("files/clack.mp3")
                }

                else -> {
                    println("Unknown iOS audio file requested: $fileName")
                    return null
                }
            }

        println("iOS audio URI for $fileName: $uri")
        return NSURL.URLWithString(uri)
    }

    /**
     * Instantiates and starts a looping background music track.
     * Safely halts any existing track before allocating a new AVAudioPlayer.
     */
    actual fun playMusic(
        fileName: String,
        loop: Boolean,
    ) {
        val url = getResourceUrl(fileName)
        if (url == null) {
            println("Cannot play music because URL was null: $fileName")
            return
        }

        try {
            musicPlayer?.stop()
            musicPlayer = null

            val player = AVAudioPlayer(contentsOfURL = url, error = null)

            musicPlayer = player
            player.numberOfLoops = if (loop) -1 else 0
            player.volume = 1.0f
            player.prepareToPlay()

            val didPlay = player.play()
            println("iOS music play result for $fileName: $didPlay")
        } catch (e: Exception) {
            println("SAFE MUSIC ERROR: $e")
        }
    }

    /**
     * Halts and safely releases the background music player's memory resources.
     */
    actual fun stopMusic() {
        try {
            musicPlayer?.stop()
        } catch (e: Exception) {
            println("SAFE STOP MUSIC ERROR: $e")
        } finally {
            musicPlayer = null
        }
    }

    /**
     * Fires a one-shot sound effect.
     * Registers the player with the [soundDelegate] to ensure it is kept alive in memory
     * exactly as long as the sound lasts, preventing out-of-memory (OOM) crashes.
     */
    actual fun playSound(fileName: String) {
        val url = getResourceUrl(fileName)
        if (url == null) {
            println("Cannot play sound because URL was null: $fileName")
            return
        }

        try {
            val soundPlayer = AVAudioPlayer(contentsOfURL = url, error = null)

            soundPlayer.delegate = soundDelegate
            soundPlayer.volume = 1.0f
            activeSounds.add(soundPlayer) // Retain strong reference
            soundPlayer.prepareToPlay()

            val didPlay = soundPlayer.play()
            println("iOS sound play result for $fileName: $didPlay")
        } catch (e: Exception) {
            println("SAFE SOUND ERROR: $e")
        }
    }
}
