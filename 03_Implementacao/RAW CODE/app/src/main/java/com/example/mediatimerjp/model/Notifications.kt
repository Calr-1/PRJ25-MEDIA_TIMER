package com.example.mediatimerjp.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs

class Notifications {
    private var numberIntervals: ArrayList<Long>
    var timeIntervals: ArrayList<Long>
    private var randomIntervals: ArrayList<Long>
    private var typeOfInterval: String
    private var intervalTime: Long
    private var intervalNumber: Int
    private var intervalRandomMax: Int
    private var intervalRandomMin: Int
    private var enableVibration: Boolean
    private var randomSounds:Boolean

    init {
        intervalTime = 0L
        intervalNumber = 1
        typeOfInterval = "No intervals"
        enableVibration = false
        intervalRandomMax = 1
        intervalRandomMin = 1
        randomSounds = false
        numberIntervals = ArrayList()
        timeIntervals = ArrayList()
        randomIntervals = ArrayList()
    }

    fun setNumberIntervalsArray(timerInitialValue: Long) {
        println("numero de intervalos definidos pelo user: $intervalNumber")
        numberIntervals.clear()
        if (intervalNumber == 0) {
            numberIntervals.add(0.toLong())
        } else if (intervalNumber == 1) {
            val l = timerInitialValue / (intervalNumber + 1)
            numberIntervals.add(l)
        } else {
            val n = intervalNumber + 1
            var l: Long = 0
            l = if (n % 2 == 0) {
                timerInitialValue / n
            } else {
                timerInitialValue / n + 1
            }
            var total = timerInitialValue
            while (total > 0) {
                total -= l
                if (total > 0) {
                    numberIntervals.add(total)
                }
            }
        }
    }

    fun setTimeIntervalsArray(timerInitialValue: Long) {
        println("intervalo de tempo definido pelo user: $intervalTime")
        if (intervalTime > 0) {
            timeIntervals.clear()
            var total = timerInitialValue
            while (total > 0) {
                total -= intervalTime
                if (total > 0) {
                    timeIntervals.add(total)
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun setRandomIntervalsArray(
        timerInitialValue: Long
    ): ArrayList<Long> {
        val intervals = ArrayList<Long>()
        var time = timerInitialValue
        val interval = timerInitialValue / (intervalRandomMax + 1)
        var numberOfIntervals = 0
        numberOfIntervals = if (intervalRandomMax > intervalRandomMin) {
            val r = Random()
            r.nextInt(intervalRandomMax + 1 - intervalRandomMin) + intervalRandomMin
        } else intervalRandomMax
        for (i in 0 until numberOfIntervals) {
            val random = Random()
            val low = -40
            val high = 40
            val r = random.nextInt(high - low) + low
            val percentage = ((100.0 + r) / 100.0).toFloat()
            val alarm = (interval * percentage).toLong()
            if (i == intervalRandomMax - 1) intervals.add(abs(time - alarm)) else intervals.add(time - alarm)
            time -= interval
        }
        return intervals
    }


    fun getNumberIntervalsArray(): ArrayList<Long> {
        return numberIntervals
    }

    fun getTimeIntervalsArray(): ArrayList<Long> {
        return timeIntervals
    }

    fun getRandomIntervalsArray(): ArrayList<Long> {
        return randomIntervals
    }

    fun getTypeOfInterval(): String {
        return typeOfInterval
    }

    fun setTypeOfInterval(typeOfInterval: String) {
        this.typeOfInterval = typeOfInterval
    }

    fun getIntervalTime(): Long {
        return intervalTime
    }

    fun setIntervalTime(intervalTime: Long) {
        this.intervalTime = intervalTime
    }

    fun getNumberOfIntervals(): Int {
        return intervalNumber
    }

    fun setNumberOfIntervals(numberOfIntervals: Int) {
        intervalNumber = numberOfIntervals
    }

    fun getRandomIntervalsMax(): Int {
        return intervalRandomMax
    }

    fun setRandomIntervalsMax(intervalRandomMax: Int) {
        this.intervalRandomMax = intervalRandomMax
    }

    fun getRandomIntervalsMin(): Int {
        return intervalRandomMin
    }

    fun setRandomIntervalsMin(intervalRandomMin: Int) {
        this.intervalRandomMin = intervalRandomMin
    }

    fun isEnableVibration():Boolean{
        return enableVibration
    }

    fun setEnableVibration(vibration:Boolean){
        this.enableVibration = vibration
    }

    fun isRandomSounds():Boolean{
        return randomSounds
    }

    fun setRandomSounds(random:Boolean){
        randomSounds = random
    }
}