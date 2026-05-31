package com.piepie.brainhouse.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.media.ToneGenerator
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.io.File
import java.util.Locale

class SoundManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var mediaPlayer: MediaPlayer? = null
    private var soundPool: SoundPool? = null
    private var toneGenerator: ToneGenerator? = null
    private var isTtsReady = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private val speechCacheDir = File(context.cacheDir, "speech_cache_v2")

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

        toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            
        // Load default system sounds or placeholders if assets not yet available
        // Ideally we load from R.raw.*
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.CHINESE)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("SoundManager", "Chinese language is not supported or missing data")
            } else {
                tts?.setSpeechRate(1.35f)
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

    fun prepareSpeechCache(
        texts: List<String>,
        onProgress: (prepared: Int, total: Int) -> Unit,
        onComplete: () -> Unit
    ) {
        val uniqueTexts = texts.distinct()
        if (uniqueTexts.isEmpty()) {
            onComplete()
            return
        }
        if (!isTtsReady) {
            mainHandler.postDelayed({
                prepareSpeechCache(uniqueTexts, onProgress, onComplete)
            }, 250L)
            return
        }

        speechCacheDir.mkdirs()
        val missingTexts = uniqueTexts.filterNot { speechFileFor(it).exists() }
        var prepared = uniqueTexts.size - missingTexts.size
        onProgress(prepared, uniqueTexts.size)

        if (missingTexts.isEmpty()) {
            onComplete()
            return
        }

        val queue = ArrayDeque(missingTexts)
        lateinit var synthesizeNext: () -> Unit

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit

            override fun onDone(utteranceId: String?) {
                mainHandler.post {
                    prepared += 1
                    onProgress(prepared, uniqueTexts.size)
                    synthesizeNext()
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                mainHandler.post {
                    prepared += 1
                    onProgress(prepared, uniqueTexts.size)
                    synthesizeNext()
                }
            }
        })

        synthesizeNext = {
            val text = queue.removeFirstOrNull()
            if (text == null) {
                onComplete()
            } else {
                val params = Bundle()
                val utteranceId = "cache_${Integer.toHexString(text.hashCode())}"
                val result = tts?.synthesizeToFile(text, params, speechFileFor(text), utteranceId)
                if (result == TextToSpeech.ERROR) {
                    prepared += 1
                    onProgress(prepared, uniqueTexts.size)
                    synthesizeNext()
                }
            }
        }

        synthesizeNext()
    }

    fun playSpeech(text: String) {
        val cachedFile = speechFileFor(text)
        if (cachedFile.exists()) {
            try {
                voicePlayer?.release()
                voicePlayer = MediaPlayer.create(context, Uri.fromFile(cachedFile))
                voicePlayer?.start()
                voicePlayer?.setOnCompletionListener { mp ->
                    mp.release()
                    if (voicePlayer == mp) {
                        voicePlayer = null
                    }
                }
                return
            } catch (e: Exception) {
                Log.e("SoundManager", "Failed to play cached speech: $text", e)
            }
        }
        speak(text)
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

    fun playCorrect() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 120)
    }

    fun playWrong() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 180)
    }

    private var voicePlayer: MediaPlayer? = null

    private fun speechFileFor(text: String): File {
        return File(speechCacheDir, "${Integer.toHexString(text.hashCode())}.wav")
    }

    fun playVoice(resId: Int) {
        try {
            voicePlayer?.release() // Stop previous
            voicePlayer = null
            
            voicePlayer = MediaPlayer.create(context, resId)
            voicePlayer?.start()
            voicePlayer?.setOnCompletionListener { mp ->
                mp.release()
                if (voicePlayer == mp) {
                    voicePlayer = null
                }
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Failed to play voice: $resId", e)
        }
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        mediaPlayer?.release()
        voicePlayer?.release()
        soundPool?.release()
        toneGenerator?.release()
    }
}
