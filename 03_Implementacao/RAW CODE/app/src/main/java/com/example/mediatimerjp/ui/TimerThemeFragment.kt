package com.example.mediatimerjp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.mediatimerjp.R
import com.example.mediatimerjp.model.TimerColors
import com.example.mediatimerjp.model.TimerCommon


class TimerThemeFragment : Fragment() {

    private lateinit var timer: TimerCommon
    private var timerColors: TimerColors? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_timer_theme, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        timer = arguments?.getParcelable("timer")!!
        timer.setColors(requireActivity().applicationContext)
        timerColors = timerColors
        setSquareColors()
        requireView().findViewById<TextView>(R.id.backgroundColorSquare).setOnClickListener {
            val navController = Navigation.findNavController(view)
            val bundle = bundleOf("timer" to timer, "type" to "backgroundColorSquare")
            navController.navigate(
                    R.id.action_timerThemeFragment_to_fragmentColorPicker,
                    bundle
                )
        }
        requireView().findViewById<TextView>(R.id.bBackgroundColorSquare).setOnClickListener {
            val navController = Navigation.findNavController(view)
            val bundle = bundleOf("timer" to timer, "type" to "bBackgroundColorSquare")
            navController.navigate(
                R.id.action_timerThemeFragment_to_fragmentColorPicker,
                bundle
            )
        }
        requireView().findViewById<TextView>(R.id.textColorSquare).setOnClickListener {
            val navController = Navigation.findNavController(view)
            val bundle = bundleOf("timer" to timer, "type" to "textColorSquare")
            navController.navigate(
                R.id.action_timerThemeFragment_to_fragmentColorPicker,
                bundle
            )
        }
        requireView().findViewById<TextView>(R.id.bIconColorSquare).setOnClickListener {
            val navController = Navigation.findNavController(view)
            val bundle = bundleOf("timer" to timer, "type" to "bIconColorSquare")
            navController.navigate(
                R.id.action_timerThemeFragment_to_fragmentColorPicker,
                bundle
            )
        }
        requireView().findViewById<TextView>(R.id.favouriteColorSquare).setOnClickListener {
            val navController = Navigation.findNavController(view)
            val bundle = bundleOf("timer" to timer, "type" to "favouriteColorSquare")
            navController.navigate(
                R.id.action_timerThemeFragment_to_fragmentColorPicker,
                bundle
            )
        }

    }

    private fun setSquareColors() {
        val background: TextView = requireView().findViewById(R.id.backgroundColorSquare)
        background.setBackgroundColor(timer.timerColors.backgroundColor)
        val text: TextView = requireView().findViewById(R.id.textColorSquare)
        text.setBackgroundColor(timer.timerColors.textColor)
        val backgroundButton: TextView = requireView().findViewById(R.id.bBackgroundColorSquare)
        backgroundButton.setBackgroundColor(timer.timerColors.buttonBackground)
        val iconButton: TextView = requireView().findViewById(R.id.bIconColorSquare)
        iconButton.setBackgroundColor(timer.timerColors.buttonIconColor)
        val favourite: TextView = requireView().findViewById(R.id.favouriteColorSquare)
        favourite.setBackgroundColor(timer.timerColors.favouriteColor)
    }
}