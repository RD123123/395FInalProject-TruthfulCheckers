package edu.moravian.csci215.finalproject395_truthfulcheckers.audio

import android.content.Context
import android.media.MediaPlayer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual class AudioPlayer actual constructor() : KoinComponent {
    private val context: Context by inject()
    private var mediaPlayer: MediaPlayer? = null

    actual fun playMusic(fileName: String, loop: Boolean) {
        stopMusic()
        val resName = fileName.substringBefore(".")
        val resId = context.resources.getIdentifier(resName, "raw", context.packageName)
        
        if (resId != 0) {
            try {
                mediaPlayer = MediaPlayer.create(context, resId).apply {
                    isLooping = loop
                    start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    actual fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    actual fun playSound(fileName: String) {
        val resName = fileName.substringBefore(".")
        val resId = context.resources.getIdentifier(resName, "raw", context.packageName)
        
        if (resId != 0) {
            try {
                MediaPlayer.create(context, resId).apply {
                    setOnCompletionListener { it.release() }
                    start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
