package com.piepie.brainhouse.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class SoundManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var mediaPlayer: MediaPlayer? = null
    private var soundPool: SoundPool? = null
    private var isTtsReady = false

    // Sound IDs
    var clickSoundId: Int = 0
    var correctSoundId: Int = 0
    var wrongSoundId: Int = 0
    var winSoundId: Int = 0

    init {
        tts = TextToSpeech(context, this)
        
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
            
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()
            
        // Load default system sounds or placeholders if assets not yet available
        // Ideally we load from R.raw.*
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.CHINESE)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("SoundManager", "Chinese language is not supported or missing data")
            } else {
                isTtsReady = true
            }
        } else {
            Log.e("SoundManager", "TTS logic failed to initialize")
        }
    }

    fun speak(text: String) {
        if (isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun playMusic() {
        // Placeholder for BGM logic
        // if (mediaPlayer == null) {
        //     mediaPlayer = MediaPlayer.create(context, R.raw.bgm_happy)
        //     mediaPlayer?.isLooping = true
        //     mediaPlayer?.start()
        // }
    }
    
    fun playClick() {
        // soundPool?.play(clickSoundId, 1f, 1f, 0, 0, 1f)
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        mediaPlayer?.release()
        soundPool?.release()
    }
}
