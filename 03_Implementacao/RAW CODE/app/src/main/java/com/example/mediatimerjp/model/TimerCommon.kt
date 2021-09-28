package com.example.mediatimerjp.model

import android.content.Context
import android.os.Parcelable
import android.preference.PreferenceManager
import android.util.Log
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import com.example.mediatimerjp.R
import com.google.firebase.database.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
class TimerCommon(var type: String, var name: String) : Parcelable {
    var timer: Timer? = null
    var group: TimerGroup? = null
    var groupAssociated: TimerCommon? = null
    var timerColors = TimerColors()
    var active = true
    var selecting = false
    var observableName = MutableLiveData<String>()
    var biggest = MutableLiveData<String>()
    var medium = MutableLiveData<String>()
    var smallest = MutableLiveData<String>()
    var indicator = MutableLiveData<String>()
    var favourite = false
    var id = ""

    @Exclude
    lateinit var bigTextView: TextView

    @Exclude
    lateinit var mediumTextView: TextView

    @Exclude
    lateinit var smallTextView: TextView

    @Exclude
    lateinit var nameTextView: TextView

    @Exclude
    lateinit var progressBar: ProgressBar

    @Exclude
    lateinit var context: Context

    @Exclude
    lateinit var playPause: ImageButton

    @Exclude
    lateinit var playPauseFullScreen: ImageButton

    @Exclude
    lateinit var menu: ImageButton

    init {
        indicator.value = "HH/MM/SS"
        if (type == "timer") {
            timer = Timer(90000, 100)

        } else if (type == "group") {
            group = TimerGroup(0)
            group!!.progress.value = 100

        }
        observableName.value = name


    }


    fun restartTimer() {
        if (type == "timer") {
            timer!!.restart(this)
            if (::progressBar.isInitialized)
                progressBar.progress = 100
        } else {
            group!!.restart()
            if (::progressBar.isInitialized)
                progressBar.progress = 100
            group!!.currentRunning = 0
        }
    }

    fun doAction() {
        if (type == "timer") {
            timer!!.action(this, context)
        } else {
            group!!.action()
            group!!.running = !group!!.running
            if(group!!.running) alterButton(1)
            else alterButton(0)
        }
    }

    fun updateView(type: String, restart: Boolean) {
        if (type == "timer") {
            if (restart) {
                if (::bigTextView.isInitialized) {
                    bigTextView.text = TimerWrapper.getInstance().getValueFromMillis(
                        timer!!.initialTimerValue,
                        timer!!.mode
                    )["Biggest"].toString()
                    mediumTextView.text = TimerWrapper.getInstance().getValueFromMillis(
                        timer!!.initialTimerValue,
                        timer!!.mode
                    )["Medium"].toString()
                    smallTextView.text = TimerWrapper.getInstance()
                        .getValueFromMillis(
                            timer!!.initialTimerValue,
                            timer!!.mode
                        )["Small"].toString()

                    biggest.value = (TimerWrapper.getInstance()
                        .getValueFromMillis(
                            timer!!.initialTimerValue,
                            timer!!.mode
                        )["Biggest"].toString())
                    medium.value = (TimerWrapper.getInstance()
                        .getValueFromMillis(
                            timer!!.initialTimerValue,
                            timer!!.mode
                        )["Medium"].toString())
                    smallest.value = (TimerWrapper.getInstance()
                        .getValueFromMillis(
                            timer!!.initialTimerValue,
                            timer!!.mode
                        )["Small"].toString())
                    progressBar.progress = timer!!.progress.value!!
                }
            } else {
                if (::bigTextView.isInitialized) {
                    bigTextView.text = TimerWrapper.getInstance().getValueFromMillis(
                        timer!!.currentTimerValue,
                        timer!!.mode
                    )["Biggest"].toString()
                    mediumTextView.text = TimerWrapper.getInstance().getValueFromMillis(
                        timer!!.currentTimerValue,
                        timer!!.mode
                    )["Medium"].toString()
                    smallTextView.text = TimerWrapper.getInstance()
                        .getValueFromMillis(
                            timer!!.currentTimerValue,
                            timer!!.mode
                        )["Small"].toString()

                    biggest.value = (TimerWrapper.getInstance()
                        .getValueFromMillis(
                            timer!!.currentTimerValue,
                            timer!!.mode
                        )["Biggest"].toString())
                    medium.value = (TimerWrapper.getInstance()
                        .getValueFromMillis(
                            timer!!.currentTimerValue,
                            timer!!.mode
                        )["Medium"].toString())
                    smallest.value = (TimerWrapper.getInstance()
                        .getValueFromMillis(
                            timer!!.currentTimerValue,
                            timer!!.mode
                        )["Small"].toString())
                    progressBar.progress = timer!!.progress.value!!
                }
            }


        } else if (type == "group") {
            val remainding = getRemainderTime()
            if (::bigTextView.isInitialized) {
                biggest.value = TimerWrapper.getInstance()
                    .getValueFromMillis(remainding, group!!.mode)["Biggest"].toString()
                medium.value = TimerWrapper.getInstance()
                    .getValueFromMillis(remainding, group!!.mode)["Medium"].toString()
                smallest.value = TimerWrapper.getInstance()
                    .getValueFromMillis(remainding, group!!.mode)["Small"].toString()
                bigTextView.text = TimerWrapper.getInstance().getValueFromMillis(
                    remainding,
                    group!!.mode
                )["Biggest"].toString()
                mediumTextView.text = TimerWrapper.getInstance().getValueFromMillis(
                    remainding,
                    group!!.mode
                )["Medium"].toString()
                smallTextView.text = TimerWrapper.getInstance()
                    .getValueFromMillis(remainding, group!!.mode)["Small"].toString()
                getProgressTime()
                progressBar.progress = group!!.progress.value!!

            }
        }
        if (groupAssociated != null) {
            groupAssociated!!.updateView("group", true)
        }
    }

