package com.example.mediatimerjp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mediatimerjp.R
import com.example.mediatimerjp.databinding.FragmentNotificationsBinding
import com.example.mediatimerjp.model.Notifications
import com.example.mediatimerjp.model.TimerCommon
import com.example.mediatimerjp.model.TimerWrapper

class NotificationsFragment : Fragment() {
    private var _binding: FragmentNotificationsBinding? = null

    private lateinit var wrapper: TimerWrapper
    private lateinit var binding: FragmentNotificationsBinding
    private lateinit var timer: TimerCommon
    private lateinit var spinner: Spinner
    private lateinit var notifications:Notifications
    private lateinit var typeOfInterval:String
    private lateinit var numberOfIntervals:EditText
    private lateinit var randomMax:EditText
    private lateinit var randomMin:EditText
    private lateinit var vibration:CheckBox
    private lateinit var intervalsVolumePicker:SeekBar



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        wrapper = TimerWrapper.getInstance()
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timer = arguments?.getParcelable("timer")!!
        timer.timer!!.context = activity?.applicationContext!!
        notifications = timer.timer!!.notifications
        typeOfInterval = notifications.getTypeOfInterval()
        spinner = binding.typeOfIntervalSpinner
        spinner.setSelection(getIndex(spinner, typeOfInterval))
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                typeOfInterval = parent.getItemAtPosition(position).toString()
                notifications.setTypeOfInterval(typeOfInterval)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }


        //Displaying the saved interval details
        numberOfIntervals = binding.inputNumberIntervals
        randomMax = binding.inputNumberIntervalsMaxRandom
        randomMin = binding.inputNumberIntervalsMinRandom
        vibration = binding.enableVibrationsCB

        randomMax.setText(timer.timer!!.notifications.getRandomIntervalsMax().toString())
        randomMin.setText(timer.timer!!.notifications.getRandomIntervalsMin().toString())
        vibration.isChecked = timer.timer!!.notifications.isEnableVibration()

        val time = timer.timer!!.notifications.getIntervalTime()
        wrapper.updateViews(time, timer.timer!!.mode, binding.secondsEditInterval, binding.minutesEditInterval, binding.hoursEditInterval)

        val nIntervals: Int = timer.timer!!.notifications.getNumberOfIntervals()
        binding.inputNumberIntervals.setText(nIntervals.toString())

        binding.chooseIntervalSoundText.setOnClickListener {
            val bundle = bundleOf("timer" to timer)
            findNavController().navigate(
                R.id.action_notificationsFragment_to_intervalSoundsFragment,
                bundle
            )
        }

        intervalsVolumePicker = binding.intervalsSoundVolumeBar
        intervalsVolumePicker.progress = timer.timer!!.intervalsSoundVolume
        binding.intervalsSoundVolume.text = timer.timer!!.intervalsSoundVolume.toString()
        intervalsVolumePicker.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                timer.timer!!.intervalsSoundVolume = progress
                binding.intervalsSoundVolume.text = progress.toString()
            }
        })
    }


    private fun getIndex(spinner: Spinner, myString: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString().equals(myString, ignoreCase = true)) {
                return i
            }
        }
        return 0
    }

    override fun onStop() {
        super.onStop()
        //saving the interval details
        val time = wrapper.getTimerValue(timer.timer!!.mode, binding.secondsEditInterval, binding.minutesEditInterval, binding.hoursEditInterval)
        timer.timer!!.notifications.setIntervalTime(time)
        timer.timer!!.notifications.setNumberOfIntervals(numberOfIntervals.text.toString().toInt())
        timer.timer!!.notifications.setRandomIntervalsMax(randomMax.text.toString().toInt())
        timer.timer!!.notifications.setRandomIntervalsMin(randomMin.text.toString().toInt())
        timer.timer!!.notifications.setEnableVibration(vibration.isChecked)
        context?.let { wrapper.saveTimersToSharedPreference(it, "SaveFile") }
        _binding = null
    }

}