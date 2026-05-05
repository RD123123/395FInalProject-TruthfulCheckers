package edu.moravian.csci215.finalproject395_truthfulcheckers.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileOutputStream

/**
 * The Android-specific implementation of the cross-platform AudioPlayer.
 * * Utilizes Android's native [MediaPlayer] API to handle playback. Because Kotlin Multiplatform
 * bundles assets in a way that MediaPlayer cannot directly stream, this class acts as an
 * intelligent proxy: it locates the bundled asset, copies it to the device's local cache,
 * and streams it from there.
 * * @property context Injected dynamically via Koin to access Android system assets.
 */
actual class AudioPlayer actual constructor() : KoinComponent {
    private val context: Context by inject()
    private var mediaPlayer: MediaPlayer? = null
    private val TAG = "TruthfulCheckersAudio"

    // Caches the resolved asset paths to prevent expensive recursive lookups on subsequent plays.
    private val pathCache = mutableMapOf<String, String>()

    /**
     * Recursively searches the Android assets directory to find the actual location
     * of the requested audio file, which varies based on the KMP compiler version.
     */
    private fun findAssetPath(
        fileName: String,
        root: String,
    ): String? {
        try {
            val list = context.assets.list(root) ?: return null
            for (item in list) {
                val path = if (root.isEmpty()) item else "$root/$item"
                if (item.equals(fileName, ignoreCase = true)) return path

                val subList = context.assets.list(path)
                if (!subList.isNullOrEmpty()) {
                    val found = findAssetPath(fileName, path)
                    if (found != null) return found
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning assets folder for $fileName", e)
        }
        return null
    }

    /**
     * Attempts to resolve the file path using known fast-paths before falling back
     * to the heavy recursive search.
     */
    private fun getFilePath(fileName: String): String? {
        pathCache[fileName]?.let { return it }

        val commonPaths =
            listOf(
                "composeResources/truthfulcheckers.composeapp.generated.resources/files/$fileName",
                "composeResources/files/$fileName",
                "files/$fileName",
                "compose-resources/files/$fileName",
                fileName,
            )

        for (path in commonPaths) {
            try {
                // Quick test to see if the file exists at this path
                context.assets.open(path).use { it.read() }
                pathCache[fileName] = path
                return path
            } catch (_: Exception) {
                // Ignore and try the next common path
            }
        }

        // Fallback to recursive search if fast-paths fail
        val found = findAssetPath(fileName, "")
        if (found != null) {
            pathCache[fileName] = found
            return found
        }
        return null
    }

    /**
     * Prepares an Android MediaPlayer instance.
     * Copies the file from the read-only APK assets into the writable cache directory
     * so MediaPlayer has a valid absolute path to read from.
     */
    private fun createMediaPlayer(fileName: String): MediaPlayer? {
        val path =
            getFilePath(fileName) ?: run {
                Log.e(TAG, "File not found in assets: $fileName")
                return null
            }

        val attributes =
            AudioAttributes
                .Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

        return try {
            val tempFile = File(context.cacheDir, "audio_cache_${fileName.hashCode()}")

            // Only perform the expensive file-copy if it hasn't been cached yet
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
            Log.e(TAG, "Failed to load and prepare audio: $fileName", e)
            null
        }
    }

    /**
     * Instantiates and starts a looping background music track.
     */
    actual fun playMusic(
        fileName: String,
        loop: Boolean,
    ) {
        stopMusic()
        try {
            createMediaPlayer(fileName)?.apply {
                isLooping = loop
                setVolume(1.0f, 1.0f)
                mediaPlayer = this
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Crash during playMusic initialization", e)
        }
    }

    /**
     * Halts and safely releases the background music player's memory resources.
     */
    actual fun stopMusic() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error while stopping music", e)
        } finally {
            mediaPlayer = null
        }
    }

    /**
     * Fires a one-shot sound effect. Auto-releases its memory via an OnCompletionListener
     * to prevent out-of-memory (OOM) crashes during rapid rapid piece capturing.
     */
    actual fun playSound(fileName: String) {
        try {
            createMediaPlayer(fileName)?.apply {
                setVolume(1.0f, 1.0f)
                setOnCompletionListener { it.release() } // Critical for avoiding memory leaks
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Crash during playSound initialization", e)
        }
    }
}
