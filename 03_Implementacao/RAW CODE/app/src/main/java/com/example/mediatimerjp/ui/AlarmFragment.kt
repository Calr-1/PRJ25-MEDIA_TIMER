package com.example.mediatimerjp.ui

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.SeekBar
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.mediatimerjp.database.Database
import com.example.mediatimerjp.databinding.FragmentAlarmBinding
import com.example.mediatimerjp.model.TimerCommon
import com.example.mediatimerjp.model.TimerWrapper
import java.io.IOException


class AlarmFragment : Fragment() {

    private lateinit var wrapper: TimerWrapper

    private lateinit var binding: FragmentAlarmBinding

    private lateinit var timer: TimerCommon


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        wrapper = TimerWrapper.getInstance()
        binding = FragmentAlarmBinding.inflate(inflater, container, false)
        return binding.root

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timer = arguments?.getParcelable("timer")!!

        binding.chooseSoundText.setOnClickListener { v: View? ->
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone")
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, null as Uri?)
            this.startActivityForResult(intent, 5)
        }

        val mainVolumePicker = binding.mainSoundVolumeBar
        mainVolumePicker.progress = timer.timer!!.mainSoundVolume
        binding.mainSoundVolume.text = timer.timer!!.mainSoundVolume.toString()
        mainVolumePicker.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                timer.timer!!.mainSoundVolume = progress
                binding.mainSoundVolume.text = progress.toString()
            }
        })

        val spinner = binding.alarmConfigSpinner
        var typeOfAlarm = timer.timer!!.typeOfAlarm
        spinner.setSelection(getIndex(spinner, typeOfAlarm))
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                typeOfAlarm = parent.getItemAtPosition(position).toString()
                timer.timer!!.typeOfAlarm = typeOfAlarm
            }
            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        binding.inputAlarmRepetitions.setText(timer.timer!!.alarmRepetitions.toString())

        val time = timer.timer!!.alarmTime
        wrapper.updateViews(time, timer.timer!!.mode, binding.secondsEditAlarmTime, binding.minutesEditAlarmTime, binding.hoursEditAlarmTime)

    }

    private fun getIndex(spinner: Spinner, myString: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString().equals(myString, ignoreCase = true)) {
                return i
            }
        }
        return 0
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_CANCELED) {
            if (resultCode == Activity.RESULT_OK && requestCode == 5) {
                val uri = data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                if (uri != null) {
                    context?.let { Database.uploadSound(uri, it, timer.timer!!) }
                    try {
                        timer.timer?.alarmUri  = uri
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        //saving the interval details
        timer.timer!!.alarmTime = wrapper.getTimerValue(
            "HH/MM/SS",
            binding.secondsEditAlarmTime,
            binding.minutesEditAlarmTime,
            binding.hoursEditAlarmTime
        )
        timer.timer!!.alarmRepetitions = binding.inputAlarmRepetitions.text.toString().toInt()
    }
}