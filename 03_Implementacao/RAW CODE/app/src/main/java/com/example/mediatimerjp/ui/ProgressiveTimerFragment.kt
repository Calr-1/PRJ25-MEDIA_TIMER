package com.example.mediatimerjp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mediatimerjp.R
import com.example.mediatimerjp.databinding.FragmentProgressiveTimerBinding
import com.example.mediatimerjp.model.TimerCommon
import com.example.mediatimerjp.model.TimerWrapper


class ProgressiveTimerFragment : Fragment() {

    private lateinit var wrapper: TimerWrapper
    private lateinit var binding: FragmentProgressiveTimerBinding
    private lateinit var timer:TimerCommon

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        wrapper = TimerWrapper.getInstance()
        binding = FragmentProgressiveTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timer = arguments?.getParcelable("timer")!!
        wrapper.updateProgressiveViews(timer.timer!!.maxProgressiveValue, "HH/MM/SS", binding.maxSecondsEditInterval, binding.maxMinutesEditInterval, binding.maxHoursEditInterval)


        binding.enableProgressiveTimer.isChecked = timer.timer?.enableProgressiveTimer!!
        binding.enableProgressiveTimer.setOnCheckedChangeListener { buttonView, isChecked ->
            timer.timer!!.enableProgressiveTimer = isChecked
        }
        binding.measurementIncreaseSpinner.setSelection(getIndex(binding.measurementIncreaseSpinner, timer.timer!!.measurementIncrease))
        binding.measurementIncreaseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                timer.timer!!.measurementIncrease = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        binding.ConsecutiveIncrease.setText(timer.timer!!.consecutiveIncrease.toString())
        binding.secondsToIncrease.setText(timer.timer!!.secondsIncrease.toString())
        binding.checkHistoryTextText.setOnClickListener {
            val bundle = bundleOf("timer" to timer)
            findNavController().navigate(
                R.id.action_progressiveTimerFragment_to_historyFragment,
                bundle
            )
        }
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
        timer.timer!!.consecutiveIncrease = binding.ConsecutiveIncrease.text.toString().toInt()
        timer.timer!!.secondsIncrease = binding.secondsToIncrease.text.toString().toInt()
        timer.timer!!.maxProgressiveValue = wrapper.getTimerValue("HH/MM/SS", binding.maxSecondsEditInterval, binding.maxMinutesEditInterval, binding.maxHoursEditInterval)
    }
}