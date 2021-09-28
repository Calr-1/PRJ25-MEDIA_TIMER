package com.example.mediatimerjp.ui

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.mediatimerjp.R
import com.example.mediatimerjp.model.TimerColors
import com.example.mediatimerjp.model.TimerCommon
import com.example.mediatimerjp.model.TimerWrapper

class ColorPickerFragment : Fragment() {

    private lateinit var timer: TimerCommon
    private lateinit var type: String
    private var previousColor: Int = 0
    private var newColor: Int = 0
    val adapter = TimerAdapter()
    lateinit var timerList :RecyclerView
    private lateinit var typeList :Spinner

    private var selectedColor: Int = -1

    private var timerArray = MutableLiveData<ArrayList<TimerCommon>>()
    var listOfTimers = ArrayList<TimerCommon>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_color_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timer = arguments?.getParcelable("timer")!!
        type="Timer background"

        timerList = requireView().findViewById(R.id.timerListColor)

        timerList.adapter = adapter

        timerArray.observe(viewLifecycleOwner, {
            adapter.submitList(timerArray.value?.toList())
        })
        listOfTimers.add(timer)
        timerArray.value = listOfTimers
        typeList = requireView().findViewById(R.id.spinnerType)

        typeList.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if(selectedColor==-1){
                    if(type=="Button background"){
                        timer.timerColors.buttonBackground = previousColor
                    }
                    else if(type=="Text"){
                        timer.timerColors.textColor = previousColor
                    }
                    else if(type=="Timer background"){
                        timer.timerColors.backgroundColor = previousColor
                    }
                    else if(type=="Button foreground"){
                        timer.timerColors.buttonIconColor = previousColor
                    }
                    else if(type=="Favourite icon"){
                        timer.timerColors.favouriteColor = previousColor
                    }
                }
                else{
                    if(type=="Button background"){
                        timer.timerColors.buttonBackground = selectedColor
                    }
                    else if(type=="Text"){
                        timer.timerColors.textColor = selectedColor
                    }
                    else if(type=="Timer background"){
                        timer.timerColors.backgroundColor = selectedColor
                    }
                    else if(type=="Button foreground"){
                        timer.timerColors.buttonIconColor = selectedColor
                    }
                    else if(type=="Favourite icon"){
                        timer.timerColors.favouriteColor = selectedColor
                    }
                }
                type = parent.getItemAtPosition(position).toString()

                if(type=="Button background"){
                    previousColor = timer.timerColors.buttonBackground
                }
                else if(type=="Text"){
                    previousColor = timer.timerColors.textColor
                }
                else if(type=="Timer background"){
                    previousColor = timer.timerColors.backgroundColor
                }
                else if(type=="Button foreground"){
                    previousColor = timer.timerColors.buttonIconColor
                }
                else if(type=="Favourite icon"){
                    previousColor = timer.timerColors.favouriteColor
                }
                selectedColor = -1
                resetColor()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        if(type=="Button background"){
            previousColor = timer.timerColors.buttonBackground
        }
        else if(type=="Text"){
            previousColor = timer.timerColors.textColor
        }
        else if(type=="Timer background"){
            previousColor = timer.timerColors.backgroundColor
        }
        else if(type=="Button foreground"){
            previousColor = timer.timerColors.buttonIconColor
        }
        else if(type=="Favourite icon"){
            previousColor = timer.timerColors.favouriteColor
        }
        val redBar: SeekBar = requireView().findViewById(R.id.redBar)
        redBar.progress = Color.red(previousColor)
        redBar.progressDrawable.setColorFilter(
            Color.rgb(redBar.progress, 0, 0),
            PorterDuff.Mode.SRC_ATOP
        )

        val blueBar: SeekBar = requireView().findViewById(R.id.blueBar)
        blueBar.progress = Color.blue(previousColor)
        blueBar.progressDrawable.setColorFilter(
            Color.rgb(0, 0, blueBar.progress),
            PorterDuff.Mode.SRC_ATOP
        )


