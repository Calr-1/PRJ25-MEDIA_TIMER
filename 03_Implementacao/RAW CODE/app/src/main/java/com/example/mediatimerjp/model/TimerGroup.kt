package com.example.mediatimerjp.model

import android.util.Log
import com.example.mediatimerjp.database.Database


class TimerGroup(var actualTime: Long) {

    var mode = "HH/MM/SS"
    var sequence = true
    private var stopAtZero = false
    var skipGroups = false
    private var currentRunning = 0
    var propagateTheme = true
    var name = ""
    var id:String = ""
    lateinit var timerColors: TimerColors

    var timersAssociated: ArrayList<TimerCommon> = ArrayList()

    var groupAssociated: TimerCommon? = null

    lateinit var itself: TimerCommon

    var favorite = false


    fun restart() {
        for (timer in timersAssociated) {
            timer.restartTimer()
        }
    }

    fun setId(){
        if (id == "") id = Database.getKey(id)
    }

    fun action(timerCommon: TimerCommon? = null) {
        Log.e("Current", currentRunning.toString())

        if(timerCommon==null){
            for(i in currentRunning until timersAssociated.size){
                if(!(timersAssociated[i].type=="group" && skipGroups) && timersAssociated[i].active){
                    if(timersAssociated[i].getRemainderTime()>0) {
                        timersAssociated[i].doAction()
                        break
                    }
                }
            }
        }

        else{
            if(currentRunning<timersAssociated.size-1){
                var idx = 0
                for (timer in timersAssociated){
                    if (timer==timerCommon){
                        idx = timersAssociated.indexOf(timer)
                        break
                    }
                    else if (timer.id==timerCommon.id){
                        idx = timersAssociated.indexOf(timer)
                        break
                    }
                }
                idx += 1
                currentRunning=idx
                if(timersAssociated[currentRunning].active) {
                    if (stopAtZero) {
                        if (timerCommon.groupAssociated != null) {
                            timerCommon.groupAssociated!!.doAction()
                        }
                    } else {
                        if (timersAssociated[currentRunning].type == "group" && skipGroups) {
                            this.action(timersAssociated[currentRunning])
                        } else {
                            timersAssociated[currentRunning].doAction()
                        }
                    }
                }
                else{
                    this.action(timersAssociated[currentRunning])
                }
            }
            else {
                if(timerCommon.groupAssociated!=null){
                    timerCommon.groupAssociated!!.doAction()
                }
                currentRunning=0
            }
        }
    }

    fun getGroupToSave(group: TimerCommon): TimerToSave {
        val arrayTimers = ArrayList<TimerToSave>()
        group.group!!.timersAssociated.forEach {
            if (it.type == "timer") it.timer!!.let { it1 -> arrayTimers.add(it1.getTimerToSave()) }
            else {
                it.group!!.let { it1 -> arrayTimers.add(it1.getGroupToSave(it)) }
            }
        }
        if (group.group!!.id=="") group.group!!.id = Database.getKey("")
        val groupToSave = TimerToSave(
            group.type,
            group.name,
            null,
            null,
            group.group!!.mode,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            arrayTimers,
            group.group!!.sequence,
            group.group!!.stopAtZero,
            group.group!!.skipGroups,
            group.group!!.currentRunning,
            group.timerColors,
            group.group!!.id,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            favorite,
            null
        )
        Database.saveTimer(groupToSave)
        return groupToSave
    }

    fun setGroup(groupToSave: TimerToSave, groupAssociated: TimerCommon?) {
        mode = groupToSave.mode!!
        sequence = groupToSave.seq!!
        stopAtZero = groupToSave.stopAtZero!!
        skipGroups = groupToSave.skipGroups!!
        currentRunning = groupToSave.currentRunning!!
        name = groupToSave.name!!
        timerColors = groupToSave.timerColors!!
        if (groupAssociated != null) {
            groupAssociated.timerColors = groupToSave.timerColors!!
        }
        id = groupToSave.id!!
        timersAssociated.clear()
        groupToSave.timers?.forEach {
            if (it.type == "timer") {
                val timer = TimerCommon("timer", it.name!!)
                timer.timer!!.setTimer(it)
                timer.id = timer.timer!!.id
                timer.timer!!.groupAssociated = groupAssociated
                timer.groupAssociated = groupAssociated
                timer.favourite = timer.timer!!.favorite
                timersAssociated.add(timer)
            } else {
                val timer = TimerCommon("group", it.name!!)
                timer.group!!.itself = timer
                timer.group!!.groupAssociated = groupAssociated
                timer.groupAssociated = groupAssociated
                timer.group!!.setGroup(it, groupAssociated)
                timer.id = timer.group!!.id
                timer.favourite = timer.group!!.favorite
                timersAssociated.add(timer)
            }
        }
    }

    fun addTimer(timer:TimerCommon): Boolean{
        if(!timersAssociated.contains(timer)){
            timersAssociated.forEach {
                if (it.id == timer.id) return false
            }
            timersAssociated.add(timer)
            return true
        }
        return false
    }

    fun removeTimer(timer:TimerCommon){
        timersAssociated.remove(timer)
    }

    fun changeSequence() {
        sequence = !sequence
    }
}
