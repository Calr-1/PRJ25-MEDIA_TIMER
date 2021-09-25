package com.example.mediatimerjp.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mediatimerjp.R
import com.example.mediatimerjp.model.TimerWrapper


class ChangeThemeFragment : Fragment(), View.OnClickListener {

    private var settings: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_change_theme, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settings = PreferenceManager.getDefaultSharedPreferences(activity?.applicationContext)
        editor = (settings as SharedPreferences).edit()
        activity?.applicationContext?.let { TimerWrapper.getInstance().loadTheme(it) }
        getView()?.findViewById<View>(R.id.chooseBlueGreyColor)?.setOnClickListener(this)
        getView()?.findViewById<View>(R.id.chooseBlackColor)?.setOnClickListener(this)
        getView()?.findViewById<View>(R.id.chooseGreenColor)?.setOnClickListener(this)
        getView()?.findViewById<View>(R.id.chooseOrangeColor)?.setOnClickListener(this)
        getView()?.findViewById<View>(R.id.chooseRedBlackColor)?.setOnClickListener(this)
    }
    override fun onClick(view: View) {
        when (view.id) {
            R.id.chooseBlueGreyColor -> {
                editor?.putInt("currentTheme", 0)
                editor?.commit()
            }
            R.id.chooseOrangeColor -> {
                editor?.putInt("currentTheme", 1)
                editor?.commit()
            }
            R.id.chooseGreenColor -> {
                editor?.putInt("currentTheme", 2)
                editor?.commit()
            }
            R.id.chooseRedBlackColor -> {
                editor?.putInt("currentTheme", 3)
                editor?.commit()
            }
            R.id.chooseBlackColor -> {
                editor?.putInt("currentTheme", 4)
                editor?.commit()
            }
        }
        TimerWrapper.getInstance().currentModel.timerList.value?.forEach {
            activity?.applicationContext?.let { it1 -> it.setColors(it1) }
        }
        activity?.applicationContext?.let {
            TimerWrapper.getInstance().saveTimersToSharedPreference(it, "SaveFile")
        }

        activity?.recreate()
    }

    fun onBackPressed() {

    }
}