    fun changeFavourite() {
        favourite = !favourite
        if (type == "timer") {
            timer!!.favorite = favourite
        } else if (type == "group") {
            group!!.favorite = favourite
        }
    }

    fun getRemainderTime(): Long {
        var remainder = 0L
        if (type == "timer") {
            remainder = timer!!.currentTimerValue

        } else if (type == "group") {
            for (timer in group!!.timersAssociated) {
                remainder += timer.getRemainderTime()
            }

        }
        return remainder
    }

    fun getInitialTime(): Long {
        var remainder = 0L
        if (type == "timer") {
            remainder = timer!!.initialTimerForProgressBar

        } else if (type == "group") {
            for (timer in group!!.timersAssociated) {
                remainder += timer.getInitialTime()
            }

        }
        return remainder
    }

    fun getProgressTime() {
        group?.progress?.value = (100.0 * getRemainderTime() / getInitialTime()).toInt()
    }


    fun setContexts(context: Context) {
        this.context = context
        if (group != null) {
            for (timer in group!!.timersAssociated) {
                timer.setContexts(context)
            }
        }
    }

    fun alterButton(state: Int) {
        when (state) {
            0 -> {
                if (::playPause.isInitialized) {
                    playPause.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                    if (this::playPauseFullScreen.isInitialized) playPauseFullScreen.setImageResource(
                        R.drawable.ic_baseline_play_arrow_24
                    )
                }
            }
            1 -> {
                if (::playPause.isInitialized) {
                    playPause.setImageResource(R.drawable.ic_baseline_pause_24)
                    if (this::playPauseFullScreen.isInitialized) playPauseFullScreen.setImageResource(
                        R.drawable.ic_baseline_pause_24
                    )
                }
            }
            2 -> {
                if (::playPause.isInitialized) {
                    playPause.setImageResource(R.drawable.ic_baseline_stop_24)
                    if (this::playPauseFullScreen.isInitialized) playPauseFullScreen.setImageResource(
                        R.drawable.ic_baseline_stop_24
                    )
                }
            }
        }
    }

