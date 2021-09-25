package com.example.mediatimerjp.model

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.CountDownTimer
import android.os.Vibrator
import kotlin.math.ln

class AlarmPlayer(var context: Context, private var soundID: Int) {
    private var soundObject: MediaPlayer
    private var vibrator: Vibrator

    private var released: Boolean
    var uri: Uri
    private val maxVolume = 100


    init {
        soundObject = MediaPlayer.create(context, soundID)
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        uri = Uri.EMPTY
        released = false
    }

    fun getDuration(): Int {
        return soundObject.duration
    }

    fun setNewTime(progress: Int) {
        soundObject.seekTo(progress)
    }


    fun getCurrentTime(): Int {
        return soundObject.currentPosition
    }

    fun AlarmPlayerRecreate(context: Context) {
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        soundObject = if (uri != Uri.EMPTY) {
            MediaPlayer.create(context, uri)
        } else MediaPlayer.create(context, soundID)
    }

    fun startSoundObject(
        final: Boolean,
        type: String,
        repetitions: Int,
        time: Long,
        context: Context?
    ) {
        var count = 0
        soundObject.isLooping = false
        soundObject.start()
        if (final) {
            when (type) {
                "Time" -> {
                    var check = time
                    if (check==0L) check = soundObject.duration.toLong()
                    var countDownTimerObject = object : CountDownTimer(
                        check,
                        100L
                    ) {
                        override fun onTick(p0: Long) {}
                        override fun onFinish() {
                            stopSoundObject(context)
                        }
                    }.start()
                }
                "Repetitions"-> {
                    soundObject.setOnCompletionListener {
                        if (count < repetitions) {
                            soundObject.start()
                            count++
                        }
                        else{
                            stopSoundObject(context)
                        }
                    }
                }
            }
        }
        else{
            val time = soundObject.duration.toLong()
            var countDownTimerObject = object : CountDownTimer(
                time,
                100L
            ) {
                override fun onTick(p0: Long) {}
                override fun onFinish() {
                    stopSoundObject(context)
                }
            }.start()
        }
        released = true
    }

    fun stopSoundObject(context: Context?) {
        soundObject.stop()
        soundObject.release()
        released = false
        soundObject =
            if (uri == Uri.EMPTY) MediaPlayer.create(context, soundID) else MediaPlayer.create(
                context,
                uri
            )
    }

    fun pauseSoundObject() {
        soundObject.pause()
    }


    fun setVolume(volume: Int) {
        val new_volume =
            (1 - (ln((maxVolume - volume).toDouble()) / ln(maxVolume.toDouble()))).toFloat()
        soundObject.setVolume(new_volume, new_volume)
    }

    fun startVibrateEnd(pattern: LongArray?) {
        vibrator.vibrate(pattern, 0)
    }

    fun startVibrateInterval(time: Long) {
        vibrator.vibrate(time)
    }

    fun stopVibrate() {
        vibrator.cancel()
    }

    fun isPlaying(): Boolean {
        return released
    }

    fun setRingtone(ringtone: Uri?) {
        if (ringtone != null && ringtone!= Uri.EMPTY) {
            uri = ringtone
            soundObject = MediaPlayer.create(context, uri)
        }
    }
}