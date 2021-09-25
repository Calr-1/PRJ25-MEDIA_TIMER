package com.example.mediatimerjp.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

class TimerToSave() {
    var type: String? = null
    var name: String? = null
    var initialTimerValue: Long? = null
    var timerCountdownInterval: Int? = null
    var mode: String? = null
    var currentTimerValue: Long? = null
    var intervalArrayNumber: ArrayList<Long>? = null
    var intervalArrayTime: ArrayList<Long>? = null
    var intervalArrayRandom: ArrayList<Long>? = null
    var timerRunning: Boolean? = null
    var timerCreated: Boolean? = null
    var finished: Boolean? = null
    var notifications: Notifications? = null
    var sounds: ArrayList<String>? = null
    var media: String? = null
    var upload: String? = null
    var initialTimerForProgressBar: Long? = null
    var enableProgressiveTimer: Boolean? = null
    var measurementIncrease: String? = null
    var consecutiveIncrease: Int? = null
    var secondsIncrease: Int? = null
    var measurementDecrease: String? = null
    var consecutiveDecrease: Int? = null
    var secondsDecrease: Int? = null
    var consecutiveIncreaseCounter: Int? = null
    var consecutiveDecreaseCounter: Int? = null
    var arrayDates: ArrayList<ArrayList<Int>>? = null
    var arrayTimes: ArrayList<Long>? = null
    var first: Boolean? = null
    var alarmUri: String? = null
    var timers: ArrayList<TimerToSave>? = null
    var seq: Boolean? = null
    var stopAtZero: Boolean? = null
    var skipGroups: Boolean? = null
    var currentRunning: Int? = null
    var timerColors: TimerColors? = null
    var id: String? = null
    var canNotify: Boolean? = null
    var arrayDatesHistory: ArrayList<ArrayList<Int>>? = null
    var arrayTimesHistory: ArrayList<Long>? = null
    var mainSoundVolume:Int? = null
    var intervalsSoundVolume:Int? = null
    var minProgressiveValue:Long? = null
    var maxProgressiveValue:Long? = null
    var typeOfAlarm:String? = null
    var alarmRepetitions:Int? = null
    var alarmTime:Long? = null
    var favorite:Boolean = false
    var backgroundSoundVolume:Int? = 100

    constructor(
        type: String?,
        name: String?,
        initialTimerValue: Long?,
        timerCountdownInterval: Int?,
        mode: String?,
        currentTimerValue: Long?,
        intervalArrayNumber: ArrayList<Long>?,
        intervalArrayTime: ArrayList<Long>?,
        intervalArrayRandom: ArrayList<Long>?,
        timerRunning: Boolean?,
        timerCreated: Boolean?,
        finished: Boolean?,
        notifications: Notifications?,
        sounds: ArrayList<String>?,
        media: String?,
        upload: String?,
        initialTimerForProgressBar: Long?,
        enableProgressiveTimer: Boolean?,
        measurementIncrease: String?,
        consecutiveIncrease: Int?,
        secondsIncrease: Int?,
        measurementDecrease: String?,
        consecutiveDecrease: Int?,
        secondsDecrease: Int?,
        consecutiveIncreaseCounter: Int?,
        consecutiveDecreaseCounter: Int?,
        arrayDates: ArrayList<ArrayList<Int>>?,
        arrayTimes: ArrayList<Long>?,
        first: Boolean?,
        alarmUri: String?,
        timers: ArrayList<TimerToSave>?,
        seq: Boolean?,
        stopAtZero: Boolean?,
        skipGroups: Boolean?,
        currentRunning: Int?,
        timerColors: TimerColors?,
        id: String?,
        canNotify: Boolean?,
        arrayDatesHistory: ArrayList<ArrayList<Int>>?,
        arrayTimesHistory: ArrayList<Long>?,
        mainSoundVolume:Int?,
        intervalsSoundVolume:Int?,
        minProgressiveValue:Long?,
        maxProgressiveValue:Long?,
        typeOfAlarm:String?,
        alarmRepetitions:Int?,
        alarmTime:Long?,
        favorite:Boolean,
        backgroundSoundVolume:Int?
    ) : this() {
        this.type = type
        this.name = name
        this.initialTimerValue = initialTimerValue
        this.timerCountdownInterval = timerCountdownInterval
        this.mode = mode
        this.currentTimerValue = currentTimerValue
        this.intervalArrayNumber = intervalArrayNumber
        this.intervalArrayTime = intervalArrayTime
        this.intervalArrayRandom = intervalArrayRandom
        this.timerRunning = timerRunning
        this.timerCreated = timerCreated
        this.finished = finished
        this.notifications = notifications
        this.sounds = sounds
        this.media = media
        this.upload = upload
        this.initialTimerForProgressBar = initialTimerForProgressBar
        this.enableProgressiveTimer = enableProgressiveTimer
        this.measurementIncrease = measurementIncrease
        this.consecutiveIncrease = consecutiveIncrease
        this.secondsIncrease = secondsIncrease
        this.measurementDecrease = measurementDecrease
        this.consecutiveDecrease = consecutiveDecrease
        this.secondsDecrease = secondsDecrease
        this.consecutiveIncreaseCounter = consecutiveIncreaseCounter
        this.consecutiveDecreaseCounter = consecutiveDecreaseCounter
        this.arrayDates = arrayDates
        this.arrayTimes = arrayTimes
        this.first = first
        this.alarmUri = alarmUri
        this.timers = timers
        this.seq = seq
        this.stopAtZero = stopAtZero
        this.skipGroups = skipGroups
        this.currentRunning = currentRunning
        this.timerColors = timerColors
        this.id = id
        this.canNotify = canNotify
        this.arrayDatesHistory = arrayDatesHistory
        this.arrayTimesHistory = arrayTimesHistory
        this.mainSoundVolume = mainSoundVolume
        this.intervalsSoundVolume = intervalsSoundVolume
        this.minProgressiveValue = minProgressiveValue
        this.maxProgressiveValue = maxProgressiveValue
        this.typeOfAlarm = typeOfAlarm
        this.alarmRepetitions = alarmRepetitions
        this.alarmTime = alarmTime
        this.favorite = favorite
        this.backgroundSoundVolume = backgroundSoundVolume
    }
}