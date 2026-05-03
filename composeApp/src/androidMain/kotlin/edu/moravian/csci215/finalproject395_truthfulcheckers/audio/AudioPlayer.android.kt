package edu.moravian.csci215.finalproject395_truthfulcheckers.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileOutputStream

actual class AudioPlayer actual constructor() : KoinComponent {
    private val context: Context by inject()
    private var mediaPlayer: MediaPlayer? = null
    private val TAG = "TruthfulCheckersAudio"
    private val pathCache = mutableMapOf<String, String>()

    private fun findAssetPath(fileName: String, root: String): String? {
        try {
            val list = context.assets.list(root) ?: return null
            for (item in list) {
                val path = if (root.isEmpty()) item else "$root/$item"
                if (item.equals(fileName, ignoreCase = true)) return path
                
                val subList = context.assets.list(path)
                if (subList != null && subList.isNotEmpty()) {
                    val found = findAssetPath(fileName, path)
                    if (found != null) return found
                }
            }
        } catch (e: Exception) { }
        return null
    }

    private fun getFilePath(fileName: String): String? {
        pathCache[fileName]?.let { return it }
        
        val commonPaths = listOf(
            "composeResources/truthfulcheckers.composeapp.generated.resources/files/$fileName",
            "composeResources/files/$fileName",
            "files/$fileName",
            "compose-resources/files/$fileName",
            fileName
        )
        
        for (path in commonPaths) {
            try {
                context.assets.open(path).use { it.read() }
                pathCache[fileName] = path
                return path
            } catch (e: Exception) { }
        }

        val found = findAssetPath(fileName, "")
        if (found != null) {
            pathCache[fileName] = found
            return found
        }
        return null
    }

    private fun createMediaPlayer(fileName: String): MediaPlayer? {
        val path = getFilePath(fileName) ?: return null
        
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        return try {
            val tempFile = File(context.cacheDir, "audio_cache_${fileName.hashCode()}")
            if (!tempFile.exists()) {
                context.assets.open(path).use { input ->
                    FileOutputStream(tempFile).use { output -> input.copyTo(output) }
                }
            }

            MediaPlayer().apply {
                setAudioAttributes(attributes)
                setDataSource(tempFile.absolutePath)
                prepare()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load audio: $fileName", e)
            null
        }
    }

    actual fun playMusic(fileName: String, loop: Boolean) {
        stopMusic()
        try {
            createMediaPlayer(fileName)?.apply {
                isLooping = loop
                setVolume(1.0f, 1.0f)
                mediaPlayer = this
                start()
            }
        } catch (e: Exception) { }
    }

    actual fun stopMusic() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (e: Exception) {
        } finally {
            mediaPlayer = null
        }
    }

    actual fun playSound(fileName: String) {
        try {
            createMediaPlayer(fileName)?.apply {
                setVolume(1.0f, 1.0f)
                setOnCompletionListener { it.release() }
                start()
            }
        } catch (e: Exception) { }
    }
}
