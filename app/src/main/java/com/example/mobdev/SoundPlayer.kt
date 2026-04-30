package com.example.soundboard

import android.content.Context
import android.media.*
import android.os.Build
import android.util.Log
import android.widget.Toast

class SoundPlayer private constructor(private val context: Context) {

    companion object {
        private const val TAG = "SoundPlayer"
        @Volatile
        private var instance: SoundPlayer? = null

        fun getInstance(context: Context): SoundPlayer {
            return instance ?: synchronized(this) {
                instance ?: SoundPlayer(context.applicationContext).also { instance = it }
            }
        }
    }

    interface OnSoundCompletionListener {
        fun onSoundCompleted(soundName: String)
    }

    private var soundPool: SoundPool
    private val soundMap: MutableMap<String, Int> = HashMap()

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var currentSoundName = ""
    private var completionListener: OnSoundCompletionListener? = null

    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    init {
        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(attributes)
                .build()
        } else {
            SoundPool(10, AudioManager.STREAM_MUSIC, 0)
        }
    }

    fun loadSound(context: Context, soundName: String, resourceId: Int) {
        try {
            val soundId = soundPool.load(context, resourceId, 1)
            soundMap[soundName] = soundId
        } catch (e: Exception) {
            Log.e(TAG, "Error loading sound: $soundName", e)
            Toast.makeText(context, "Error loading sound: $soundName", Toast.LENGTH_SHORT).show()
        }
    }

    fun playShortSound(soundName: String) {
        val soundId = soundMap[soundName]
        if (soundId != null) {
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
            currentSoundName = soundName
        } else {
            Log.e(TAG, "Sound not found: $soundName")
        }
    }

    fun playLongSound(context: Context, resourceId: Int, soundName: String) {
        try {
            stopSound()

            mediaPlayer = MediaPlayer.create(context, resourceId)
            mediaPlayer?.setOnCompletionListener {
                isPlaying = false
                completionListener?.onSoundCompleted(currentSoundName)
            }
            mediaPlayer?.start()
            isPlaying = true
            currentSoundName = soundName
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound: $soundName", e)
            Toast.makeText(context, "Error playing sound", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopSound() {
        mediaPlayer?.let {
            if (isPlaying) {
                it.stop()
                it.release()
                mediaPlayer = null
                isPlaying = false
            }
        }
    }

    fun release() {
        stopSound()
        soundPool.release()
        soundMap.clear()
    }

    fun isPlaying(): Boolean = isPlaying

    fun getCurrentSoundName(): String = currentSoundName

    fun setOnSoundCompletionListener(listener: OnSoundCompletionListener) {
        completionListener = listener
    }

    fun setOnSoundCompletionListener(listener: (String) -> Unit) {
        completionListener = object : OnSoundCompletionListener {
            override fun onSoundCompleted(soundName: String) {
                listener(soundName)
            }
        }
    }
}