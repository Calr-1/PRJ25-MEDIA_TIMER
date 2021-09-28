package com.example.mediatimerjp.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.mediatimerjp.database.Database
import com.example.mediatimerjp.model.TimerCommon
import com.example.mediatimerjp.model.TimerToSave
import com.example.mediatimerjp.model.TimerWrapper
import kotlin.coroutines.coroutineContext

class MainViewModel(
    private val savedStateHandle: SavedStateHandle?,
) : ViewModel() {

    var timerList = MutableLiveData<ArrayList<TimerCommon>>()
    var listOfTimers = ArrayList<TimerCommon>()
    private var backupList = ArrayList<TimerCommon>()
    private var fbackupList = ArrayList<TimerCommon>()
    var wrapper = TimerWrapper.getInstance()

    var timerCount = 0
    var groupCount = 0

    var ids = ArrayList<String>()
    var timerAdapter: TimerAdapter? = null


    fun getTimers(context: Context) {
        addTimer("timer",context)
        addTimer("timer",context)
        addTimer("timer",context)
    }



    fun addTimer(type : String, context: Context) {
        if(type == "timer"){
            timerCount++
            val timer  = TimerCommon("timer","Timer $timerCount")
            timer.context = context
            timer.timer?.context = context
            timer.timer!!.name = timer.name
            timer.setColors(context)
            timer.timer?.timerColors = timer.timerColors
            timer.timer!!.setId()
            listOfTimers.add(timer)
            timerList.value = listOfTimers
        }
        else if (type == "group"){
            groupCount++
            val timer  = TimerCommon("group","Group $groupCount")
            timer.context = context
            timer.timer?.context = context
            timer.group!!.name = timer.name
            timer.group?.itself = timer
            timer.setColors(context)
            timer.group?.timerColors = timer.timerColors
            listOfTimers.add(timer)
            timerList.value = listOfTimers
        }
        TimerWrapper.getInstance().saveTimersToSharedPreference(context, "SaveFile")
    }

    fun addTimer(timer:TimerCommon){
        if (timer.type=="timer"){
            timerCount++
        }
        else{
            groupCount++
        }
        listOfTimers.add(timer)
        timerList.value = listOfTimers
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

    fun swapTimers(first: Int, second: Int) {
        listOfTimers[first].name.let { Log.e("drag", it) }
        listOfTimers[second].name.let { Log.e("drag", it) }
        val timer = listOfTimers[first]
        listOfTimers.removeAt(first)
        listOfTimers.add(second, timer)
        timerList.value = listOfTimers
    }

    fun getTimersToSave(): ArrayList<TimerToSave> {
        val array = ArrayList<TimerToSave>()
        listOfTimers.forEach {
            if (it.type == "timer") {
                it.timer?.let { it1 -> array.add(it1.getTimerToSave()) }
            } else {
                it.group?.let { it1 -> array.add(it1.getGroupToSave(it)) }
            }
        }
        return array
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setSavedTimers(array: ArrayList<TimerToSave>?) {
        listOfTimers.clear()
        timerCount = 0
        groupCount = 0
        array?.forEach {
            if (it.type == "timer") {
                timerCount++
                val timer = TimerCommon(it.type!!, it.name!!)
                timer.timer!!.context = wrapper.context
                timer.timer?.setTimer(it)
                timer.indicator.value = timer.timer!!.mode
                timer.id = timer.timer!!.id
                timer.name = timer.timer!!.name
                timer.timerColors = timer.timer!!.timerColors
                timer.favourite = timer.timer!!.favorite
                listOfTimers.add(timer)
            } else {
                groupCount++
                val timer = TimerCommon(it.type!!, it.name!!)
                timer.group?.setGroup(it, timer)
                timer.id = timer.group!!.id
                timer.name = timer.group!!.name
                timer.group?.itself = timer
                timer.timerColors = timer.group!!.timerColors
                timer.favourite = timer.group!!.favorite
                listOfTimers.add(timer)
            }
        }
        timerList.value = listOfTimers
    }

    fun setUploadTimer(timerToSave: TimerToSave) {
        if (timerToSave.type == "timer") {
            timerCount++
            val timer = TimerCommon(timerToSave.type!!, timerToSave.name!!)
            timer.timer?.setTimer(timerToSave)
            timer.id = timer.timer!!.id
            timer.name = timer.timer!!.name
            timer.groupAssociated = timer.timer?.groupAssociated
            timer.timerColors = timer.timer!!.timerColors
            listOfTimers.add(0, timer)
        } else {
            groupCount++
            val timer = TimerCommon(timerToSave.type!!, timerToSave.name!!)
            timer.group!!.setGroup(timerToSave, timer)
            timer.id = timer.group!!.id
            timer.groupAssociated = timer.group?.groupAssociated
            timer.group!!.itself = timer
            timer.name = timer.group!!.name
            timer.timerColors = timer.group!!.timerColors
            listOfTimers.add(0, timer)
        }
        timerList.value = listOfTimers
    }

    fun setTimers(array: ArrayList<TimerCommon>) {
        listOfTimers = array
        timerList.value = listOfTimers
        for (timer in array) {
            if (timer.type == "timer") timerCount++
            else groupCount++
        }
    }

    fun shareTimer(timer: TimerCommon) {
        val context = timer.context
        var id = ""
        id = if (timer.type == "timer") timer.timer!!.id
        else timer.group!!.id
        val shareIntent = Intent().apply {
            this.action = Intent.ACTION_SEND
            this.putExtra(Intent.EXTRA_TEXT, id)
            this.putExtra(Intent.EXTRA_TITLE, "Timer code")
            this.type = "text/plain"
            this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(shareIntent)

    }

    fun addId(id: String?) {
        if (!ids.contains(id)) id?.let { ids.add(it) }
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