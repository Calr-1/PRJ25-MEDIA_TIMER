package com.example.mediatimerjp.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.mediatimerjp.database.Database
import com.example.mediatimerjp.model.*

class GroupViewModel constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var group: TimerCommon? = null

    var timerList = MutableLiveData<ArrayList<TimerCommon>>()
    var listOfTimers = ArrayList<TimerCommon>()
    private var backupList = ArrayList<TimerCommon>()
    private var fbackupList = ArrayList<TimerCommon>()


    private var timerCount = 0
    private var groupCount = 0

    var timerAdapter: TimerAdapter? = null

    fun getTimers(context: Context) {

        addTimer("timer",context)
        addTimer("timer",context)
        addTimer("timer",context)


    }

    fun addTimer(type: String, context: Context) {
        if (type == "timer") {
            timerCount++
            val timer = TimerCommon("timer", "Timer $timerCount")
            timer.context = context
            timer.groupAssociated = group
            timer.timer!!.groupAssociated = group
            timer.setColors(context)
            if(group!!.group!!.propagateTheme) setColors(timer.timerColors, group!!.group!!.timerColors)
            timer.timer!!.timerColors = timer.timerColors
            timer.timer!!.name = timer.name
            timer.timer!!.setId()
            listOfTimers.add(timer)
            timerList.value = listOfTimers
        } else if (type == "group") {
            groupCount++
            val timer = TimerCommon("group", "Group $groupCount")
            timer.context = context
            timer.group?.itself = timer
            timer.groupAssociated = group
            timer.group!!.groupAssociated = group
            timer.setColors(context)
            if(group!!.group!!.propagateTheme) setColors(timer.timerColors, group!!.group!!.timerColors)
            timer.group!!.timerColors = timer.timerColors
            timer.group!!.name = timer.name
            listOfTimers.add(timer)
            timerList.value = listOfTimers
        }
        group?.group?.timersAssociated = timerList.value!!
        TimerWrapper.getInstance().saveTimersToSharedPreference(context, "SaveFile")

    }
    private fun setColors(currentColors : TimerColors, colors : TimerColors){
        currentColors.backgroundColor = colors.backgroundColor
        currentColors.buttonBackground = colors.buttonBackground
        currentColors.textColor = colors.textColor
        currentColors.buttonIconColor = colors.buttonIconColor
        currentColors.favouriteColor = colors.favouriteColor

    }

    fun swapTimers(first: Int, second: Int) {
        listOfTimers[first].name.let { Log.e("drag", it) }
        listOfTimers[second].name.let { Log.e("drag", it) }
        val timer = listOfTimers[first]
        listOfTimers.removeAt(first)
        listOfTimers.add(second, timer)
        timerList.value = listOfTimers
        group?.group?.timersAssociated = timerList.value!!
    }


    fun setLists() {
        listOfTimers = group?.group?.timersAssociated!!
        timerList.value = listOfTimers
        var timerTemp = 0
        var groupTemp = 0

        for(time in listOfTimers){
            if(time.type=="timer"){
                timerTemp++
            }
            if(time.type=="group"){
                groupTemp++
            }
        }
        timerCount = timerTemp
        groupCount = groupTemp

    }

    fun removeTimer(timer: TimerCommon) {
        val user = Database.getUser()
        user.timers.remove(timer.id)
        Database.setUser(user)
        listOfTimers.remove(timer)
        timerList.value = listOfTimers
        Database.removeTimer(timer.id)
        if (timer.type=="timer"){
            timerCount--
        }
        else{
            groupCount--
        }
    }

    fun filterGroups(){
        val groups = ArrayList<TimerCommon>()
        backupList = timerList.value!!

        for(timer in backupList){
            if(timer.type=="group"){
                timer.selecting = true
                groups.add(timer)
            }
        }

        timerList.value = groups
        timerAdapter?.notifyDataSetChanged()
    }

    fun resetAdapter(){
        for(timer in backupList){
            if(timer.type=="group"){
                timer.selecting = false
            }
        }
        timerList.value = backupList
        TimerWrapper.getInstance().selectedTimer = null
        timerAdapter?.notifyDataSetChanged()

    }

    fun listFavorites(){
        val groups = ArrayList<TimerCommon>()
        fbackupList = timerList.value!!

        for(timer in fbackupList){

            if(timer.favourite){
                groups.add(timer)
            }
        }

        timerList.value = groups
        timerAdapter?.notifyDataSetChanged()
    }

    fun resetFavourites(){

        timerList.value = fbackupList
        timerAdapter?.notifyDataSetChanged()
    }
}