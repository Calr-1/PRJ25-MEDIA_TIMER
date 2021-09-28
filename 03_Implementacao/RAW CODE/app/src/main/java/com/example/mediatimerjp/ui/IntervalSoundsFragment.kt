package com.example.mediatimerjp.ui

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mediatimerjp.database.Database
import com.example.mediatimerjp.databinding.FragmentIntervalSoundsBinding
import com.example.mediatimerjp.model.IntervalSound
import com.example.mediatimerjp.model.TimerCommon
import com.example.mediatimerjp.model.TimerWrapper
import java.util.*
import kotlin.collections.ArrayList

class IntervalSoundsFragment : Fragment() {

    private var _binding: FragmentIntervalSoundsBinding? = null
    private lateinit var wrapper: TimerWrapper
    private lateinit var binding: FragmentIntervalSoundsBinding
    private lateinit var timer: TimerCommon

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: IntervalSoundAdapter
    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private lateinit var intervalSounds: ArrayList<IntervalSound>

    private var size = 0
    private lateinit var sounds: ArrayList<Uri>

    private var pos = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        wrapper = TimerWrapper.getInstance()
        binding = FragmentIntervalSoundsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        timer = arguments?.getParcelable("timer")!!

        timer.timer!!.setIntervals(timer.timer!!.initialTimerValue)

        val type = timer.timer!!.notifications.getTypeOfInterval()
        when(type){
            "Number of Intervals"->{
                size = timer.timer!!.intervalArrayNumber.size
            }
            "Timed Intervals"->{
                size = timer.timer!!.intervalArrayTime.size
            }
            "Random Intervals"->{
                size = timer.timer!!.intervalArrayRandom.size
            }
        }
        sounds = timer.timer!!.sounds
        intervalSounds = ArrayList()
        createIntervalsList(size, sounds)

        mRecyclerView = binding.intervalsRecyclerView
        mRecyclerView.setHasFixedSize(true)
        mLayoutManager = LinearLayoutManager(activity)
        mAdapter = IntervalSoundAdapter(intervalSounds)

        mRecyclerView.layoutManager = mLayoutManager
        mRecyclerView.adapter = mAdapter

        pos = 0
        mAdapter.setOnItemClickListener(object : IntervalSoundAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                pos = position
                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE)
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone")
                var uri: Uri? = null
                try {
                    uri = intervalSounds[pos].uri
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, uri)
                } catch (exception: IndexOutOfBoundsException) {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, null as Uri?)
                }
                startActivityForResult(intent, 7)
            }
        })

        val cb: CheckBox = binding.randomSoundsCB
        cb.isChecked = timer.timer!!.notifications.isRandomSounds()
        cb.setOnClickListener { timer.timer!!.notifications.setRandomSounds(cb.isChecked) }


    }

    fun createIntervalsList(size: Int, sounds: ArrayList<Uri>) {
        for (i in 0 until size) {
            var uri: Uri? = null
            if (sounds.size > 0 && i < sounds.size) {
                uri = sounds[i]
                if (uri!=null && uri!=Uri.EMPTY) {
                    val value_split = uri.toString().split("/").toTypedArray()
                    intervalSounds.add(
                        IntervalSound(
                            "Sound " + (i + 1) + ":",
                            value_split[value_split.size - 1],
                            uri
                        )
                    )
                } else {
                    intervalSounds.add(IntervalSound("Sound " + (i + 1) + ":", "Default", uri))
                }
            } else {
                intervalSounds.add(IntervalSound("Sound " + (i + 1) + ":", "Default", Uri.EMPTY))
            }
        }
    }

    private fun selectSound(position: Int, name: String?) {
        intervalSounds[position].soundNumber = "Sound $position: "
        if (name != null) {
            intervalSounds[position].soundName = name
        }
        mAdapter.notifyItemChanged(position)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(" RESULT CODE ", resultCode.toString())
        if (resultCode != Activity.RESULT_CANCELED) {
            if (resultCode == Activity.RESULT_OK && requestCode == 7) {
                val uri = data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                if (uri != null) {
                    val value_split = uri.toString().split("/").toTypedArray()
                    selectSound(pos, value_split[value_split.size - 1])
                }
                if (uri != null) {
                    intervalSounds[pos].uri = uri
                    context?.let { Database.uploadSounds(uri, it, timer.timer!!) }
                }
            }
        }
    }

}