        val greenBar: SeekBar = requireView().findViewById(R.id.greenBar)
        greenBar.progress = Color.green(previousColor)
        greenBar.progressDrawable.setColorFilter(
            Color.rgb(0, greenBar.progress, 0),
            PorterDuff.Mode.SRC_ATOP
        )


        val redText: TextView = requireView().findViewById(R.id.redTextValue)
        redText.text = Color.red(previousColor).toString()
        val blueText: TextView = requireView().findViewById(R.id.blueTextValue)
        blueText.text = Color.blue(previousColor).toString()
        val greenText: TextView = requireView().findViewById(R.id.greenTextValue)
        greenText.text = Color.green(previousColor).toString()



        val currentValueText: TextView = requireView().findViewById(R.id.actualCurrentValue)
        currentValueText.text =
            StringBuilder().append(Color.red(previousColor)).append(",")
                .append(Color.green(previousColor)).append(",").append(
                    Color.blue(previousColor)
                ).toString()

        newColor = Color.rgb(redBar.progress, greenBar.progress, blueBar.progress)

        redBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                redText.text = progress.toString()
                redBar.progressDrawable.setColorFilter(
                    Color.rgb(progress, 0, 0),
                    PorterDuff.Mode.MULTIPLY
                )

                currentValueText.text =
                    StringBuilder().append(progress).append(",").append(greenBar.progress)
                        .append(",").append(blueBar.progress).toString()
                newColor = Color.rgb(
                    redBar.progress,
                    greenBar.progress,
                    blueBar.progress
                )
                if(type=="Button background"){
                    timer.timerColors.buttonBackground = newColor
                }
                else if(type=="Text"){
                    timer.timerColors.textColor = newColor
                }
                else if(type=="Timer background"){
                    timer.timerColors.backgroundColor = newColor
                }
                else if(type=="Button foreground"){
                    timer.timerColors.buttonIconColor = newColor
                }
                else if(type=="Favourite icon"){
                    timer.timerColors.favouriteColor = newColor
                }
                adapter.notifyDataSetChanged()

            }
        })
        greenBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                greenText.text = progress.toString()
                greenBar.progressDrawable.setColorFilter(
                    Color.rgb(0, progress, 0),
                    PorterDuff.Mode.MULTIPLY
                )

                currentValueText.text =
                    StringBuilder().append(redBar.progress).append(",")
                        .append(progress).append(",").append(blueBar.progress).toString()
                newColor = Color.rgb(
                    redBar.progress,
                    greenBar.progress,
                    blueBar.progress
                )
                if(type=="Button background"){
                    timer.timerColors.buttonBackground = newColor
                }
                else if(type=="Text"){
                    timer.timerColors.textColor = newColor
                }
                else if(type=="Timer background"){
                    timer.timerColors.backgroundColor = newColor
                }
                else if(type=="Button foreground"){
                    timer.timerColors.buttonIconColor = newColor
                }
                else if(type=="Favourite icon"){
                    timer.timerColors.favouriteColor = newColor
                }
                adapter.notifyDataSetChanged()
            }

        })
        blueBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                blueText.text = progress.toString()
                blueBar.progressDrawable.setColorFilter(
                    Color.rgb(0, 0, progress),
                    PorterDuff.Mode.MULTIPLY
                )

                currentValueText.text =
                    StringBuilder().append(redBar.progress).append(",")
                        .append(greenBar.progress).append(",").append(progress).toString()
                newColor = Color.rgb(
                    redBar.progress,
                    greenBar.progress,
                    blueBar.progress
                )
                if(type=="Button background"){
                    timer.timerColors.buttonBackground = newColor
                }
                else if(type=="Text"){
                    timer.timerColors.textColor = newColor
                }
                else if(type=="Timer background"){
                    timer.timerColors.backgroundColor = newColor
                }
                else if(type=="Button foreground"){
                    timer.timerColors.buttonIconColor = newColor
                }
                else if(type=="Favourite icon"){
                    timer.timerColors.favouriteColor = newColor
                }
                adapter.notifyDataSetChanged()

            }

        })
        //val resetButton: Button = requireView().findViewById<Button>(R.id.button2)
        //resetButton.setOnClickListener { v: View? -> resetColor() }
        val resetButton: Button = requireView().findViewById(R.id.button2)
        resetButton.setOnClickListener { v: View? ->

            if(type=="Button background"){
                timer.timerColors.buttonBackground = newColor
            }
            else if(type=="Text"){
                timer.timerColors.textColor = newColor
            }
            else if(type=="Timer background"){
                timer.timerColors.backgroundColor = newColor
            }
            else if(type=="Button foreground"){
                timer.timerColors.buttonIconColor = newColor
            }
            else if(type=="Favourite icon"){
                timer.timerColors.favouriteColor = newColor
            }

            selectedColor = newColor
            adapter.notifyDataSetChanged()
            activity?.applicationContext?.let {
                TimerWrapper.getInstance().saveTimersToSharedPreference(it, "SaveFile")
            }
        }


    }




    fun resetColor()
    {
        val redBar: SeekBar = requireView().findViewById(R.id.redBar)
        redBar.progress = Color.red(previousColor)
        redBar.progressDrawable.setColorFilter(
            Color.rgb(redBar.progress, 0, 0),
            PorterDuff.Mode.MULTIPLY
        )

        val blueBar: SeekBar = requireView().findViewById(R.id.blueBar)
        blueBar.progress = Color.blue(previousColor)
        blueBar.progressDrawable.setColorFilter(
            Color.rgb(0, 0, blueBar.progress),
            PorterDuff.Mode.MULTIPLY
        )


        val greenBar: SeekBar = requireView().findViewById(R.id.greenBar)
        greenBar.progress = Color.green(previousColor)
        greenBar.progressDrawable.setColorFilter(
            Color.rgb(0, greenBar.progress, 0),
            PorterDuff.Mode.MULTIPLY
        )


        val redText: TextView = requireView().findViewById(R.id.redTextValue)
        redText.text = Color.red(previousColor).toString()
        val blueText: TextView = requireView().findViewById(R.id.blueTextValue)
        blueText.text = Color.blue(previousColor).toString()
        val greenText: TextView = requireView().findViewById(R.id.greenTextValue)
        greenText.text = Color.green(previousColor).toString()



        val currentValueText: TextView = requireView().findViewById(R.id.actualCurrentValue)
        currentValueText.text =
            StringBuilder().append(Color.red(previousColor)).append(",")
                .append(Color.green(previousColor)).append(",").append(
                    Color.blue(previousColor)
                ).toString()

        newColor = Color.rgb(redBar.progress, greenBar.progress, blueBar.progress)


    }
    fun onBackPressed() {
        resetColor()
    }
    private fun setColors(currentColors : TimerColors, colors : TimerColors){
        currentColors.backgroundColor = colors.backgroundColor
        currentColors.buttonBackground = colors.buttonBackground
        currentColors.textColor = colors.textColor
        currentColors.buttonIconColor = colors.buttonIconColor
        currentColors.favouriteColor = colors.favouriteColor

    }
    private fun setAllColors(){
        if(timer.group!!.propagateTheme){
            for(timers in timer.group!!.timersAssociated){
                if(timers.type=="timer"){
                    timers.timer?.let { setColors(it.timerColors,timer.group!!.timerColors) }
                    timers.timerColors = timers.timer?.timerColors!!

                }
                else{
                    timers.group?.let { setColors(it.timerColors,timer.group!!.timerColors) }
                    timers.timerColors = timers.group?.timerColors!!}

            }

        }


    }
}