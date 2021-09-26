package com.example.mediatimerjp.model

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.preference.PreferenceManager
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import com.example.mediatimerjp.R
import com.example.mediatimerjp.database.Database
import com.example.mediatimerjp.ui.GroupViewModel
import com.example.mediatimerjp.ui.MainViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.ArrayList


class TimerWrapper {

    lateinit var currentModel: MainViewModel
    lateinit var currentGroup: GroupViewModel
    lateinit var context: Context
    var selectedTimer: TimerCommon? = null


    var isOnMain = true
    var mainFavSelected = false

    fun getValueFromMillis(millis: Long, mode: String): HashMap<String, Long> {
        val timerInHoursMinutesSeconds = HashMap<String, Long>()
        if (mode == "HH/MM/SS") {
            val seconds = (millis / 1000) % 60
            val minutes = (millis / (1000 * 60) % 60)
            val hours = (millis / (1000 * 60 * 60) % 24)
            val days = (millis / (1000 * 60 * 60 * 24))
            timerInHoursMinutesSeconds["Biggest"] = hours + days * 24
            timerInHoursMinutesSeconds["Medium"] = minutes
            timerInHoursMinutesSeconds["Small"] = seconds
        } else if (mode == "DD/HH/MM") {
            val minutes = (millis / (1000 * 60) % 60)
            val hours = (millis / (1000 * 60 * 60) % 24)
            val days = (millis / (1000 * 60 * 60 * 24))
            timerInHoursMinutesSeconds["Biggest"] = days
            timerInHoursMinutesSeconds["Medium"] = hours
            timerInHoursMinutesSeconds["Small"] = minutes
        }
        return timerInHoursMinutesSeconds
    }

    fun getTimerValue(mode: String, small: TextView, medium: TextView, big: TextView): Long {
        var seconds: Long = 0
        var minutes: Long = 0
        var hours: Long = 0
        if (small.text.toString() != "") {
            seconds = small.text.toString().toLong()
        }
        if (medium.text.toString() != "") {
            minutes = medium.text.toString().toLong()
        }
        if (big.text.toString() != "") {
            hours = big.text.toString().toLong()
        }
        var value: Long = 0
        value =
            if (mode == "HH/MM/SS") (hours * (60 * 60) + minutes * 60 + seconds) * 1000 else (hours * (60 * 60 * 24) + minutes * (60 * 60) + seconds * 60) * 1000
        return value
    }

    fun updateViews(time: Long, mode: String, small: TextView, medium: TextView, big: TextView) {
        val timeLeft: HashMap<String, Long> = getValueFromMillis(time, mode)
        big.text = timeLeft["Biggest"].toString()
        medium.text = timeLeft["Medium"].toString()
        small.text = timeLeft["Small"].toString()
    }

    fun updateProgressiveViews(time: Long, mode: String, small: EditText, medium: EditText, big: EditText) {
        val timeLeft: HashMap<String, Long> = getValueFromMillis(time, mode)
        big.setText(timeLeft["Biggest"].toString())
        medium.setText(timeLeft["Medium"].toString())
        small.setText(timeLeft["Small"].toString())
    }