    fun setColors(context: Context) {
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        val appColor = sharedPreferences.getInt("currentTheme", 0)
        if (timerColors.favouriteColor == -1 ||
            timerColors.favouriteColor == context.resources.getColor(R.color.primaryColorBlueGrey) ||
            timerColors.favouriteColor == context.resources.getColor(R.color.primaryColorOrange) ||
            timerColors.favouriteColor == context.resources.getColor(R.color.primaryColorGreen) ||
            timerColors.favouriteColor == context.resources.getColor(R.color.primaryColorRedBlack) ||
            timerColors.favouriteColor == context.resources.getColor(R.color.primaryColorBlack)
        ) {
            if (appColor == 0) {
                timerColors.favouriteColor =
                    context.resources.getColor(R.color.primaryColorBlueGrey)
            } else if (appColor == 1) {
                timerColors.favouriteColor = context.resources.getColor(R.color.primaryColorOrange)
            } else if (appColor == 2) {
                timerColors.favouriteColor = context.resources.getColor(R.color.primaryColorGreen)
            } else if (appColor == 3) {
                timerColors.favouriteColor =
                    context.resources.getColor(R.color.primaryColorRedBlack)
            } else if (appColor == 4) {
                timerColors.favouriteColor = context.resources.getColor(R.color.primaryColorBlack)
            }
        }
        if (timerColors.backgroundColor == -1 ||
            timerColors.backgroundColor == context.resources.getColor(R.color.onPrimaryColorBlack) ||
            timerColors.backgroundColor == context.resources.getColor(R.color.onPrimaryColorBlack) ||
            timerColors.backgroundColor == context.resources.getColor(R.color.onPrimaryColorBlack) ||
            timerColors.backgroundColor == context.resources.getColor(R.color.onPrimaryColorBlack) ||
            timerColors.backgroundColor == context.resources.getColor(R.color.onPrimaryColorBlack)
        ) {
            if (appColor == 0) {
                timerColors.backgroundColor =
                    context.resources.getColor(R.color.onPrimaryColorBlack)
            } else if (appColor == 1) {
                timerColors.backgroundColor =
                    context.resources.getColor(R.color.onPrimaryColorBlack)
            } else if (appColor == 2) {
                timerColors.backgroundColor =
                    context.resources.getColor(R.color.onPrimaryColorBlack)
            } else if (appColor == 3) {
                timerColors.backgroundColor =
                    context.resources.getColor(R.color.onPrimaryColorBlack)
            } else if (appColor == 4) {
                timerColors.backgroundColor =
                    context.resources.getColor(R.color.onPrimaryColorBlack)
            }
        }
        if (timerColors.buttonBackground == -1 ||
            timerColors.buttonBackground == context.resources.getColor(R.color.primaryColorBlueGrey) ||
            timerColors.buttonBackground == context.resources.getColor(R.color.primaryColorOrange) ||
            timerColors.buttonBackground == context.resources.getColor(R.color.primaryColorGreen) ||
            timerColors.buttonBackground == context.resources.getColor(R.color.primaryColorRedBlack) ||
            timerColors.buttonBackground == context.resources.getColor(R.color.primaryColorBlack)
        ) {
            if (appColor == 0) {
                timerColors.buttonBackground =
                    context.resources.getColor(R.color.primaryColorBlueGrey)
            } else if (appColor == 1) {
                timerColors.buttonBackground =
                    context.resources.getColor(R.color.primaryColorOrange)
            } else if (appColor == 2) {
                timerColors.buttonBackground = context.resources.getColor(R.color.primaryColorGreen)
            } else if (appColor == 3) {
                timerColors.buttonBackground =
                    context.resources.getColor(R.color.primaryColorRedBlack)
            } else if (appColor == 4) {
                timerColors.buttonBackground = context.resources.getColor(R.color.primaryColorBlack)
            }
        }
        if (timerColors.buttonIconColor == -1 ||
            timerColors.buttonIconColor == context.resources.getColor(R.color.primaryColorBlueGrey) ||
            timerColors.buttonIconColor == context.resources.getColor(R.color.primaryColorOrange) ||
            timerColors.buttonIconColor == context.resources.getColor(R.color.primaryColorGreen) ||
            timerColors.buttonIconColor == context.resources.getColor(R.color.primaryColorRedBlack) ||
            timerColors.buttonIconColor == context.resources.getColor(R.color.primaryColorBlack)
        ) {
            if (appColor == 0) {
                timerColors.buttonIconColor =
                    context.resources.getColor(R.color.onPrimaryColorBlack)
            } else if (appColor == 1) {
                timerColors.buttonIconColor =
                    context.resources.getColor(R.color.onPrimaryColorBlack)
            } else if (appColor == 2) {
                timerColors.buttonIconColor =
                    context.resources.getColor(R.color.onPrimaryColorBlack)
            } else if (appColor == 3) {
                timerColors.buttonIconColor =
                    context.resources.getColor(R.color.onPrimaryColorBlack)
            } else if (appColor == 4) {
                timerColors.buttonIconColor =
                    context.resources.getColor(R.color.onPrimaryColorBlack)
            }
        }
        if (timerColors.textColor == -1 ||
            timerColors.textColor == context.resources.getColor(R.color.primaryColorBlueGrey) ||
            timerColors.textColor == context.resources.getColor(R.color.primaryColorOrange) ||
            timerColors.textColor == context.resources.getColor(R.color.primaryColorGreen) ||
            timerColors.textColor == context.resources.getColor(R.color.primaryColorRedBlack) ||
            timerColors.textColor == context.resources.getColor(R.color.primaryColorBlack)
        ) {
            if (appColor == 0) {
                timerColors.textColor = context.resources.getColor(R.color.primaryColorBlueGrey)
            } else if (appColor == 1) {
                timerColors.textColor = context.resources.getColor(R.color.primaryColorOrange)
            } else if (appColor == 2) {
                timerColors.textColor = context.resources.getColor(R.color.primaryColorGreen)
            } else if (appColor == 3) {
                timerColors.textColor = context.resources.getColor(R.color.primaryColorRedBlack)
            } else if (appColor == 4) {
                timerColors.textColor = context.resources.getColor(R.color.primaryColorBlack)
            }
        }
    }

}

