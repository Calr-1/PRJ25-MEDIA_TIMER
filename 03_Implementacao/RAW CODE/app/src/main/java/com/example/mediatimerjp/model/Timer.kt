package com.example.mediatimerjp.model

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.support.v4.media.session.MediaSessionCompat
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.MutableLiveData
import com.example.mediatimerjp.R
import com.example.mediatimerjp.database.Database
import com.squareup.picasso.Picasso
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

class Timer(
    var initialTimerValue: Long,
    private var timerCountdownInterval: Int,
) {

    var groupAssociated: TimerCommon? = null
    var mode: String = "HH/MM/SS"
    val CHANNEL_ID = "channel1"
    var currentTimerValue: Long = 0
    var progress = MutableLiveData<Int>()
    var intervalArrayNumber: ArrayList<Long>
    var intervalArrayTime: ArrayList<Long>
    var intervalArrayRandom: ArrayList<Long>
    var timerRunning: Boolean = false
    var timerCreated: Boolean = false
    var finished: Boolean = false
    var notifications: Notifications
    var sounds: ArrayList<Uri>
    var media: Uri
    var upload: String
    var initialTimerForProgressBar: Long
    lateinit var timerColors: TimerColors
    var intervalSounds: ArrayList<AlarmPlayer> = ArrayList()
    lateinit var countDownTimerObject: CountDownTimer
    lateinit var alarm: AlarmPlayer
    lateinit var backgroundSound: AlarmPlayer
    lateinit var context: Context
    lateinit var notificationManager: NotificationManagerCompat
    lateinit var mediaSession: MediaSessionCompat
    var alarmUri: Uri
    var enableProgressiveTimer: Boolean
    var measurementIncrease: String
    var consecutiveIncrease: Int
    var secondsIncrease: Int
    var measurementDecrease: String
    var consecutiveDecrease: Int
    var secondsDecrease: Int
    private var consecutiveIncreaseCounter: Int
    private var consecutiveDecreaseCounter: Int
    var arrayDates: ArrayList<ArrayList<Int>>
    var arrayTimes: ArrayList<Long>
    var arrayDatesHistory: ArrayList<ArrayList<Int>>
    var arrayTimesHistory: ArrayList<Long>
    private var first = true
    var name = ""
    var id: String = ""
    var canNotify = true
    var mainSoundVolume = 100
    var intervalsSoundVolume = 100
    var minProgressiveValue: Long
    var maxProgressiveValue: Long
    var typeOfAlarm: String = "Repetitions"
    var alarmRepetitions: Int = 0
    var alarmTime: Long = 0L
    var favorite = false
    var backgroundSoundVolume = 100

    init {
        currentTimerValue = initialTimerValue
        progress.value = 100
        notifications = Notifications()
        sounds = ArrayList()
        media = Uri.EMPTY
        upload = ""
        alarmUri = Uri.EMPTY
        initialTimerForProgressBar = 0L
        enableProgressiveTimer = false
        measurementIncrease = "Day"
        consecutiveIncrease = 5
        secondsIncrease = 15
        measurementDecrease = "Day"
        consecutiveDecrease = 3
        secondsDecrease = 10
        consecutiveIncreaseCounter = 1
        consecutiveDecreaseCounter = 1
        arrayDates = ArrayList()
        arrayTimes = ArrayList()
        arrayDatesHistory = ArrayList()
        arrayTimesHistory = ArrayList()
        intervalArrayTime = ArrayList()
        intervalArrayNumber = ArrayList()
        intervalArrayRandom = ArrayList()
        minProgressiveValue = initialTimerValue - (secondsDecrease * 3000).toLong()
        if (minProgressiveValue < 0L) minProgressiveValue = 0L
        maxProgressiveValue = initialTimerValue + (secondsIncrease * 3000).toLong()
    }

    private fun createTimer(
        initialTimerValue: Long,
        timerCountdownInterval: Int,
        context: Context? = null,
        common: TimerCommon
    ) {
        finished = false
        timerCreated = true
        timerRunning = true
        progress.value = 0
        if (context != null) {
            this.context = context
        }

        alarm = AlarmPlayer(this.context, R.raw.alarm)
        alarm.setRingtone(alarmUri)
        alarm.setVolume(mainSoundVolume)
        alarm.uri = alarmUri
        if (alarmTime == 0L) alarmTime = alarm.getDuration().toLong()

        notificationManager = NotificationManagerCompat.from(this.context)
        mediaSession = MediaSessionCompat(this.context, "tag")

        if (canNotify) {
            sendNotification()
            canNotify = false
        }

        if (arrayDates.size == 0) {
            arrayDates.add(getDate())
            arrayTimes.add(initialTimerValue)
        }

        countDownTimerObject = object : CountDownTimer(
            initialTimerValue,
            timerCountdownInterval.toLong()
        ) {
            override fun onTick(p0: Long) {
                currentTimerValue = p0
                progress.value = (100.0 * currentTimerValue / initialTimerForProgressBar).toInt()
                common.updateView("timer", finished)

                when (notifications.getTypeOfInterval()) {
                    "Number of Intervals" -> {
                        if (intervalArrayNumber.isNotEmpty()) {
                            if (abs(intervalArrayNumber[0] - currentTimerValue) < 200) {
                                playNotification()
                                intervalArrayNumber.removeFirst()
                            }
                        }
                    }
                    "Timed Intervals" -> {
                        if (intervalArrayTime.isNotEmpty()) {
                            if (abs(intervalArrayTime[0] - currentTimerValue) < 200) {
                                playNotification()
                                intervalArrayTime.removeFirst()
                            }
                        }
                    }
                    "Random Intervals" -> {
                        if (intervalArrayTime.isNotEmpty()) {
                            if (abs(intervalArrayRandom[0] - currentTimerValue) < 200) {
                                playNotification()
                                intervalArrayRandom.removeFirst()
                            }
                        }
                    }
                }
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onFinish() {
                if (::backgroundSound.isInitialized) backgroundSound.stopSoundObject(context)
                alarm.setVolume(mainSoundVolume)
                alarm.startSoundObject(true, typeOfAlarm, alarmRepetitions, alarmTime, context)
                finished = true
                timerRunning = false
                canNotify = true
                common.alterButton(2)
                if (common.groupAssociated != null) {
                    if (common.groupAssociated!!.group!!.sequence) {
                        common.groupAssociated!!.group!!.action(common)

                    }
                }
                if (enableProgressiveTimer) {
                    getProgressiveness(common)
                } else {
                    arrayDates.clear()
                    arrayTimes.clear()
                }
            }

        }.start()

    }


    private fun sendNotification() {
        val title: String = name
        val message = "timer running"
        val isInGroup = TimerWrapper().millisecondsToHoursMinutesAndSeconds(initialTimerValue.toInt())

        var notification: Notification
        if (media == Uri.EMPTY) {
            val artwork = BitmapFactory.decodeResource(
                context.resources,
                R.mipmap.ic_launcher
            )
            notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setLargeIcon(artwork)
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                )
                .setSubText(isInGroup)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
            notificationManager.notify(1, notification)
        } else {
            if (TimerWrapper.getInstance().isImageFile(media)) {
                Picasso.get().load(upload).into(object : com.squareup.picasso.Target {
                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        notification =
                            NotificationCompat.Builder(context, CHANNEL_ID)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle(title)
                                .setContentText(message)
                                .setLargeIcon(bitmap)
                                .setStyle(
                                    androidx.media.app.NotificationCompat.MediaStyle()
                                )
                                .setSubText(isInGroup)
                                .setPriority(NotificationCompat.PRIORITY_LOW)
                                .build()

                        notificationManager.notify(1, notification)
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                })
            } else {
                val artwork = TimerWrapper().retriveVideoFrameFromVideo(upload)
                notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setLargeIcon(artwork)
                    .setStyle(
                        androidx.media.app.NotificationCompat.MediaStyle()
                    )
                    .setSubText(isInGroup)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build()
                notificationManager.notify(1, notification)
            }
        }
    }

    fun setIntervals(initialTimerValue: Long) {
        intervalSounds = ArrayList()
        when (notifications.getTypeOfInterval()) {
            "Timed Intervals" -> {
                notifications.setTimeIntervalsArray(initialTimerValue)
                intervalArrayTime = notifications.getTimeIntervalsArray()
                for (i in intervalArrayTime.indices) {
                    val ap = AlarmPlayer(this.context, R.raw.notification)
                    if (i < sounds.size) {
                        if (sounds[i] != Uri.EMPTY) ap.setRingtone(sounds[i])
                    }
                    ap.setVolume(intervalsSoundVolume)
                    intervalSounds.add(ap)
                }
            }
            "Number of Intervals" -> {
                notifications.setNumberIntervalsArray(initialTimerValue)
                intervalArrayNumber = notifications.getNumberIntervalsArray()
                for (i in intervalArrayNumber.indices) {
                    val ap = AlarmPlayer(this.context, R.raw.notification)
                    if (i < sounds.size) {
                        if (sounds[i] != Uri.EMPTY) ap.setRingtone(sounds[i])
                    }
                    ap.setVolume(intervalsSoundVolume)
                    intervalSounds.add(ap)
                }
            }
            "Random Intervals" -> {
                notifications.setRandomIntervalsArray(initialTimerValue)
                intervalArrayRandom = notifications.getRandomIntervalsArray()
                for (i in intervalArrayRandom.indices) {
                    val ap = AlarmPlayer(this.context, R.raw.notification)
                    if (i < sounds.size) {
                        if (sounds[i] != Uri.EMPTY) ap.setRingtone(sounds[i])
                    }
                    ap.setVolume(intervalsSoundVolume)
                    intervalSounds.add(ap)
                }
            }
            "No Intervals" -> {
                intervalArrayTime.clear()
                intervalArrayNumber.clear()
                intervalArrayRandom.clear()
            }
        }
    }

    fun playNotification() {
        if(intervalSounds.size>0){
            intervalSounds[0].startSoundObject(false, typeOfAlarm, alarmRepetitions, alarmTime, context)
            intervalSounds.removeFirst()
        }
    }


    fun restart(common: TimerCommon) {
        if (!timerRunning) {
            currentTimerValue = initialTimerForProgressBar
            initialTimerValue = currentTimerValue
            common.updateView("timer", finished)
            canNotify = true
            progress.value = 100
        }
    }

    fun action(common: TimerCommon, context: Context) {
        if (!timerCreated) {
            setIntervals(initialTimerValue)
            createTimer(initialTimerValue, timerCountdownInterval, context, common = common)
            initialTimerForProgressBar = initialTimerValue
            common.alterButton(1)
            if (media != Uri.EMPTY && !TimerWrapper.getInstance()
                    .isImageFile(media) && !TimerWrapper.getInstance().isVideoFile(media)
            ) {
                if (!this::backgroundSound.isInitialized) {
                    initializeBackgroundSound()
                    setBackgroundSound(Uri.parse(upload))
                }
                backgroundSound.setVolume(backgroundSoundVolume)
                backgroundSound.startSoundObject(false, "", 0, 0L, context)
            }
        } else if (finished) {
            timerCreated = false
            if (this::alarm.isInitialized) {
                alarm.stopSoundObject(context)
                alarm.stopVibrate()
            }
            finished = false
            common.alterButton(0)
        } else if (timerRunning) {
            if(this::countDownTimerObject.isInitialized){
                countDownTimerObject.cancel()
            }
            timerRunning = false
            common.alterButton(0)
            if (this::backgroundSound.isInitialized) backgroundSound.pauseSoundObject()
        } else {
            setIntervals(initialTimerValue)
            createTimer(currentTimerValue, timerCountdownInterval, context, common = common)
            common.alterButton(1)
            if (media != Uri.EMPTY && !TimerWrapper.getInstance()
                    .isImageFile(media) && !TimerWrapper.getInstance().isVideoFile(media)
            ) {
                if (!this::backgroundSound.isInitialized) {
                    initializeBackgroundSound()
                    setBackgroundSound(Uri.parse(upload))
                }
                backgroundSound.setVolume(backgroundSoundVolume)
                backgroundSound.startSoundObject(false, "", 0, 0L, context)
            }
        }
    }

    private fun getDate(): ArrayList<Int> {
        first = false
        val c = Calendar.getInstance()

        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)


        val array = ArrayList<Int>()
        array.add(year)
        array.add(month)
        array.add(day)
        array.add(hour)
        array.add(minute)
        return array
    }

    fun setId() {
        if (id == "") id = Database.getKey(id)
    }

    fun getTimerToSave(): TimerToSave {
        setId()
        val timerToSave = TimerToSave(
            "timer",
            name,
            initialTimerValue,
            timerCountdownInterval,
            mode,
            currentTimerValue,
            intervalArrayNumber,
            intervalArrayTime,
            intervalArrayRandom,
            timerRunning,
            timerCreated,
            finished,
            notifications,
            uriToString(sounds),
            media.toString(),
            upload,
            initialTimerForProgressBar,
            enableProgressiveTimer,
            measurementIncrease,
            consecutiveIncrease,
            secondsIncrease,
            measurementDecrease,
            consecutiveDecrease,
            secondsDecrease,
            consecutiveIncreaseCounter,
            consecutiveDecreaseCounter,
            arrayDates,
            arrayTimes,
            first,
            alarmUri.toString(),
            null,
            null,
            null,
            null,
            null,
            timerColors,
            id,
            canNotify,
            arrayDatesHistory,
            arrayTimesHistory,
            mainSoundVolume,
            intervalsSoundVolume,
            minProgressiveValue,
            maxProgressiveValue,
            typeOfAlarm,
            alarmRepetitions,
            alarmTime,
            favorite,
            backgroundSoundVolume
        )
        Database.saveTimer(timerToSave)
        return timerToSave
    }

    fun setTimer(timer: TimerToSave) {
        initialTimerValue = timer.initialTimerValue!!
        timerCountdownInterval = timer.timerCountdownInterval!!
        mode = timer.mode!!
        currentTimerValue = timer.currentTimerValue!!
        intervalArrayNumber = if (timer.intervalArrayNumber != null) {
            timer.intervalArrayNumber!!
        } else ArrayList()
        intervalArrayTime = if (timer.intervalArrayTime != null) {
            timer.intervalArrayTime!!
        } else ArrayList()
        intervalArrayRandom = if (timer.intervalArrayRandom != null) {
            timer.intervalArrayRandom!!
        } else ArrayList()
        timerRunning = timer.timerRunning!!
        timerCreated = timer.timerCreated!!
        finished = timer.finished!!
        sounds = if (timer.sounds != null) {
            stringToUri(timer.sounds!!)
        } else ArrayList()
        media = Uri.parse(timer.media)!!
        initialTimerForProgressBar = timer.initialTimerForProgressBar!!
        enableProgressiveTimer = timer.enableProgressiveTimer!!
        measurementIncrease = timer.measurementIncrease!!
        consecutiveIncrease = timer.consecutiveIncrease!!
        secondsIncrease = timer.secondsIncrease!!
        measurementDecrease = timer.measurementDecrease!!
        consecutiveDecrease = timer.consecutiveDecrease!!
        secondsDecrease = timer.secondsDecrease!!
        consecutiveIncreaseCounter = timer.consecutiveIncreaseCounter!!
        consecutiveDecreaseCounter = timer.consecutiveDecreaseCounter!!
        arrayDates = if (timer.arrayDates != null) {
            timer.arrayDates!!
        } else ArrayList()
        arrayTimes = if (timer.arrayTimes != null) {
            timer.arrayTimes!!
        } else ArrayList()
        first = timer.first!!
        notifications = timer.notifications!!
        name = timer.name!!
        alarmUri = Uri.parse(timer.alarmUri!!)
        timerColors = timer.timerColors!!
        id = timer.id!!
        upload = timer.upload!!
        canNotify = timer.canNotify!!
        arrayTimesHistory =
            if (timer.arrayTimesHistory != null) timer.arrayTimesHistory!! else ArrayList()
        arrayDatesHistory =
            if (timer.arrayDatesHistory != null) timer.arrayDatesHistory!! else ArrayList()
        mainSoundVolume = timer.mainSoundVolume!!
        intervalsSoundVolume = timer.intervalsSoundVolume!!
        minProgressiveValue = timer.minProgressiveValue!!
        maxProgressiveValue = timer.maxProgressiveValue!!
        typeOfAlarm = timer.typeOfAlarm!!
        alarmRepetitions = timer.alarmRepetitions!!
        alarmTime = timer.alarmTime!!
        favorite = timer.favorite
        backgroundSoundVolume = timer.backgroundSoundVolume!!
        progress.value = (100.0 * currentTimerValue / initialTimerForProgressBar).toInt()
    }

    fun startFromWhereItLeftOff(context: Context, timerCommon: TimerCommon) {
        if (!this::countDownTimerObject.isInitialized && timerRunning) {
            createTimer(
                currentTimerValue,
                timerCountdownInterval,
                context,
                timerCommon
            )
            initialTimerForProgressBar = initialTimerValue
            timerCommon.alterButton(1)
        } else {
            timerCommon.alterButton(0)
        }
    }

    private fun uriToString(arrayList: ArrayList<Uri>): ArrayList<String> {
        val array = ArrayList<String>()
        arrayList.forEach {
            array.add(it.toString())
        }
        return array
    }

    private fun stringToUri(arrayList: ArrayList<String>): ArrayList<Uri> {
        val array = ArrayList<Uri>()
        arrayList.forEach {
            array.add(Uri.parse(it))
        }
        return array
    }

    //Esta feito para mudar de X em X dias, se for para mudar apartir de X dias e diferente
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getProgressiveness(common: TimerCommon) {
        val lastDateArray = arrayDates.last()
        val lastDate = LocalDateTime.of(
            lastDateArray[0],
            lastDateArray[1],
            lastDateArray[2],
            lastDateArray[3],
            lastDateArray[4]
        )
        val thisDateArray = getDate()
        val thisDate = LocalDateTime.of(
            thisDateArray[0],
            thisDateArray[1],
            thisDateArray[2],
            thisDateArray[3],
            thisDateArray[4]
        )
        val days = ChronoUnit.DAYS.between(lastDate, thisDate).toInt()
        val weeks = ChronoUnit.WEEKS.between(lastDate, thisDate).toInt()
        val months = ChronoUnit.MONTHS.between(lastDate, thisDate).toInt()
        if (maxProgressiveValue < minProgressiveValue) maxProgressiveValue = minProgressiveValue
        if (maxProgressiveValue == 0L) maxProgressiveValue =
            initialTimerValue + (secondsIncrease * 3)


        when (measurementIncrease) {
            "Day" -> {
                if (days > 1) {
                    consecutiveIncreaseCounter = 1
                } else if (days == 1) {
                    if (consecutiveIncreaseCounter < consecutiveIncrease) {
                        consecutiveIncreaseCounter++
                    }
                    if (consecutiveIncreaseCounter == consecutiveIncrease) {
                        arrayDates.clear()
                        arrayTimes.clear()
                        //AUMENTAR O INITIAL TIME
                        initialTimerValue += secondsIncrease * 1000
                        initialTimerForProgressBar+= secondsIncrease * 1000
                        if (initialTimerValue > maxProgressiveValue){
                            initialTimerValue = maxProgressiveValue
                            initialTimerForProgressBar = maxProgressiveValue
                        }
                        consecutiveIncreaseCounter = 0
                    }
                    arrayDates.add(thisDateArray)
                    arrayTimes.add(initialTimerValue)
                    arrayDatesHistory.add(thisDateArray)
                    arrayTimesHistory.add(initialTimerValue)
                }

            }
            "Week" -> {
                if (weeks > 1) {
                    consecutiveIncreaseCounter = 1


                } else if (weeks == 1) {
                    if (consecutiveIncreaseCounter < consecutiveIncrease) {
                        consecutiveIncreaseCounter++
                    }
                    if (consecutiveIncreaseCounter == consecutiveIncrease) {
                        arrayDates.clear()
                        arrayTimes.clear()
                        //AUMENTAR O INITIAL TIME
                        initialTimerValue += secondsIncrease * 1000
                        initialTimerForProgressBar+= secondsIncrease * 1000
                        if (initialTimerValue > maxProgressiveValue){
                            initialTimerValue = maxProgressiveValue
                            initialTimerForProgressBar = maxProgressiveValue
                        }
                        consecutiveIncreaseCounter = 0
                    }
                    arrayDates.add(thisDateArray)
                    arrayTimes.add(initialTimerValue)
                    arrayDatesHistory.add(thisDateArray)
                    arrayTimesHistory.add(initialTimerValue)
                }

            }
            "Month" -> {
                if (months > 1) {
                    consecutiveIncreaseCounter = 1

                } else if (months == 1) {
                    if (consecutiveIncreaseCounter < consecutiveIncrease) {
                        consecutiveIncreaseCounter++
                    }
                    if (consecutiveIncreaseCounter == consecutiveIncrease) {
                        arrayDates.clear()
                        arrayTimes.clear()
                        //AUMENTAR O INITIAL TIME
                        initialTimerValue += secondsIncrease * 1000
                        initialTimerForProgressBar+= secondsIncrease * 1000
                        if (initialTimerValue > maxProgressiveValue){
                            initialTimerValue = maxProgressiveValue
                            initialTimerForProgressBar = maxProgressiveValue
                        }
                        consecutiveIncreaseCounter = 0
                    }
                    arrayDates.add(thisDateArray)
                    arrayTimes.add(initialTimerValue)
                    arrayDatesHistory.add(thisDateArray)
                    arrayTimesHistory.add(initialTimerValue)
                }
            }
        }
        //Toast.makeText(context, consecutiveIncreaseCounter.toString(), Toast.LENGTH_SHORT).show()
        common.updateView("timer", false)
    }

    fun initializeBackgroundSound() {
        if (!this::backgroundSound.isInitialized) backgroundSound =
            AlarmPlayer(context, R.raw.alarm)
    }

    fun setBackgroundSound(uri: Uri) {
        initializeBackgroundSound()
        backgroundSound.setRingtone(uri)
        backgroundSound.setVolume(backgroundSoundVolume)
    }


}