    fun getTimersFromPreference(
        context: Context,
        preferenceFileName: String?
    ): ArrayList<TimerToSave>? {
        val array = ArrayList<TimerToSave>()
        val sharedPreferences = context.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE)
        if (sharedPreferences.contains("timers")) {
            Log.e("save" , "got-it")
            val gson = Gson()
            val collectionType = object : TypeToken<Collection<String>>() {}.type
            val ids = gson.fromJson<ArrayList<String>>(
                sharedPreferences.getString(
                    "timers",
                    ""
                ), collectionType
            )
            if(ids.size>0) {
                ids.forEach {
                    val collectionType = object : TypeToken<TimerToSave>() {}.type
                    val timer = gson.fromJson<TimerToSave>(
                        sharedPreferences.getString(
                            it,
                            ""
                        ), collectionType
                    )
                    array.add(timer)
                }
                return array
            }
        }
        return null
    }

    fun saveTimersToSharedPreference(
        context: Context,
        preferenceFileName: String?,
    ) {
        val sharedPreferences = context.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE)
        val sharedPreferencesEditor = sharedPreferences.edit()
        val gson = Gson()
        val timers = currentModel.getTimersToSave()
        timers.forEach {

            currentModel.addId(it.id)
        }
        val serializedStrings = gson.toJson(timers)
        sharedPreferencesEditor.putString("timers", serializedStrings)
        sharedPreferencesEditor.apply()
        val user = Database.getUser()
        user.timers = currentModel.ids
        Database.setUser(user)
    }

    fun saveTimersToSharedPreference(
        context: Context,
        preferenceFileName: String?,
        timers:ArrayList<TimerToSave>
    ) {
        val sharedPreferences = context.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE)
        val sharedPreferencesEditor = sharedPreferences.edit()
        val gson = Gson()
        val serializedStrings = gson.toJson(timers)
        sharedPreferencesEditor.putString("timers", serializedStrings)
        sharedPreferencesEditor.apply()
        Log.e("save after service","savingz")
    }

    fun saveObjectToSharedPreference(
        context: Context,
        preferenceFileName: String?,
        serializedObjectKey: String?,
        item: Any?
    ) {
        val sharedPreferences = context.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE)
        val sharedPreferencesEditor = sharedPreferences.edit()
        val gson = Gson()
        val serializedObject = gson.toJson(item)
        sharedPreferencesEditor.putString(serializedObjectKey, serializedObject)
        sharedPreferencesEditor.apply()
        Log.e("save" , "saved")
    }

    fun getSavedObjectFromPreference(
        context: Context,
        preferenceFileName: String?,
        preferenceKey: String?
    ): ArrayList<TimerToSave>? {
        val sharedPreferences = context.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE)
        if (sharedPreferences.contains(preferenceKey)) {
            Log.e("save" , "got-it")
            val gson = Gson()
            val collectionType = object : TypeToken<Collection<TimerToSave>>() {}.type
            return gson.fromJson<ArrayList<TimerToSave>>(
                sharedPreferences.getString(
                    preferenceKey,
                    ""
                ), collectionType
            )
        }
        return null
    }
    fun loadTheme(app: Context) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app)
        val appColor = sharedPreferences.getInt("currentTheme", 0)
        Log.d("APP THEME", appColor.toString())
        if (appColor == 0) {
            app.setTheme(R.style.Theme_MediaTimerJP)
        } else if (appColor == 1) {
            app.setTheme(R.style.Theme_ORANGE_THEME)
        } else if (appColor == 2) {
            app.setTheme(R.style.Theme_GREEN_THEME)
        } else if (appColor == 3) {
            app.setTheme(R.style.Theme_REDBLACK_THEME)
        } else if (appColor == 4) {
            app.setTheme(R.style.Theme_BLACK_THEME)
        }
    }

    fun isImageFile(uri: Uri): Boolean {
        val contentResolver = context.contentResolver
        val type = contentResolver.getType(uri)
        if (type != null) {
            return type.startsWith("image/")
        }
        return false
    }

    fun isVideoFile(uri: Uri): Boolean {
        val contentResolver = context.contentResolver
        val type = contentResolver.getType(uri)
        if (type != null) {
            return type.startsWith("video/")
        }
        return false
    }

    fun millisecondsToMinutesAndSeconds(milliseconds: Int):String{

        val minutes = milliseconds / 1000 / 60
        val seconds = milliseconds / 1000 % 60

        return "$minutes:$seconds"
    }

    fun millisecondsToHoursMinutesAndSeconds(milliseconds: Int):String{

        val hours = milliseconds / 1000 / 60 / 24
        val minutes = milliseconds / 1000 / 60
        val seconds = milliseconds / 1000 % 60

        return "$hours:$minutes:$seconds"
    }
    fun getThemeID(app: Context): Int {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app)
        return sharedPreferences.getInt("currentTheme", 0)
    }

    @Throws(Throwable::class)
    fun retriveVideoFrameFromVideo(videoPath: String?): Bitmap? {
        var bitmap: Bitmap? = null
        var mediaMetadataRetriever: MediaMetadataRetriever? = null
        try {
            mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(
                videoPath,
                HashMap()
            )
            //   mediaMetadataRetriever.setDataSource(videoPath);
            bitmap = mediaMetadataRetriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST)
        } catch (e: Exception) {
            e.printStackTrace()
            throw Throwable("Exception getting video thumbnail" + e.message)
        } finally {
            mediaMetadataRetriever?.release()
        }
        return bitmap
    }


    companion object {
        private var obj: TimerWrapper? = null
        fun getInstance(): TimerWrapper {
            if (obj == null) {
                obj = TimerWrapper()
            }
            return obj as TimerWrapper
        }
    }
}