package ru.finny.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator
import ru.finny.app.R

/**
 * Менеджер звуковых эффектов. Загружает фирменный MP3-звук из res/raw/click_sound.mp3.
 */
object SoundManager {
    private var toneGen: ToneGenerator? = null
    private var soundPool: SoundPool? = null
    private var customSoundId: Int = 0

    init {
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (_: Exception) {
            toneGen = null
        }
    }

    fun init(context: Context) {
        if (soundPool != null) return
        try {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            soundPool = SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(attrs)
                .build()
            customSoundId = soundPool?.load(context.applicationContext, R.raw.click_sound, 1) ?: 0
        } catch (_: Exception) {}
    }

    private fun playCustomSound() {
        if (soundPool != null && customSoundId != 0) {
            soundPool?.play(customSoundId, 1f, 1f, 1, 0, 1f)
        } else {
            try {
                toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
            } catch (_: Exception) {}
        }
    }

    fun playClick() {
        playCustomSound()
    }

    fun playPet() {
        playCustomSound()
    }

    fun playCoin() {
        playCustomSound()
    }

    fun playSuccess() {
        playCustomSound()
    }
}
