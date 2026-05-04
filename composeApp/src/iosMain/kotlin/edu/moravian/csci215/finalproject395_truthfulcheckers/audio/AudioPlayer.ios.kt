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

@OptIn(ExperimentalForeignApi::class, ExperimentalResourceApi::class)
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
            session.setCategory(
                AVAudioSessionCategoryPlayback,
                AVAudioSessionCategoryOptionMixWithOthers,
                null
            )
            session.setActive(true, null)
        } catch (e: Exception) {
            println("iOS audio session error: $e")
        }
    }

    private fun getResourceUrl(fileName: String): NSURL? {
        val uri = when (fileName) {
            "background_music.wav" -> Res.getUri("files/background_music.wav")
            "click.wav" -> Res.getUri("files/click.wav")
            "clack.wav" -> Res.getUri("files/clack.wav")

            "background_music.mp3" -> Res.getUri("files/background_music.mp3")
            "click.mp3" -> Res.getUri("files/click.mp3")
            "clack.mp3" -> Res.getUri("files/clack.mp3")

            else -> {
                println("Unknown iOS audio file requested: $fileName")
                return null
            }
        }

        println("iOS audio URI for $fileName: $uri")
        return NSURL.URLWithString(uri)
    }

    actual fun playMusic(fileName: String, loop: Boolean) {
        val url = getResourceUrl(fileName)
        if (url == null) {
            println("Cannot play music because URL was null: $fileName")
            return
        }

        try {
            musicPlayer?.stop()
            musicPlayer = null

            val player = AVAudioPlayer(contentsOfURL = url, error = null)

            if (player == null) {
                println("Music player failed: $fileName")
                return
            }

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

    actual fun stopMusic() {
        try {
            musicPlayer?.stop()
        } catch (e: Exception) {
            println("SAFE STOP MUSIC ERROR: $e")
        } finally {
            musicPlayer = null
        }
    }

    actual fun playSound(fileName: String) {
        val url = getResourceUrl(fileName)
        if (url == null) {
            println("Cannot play sound because URL was null: $fileName")
            return
        }

        try {
            val soundPlayer = AVAudioPlayer(contentsOfURL = url, error = null)

            if (soundPlayer == null) {
                println("Sound player failed: $fileName")
                return
            }

            soundPlayer.delegate = soundDelegate
            soundPlayer.volume = 1.0f
            activeSounds.add(soundPlayer)
            soundPlayer.prepareToPlay()

            val didPlay = soundPlayer.play()
            println("iOS sound play result for $fileName: $didPlay")
        } catch (e: Exception) {
            println("SAFE SOUND ERROR: $e")
        }
    }
}