package com.example.mediatimerjp.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mediatimerjp.R
import com.example.mediatimerjp.database.Database
import com.example.mediatimerjp.databinding.FragmentSimpleTimerBinding
import com.example.mediatimerjp.model.AlarmPlayer
import com.example.mediatimerjp.model.TimerCommon
import com.example.mediatimerjp.model.TimerWrapper
import com.squareup.picasso.Picasso
import java.lang.Exception

class SimpleTimerFragment : Fragment() {


    private lateinit var wrapper: TimerWrapper

    private lateinit var binding: FragmentSimpleTimerBinding
    private lateinit var timer: TimerCommon
    private lateinit var spinner: Spinner
    private lateinit var media: Uri
    private lateinit var upload: String

    private lateinit var timerImage: ImageView
    private lateinit var timerVideo: VideoView

    private val PICK_IMAGE_REQUEST = 1

    private var soundPlayer:AlarmPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        wrapper = TimerWrapper.getInstance()
        binding = FragmentSimpleTimerBinding.inflate(inflater, container, false)
        return binding.root

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timer = arguments?.getParcelable("timer")!!
        media = timer.timer!!.media
        upload = timer.timer!!.upload
        timerImage = binding.timerImage
        timerVideo = binding.simpleTimerVideo
        if (media != Uri.EMPTY) {
            if (wrapper.isImageFile(media)) addImage(true)
            else  if(wrapper.isVideoFile(media)) addVideo(true)
            else addSound(true)
        }
        binding.groupName.setText(timer.name)
        timer.bigTextView = binding.hoursEditView
        timer.mediumTextView = binding.minutesEditView
        timer.smallTextView = binding.secondsEditView
        if (timer.timer!!.timerRunning) {
            timer.restartTimer()
        } else timer.updateView("timer", timer.timer!!.finished)

        spinner = binding.modesSpinner
        val adapter = ArrayAdapter.createFromResource(
            binding.root.context,
            R.array.modes_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                if (selectedItem == "HH/MM/SS") {
                    binding.hoursIndicator.text = "H"
                    binding.minutesIndicator.text = "M"
                    binding.secondsIndicator.text = "S"
                } else {
                    binding.hoursIndicator.text = "D"
                    binding.minutesIndicator.text = "H"
                    binding.secondsIndicator.text = "M"
                }
                timer.indicator.value = selectedItem
                timer.timer!!.mode = selectedItem
                timer.updateView("timer", timer.timer!!.finished)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }
        spinner.setSelection(getIndex(spinner, timer.timer!!.mode))

        binding.notificationsButton.setOnClickListener {
            val bundle = bundleOf("timer" to timer)
            findNavController().navigate(
                R.id.action_simpleTimerFragment_to_notificationsFragment,
                bundle
            )
        }

        binding.progressiveTimerText.setOnClickListener {
            val bundle = bundleOf("timer" to timer)
            findNavController().navigate(
                R.id.action_simpleTimerFragment_to_progressiveTimerFragment,
                bundle
            )
        }

        binding.addImagesButton.setOnClickListener {
            openFileChooser()
        }

        binding.alarmButton.setOnClickListener {
            val bundle = bundleOf("timer" to timer)
            findNavController().navigate(
                R.id.action_simpleTimerFragment_to_alarmFragment,
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

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*","audio/*"))
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            media = data.data!!
            context?.contentResolver?.takePersistableUriPermission(
                media,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            timer.timer!!.media = media
            context?.let { Database.uploadFile(media, it, timer.timer!!) }!!
            val runnable = Runnable {
                upload = timer.timer!!.upload
            }
            Handler().postDelayed(runnable, 2000)
            if (wrapper.isImageFile(media)) {
                addImage(false)
            } else if(wrapper.isVideoFile(media)){
                addVideo(false)
            }
            else{
                addSound(false)
            }
        }
    }

    private fun addImage(first: Boolean) {
        binding.showVideo.visibility = View.GONE
        binding.showSound.visibility = View.GONE
        binding.showImage.visibility = View.VISIBLE
        if (first) applyPicassoUrl()
        else applyPicassoUri()
    }

    private fun applyPicassoUri() {
        Picasso.get()
            .load(media)
            .placeholder(R.mipmap.ic_launcher)
            .fit()
            .centerCrop()
            .into(timerImage)
    }

    private fun applyPicassoUrl() {
        try{
            if (upload != "") {
                Picasso.get()
                    .load(upload)
                    .placeholder(R.mipmap.ic_launcher)
                    .fit()
                    .centerCrop()
                    .into(timerImage)
            }
        }catch (e: Exception){
            applyPicassoUri()
        }
    }


    private fun addVideo(first: Boolean) {
        binding.showImage.visibility = View.GONE
        binding.showSound.visibility = View.GONE
        binding.showVideo.visibility = View.VISIBLE
        binding.videoVolume.text = timer.timer!!.backgroundSoundVolume.toString()
        binding.videoSoundBar.max = 100
        binding.videoSoundBar.progress = timer.timer!!.backgroundSoundVolume
        if (first) timerVideo.setVideoURI(Uri.parse(upload))
        else timerVideo.setVideoURI(media)
        timerVideo.setOnPreparedListener { mp ->
            mp.isLooping = true
            mp.start()
            mp.setVolume(timer.timer!!.backgroundSoundVolume.toFloat(),
                timer.timer!!.backgroundSoundVolume.toFloat()
            )
            binding.videoSoundBar.setOnSeekBarChangeListener(@SuppressLint("AppCompatCustomView")
            object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser){
                        timer.timer!!.backgroundSoundVolume = progress
                        binding.videoVolume.text = progress.toString()
                        mp.setVolume(timer.timer!!.backgroundSoundVolume.toFloat(),
                            timer.timer!!.backgroundSoundVolume.toFloat()
                        )
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }

            })
        }
        val mediaController = MediaController(context)
        mediaController.setAnchorView(timerVideo)
        timerVideo.setMediaController(mediaController)
    }

    private fun addSound(b: Boolean) {
        binding.showVideo.visibility = View.GONE
        binding.showImage.visibility = View.GONE
        binding.showSound.visibility = View.VISIBLE
        timer.timer!!.context = requireContext()
        timer.timer!!.initializeBackgroundSound()
        if (timer.timer!!.backgroundSound.uri == Uri.EMPTY){
            if(b && timer.timer!!.backgroundSound.uri!=Uri.parse(upload.trimEnd())) timer.timer!!.setBackgroundSound(Uri.parse(upload.trimEnd()))
            else if(timer.timer!!.backgroundSound.uri!=media) timer.timer!!.setBackgroundSound(media)
        }
        soundPlayer = timer.timer!!.backgroundSound
        Toast.makeText(context, "${soundPlayer!!.getDuration()}", Toast.LENGTH_SHORT).show()
        initializeSeekBar()
        soundControls()
    }

    private fun soundControls(){
        binding.playButton.setOnClickListener {
            soundPlayer?.startSoundObject(false, "", 0, 0L, context)
        }
        binding.pauseButton.setOnClickListener {
            soundPlayer?.pauseSoundObject()
        }
        binding.stopButton.setOnClickListener {
            soundPlayer?.stopSoundObject(context)
        }
        binding.seekBar.setOnSeekBarChangeListener(@SuppressLint("AppCompatCustomView")
        object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) soundPlayer!!.setNewTime(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
        binding.backgroundSoundVolume.text = timer.timer!!.backgroundSoundVolume.toString()
        binding.backgroundSoundBar.max = 100
        binding.backgroundSoundBar.progress = timer.timer!!.backgroundSoundVolume
        binding.backgroundSoundBar.setOnSeekBarChangeListener(@SuppressLint("AppCompatCustomView")
        object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser){
                    soundPlayer!!.setVolume(progress)
                    timer.timer!!.backgroundSoundVolume = progress
                    binding.backgroundSoundVolume.text = progress.toString()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
    }

    private fun initializeSeekBar(){
        binding.seekBar.max = soundPlayer!!.getDuration()
        val handler = Handler()
        handler.postDelayed(object:Runnable{
            override fun run() {
                try {
                    binding.seekBar.progress = soundPlayer!!.getCurrentTime()
                    binding.timeLeft.text = wrapper.millisecondsToMinutesAndSeconds(soundPlayer!!.getDuration()-soundPlayer!!.getCurrentTime())
                    handler.postDelayed(this, 1000)
                }catch (e: Exception){
                    binding.seekBar.progress = 0
                }
            }

        }, 0)

    }

    override fun onPause() {
        super.onPause()
        timer.name = binding.groupName.text.toString()
        timer.nameTextView.text = binding.groupName.text.toString()
        timer.timer!!.name = binding.groupName.text.toString()
        timer.observableName.value = binding.groupName.text.toString()
        var time = TimerWrapper.getInstance().getTimerValue(
            timer.timer!!.mode,
            binding.secondsEditView,
            binding.minutesEditView,
            binding.hoursEditView
        )
        if (!timer.timer!!.timerRunning && (timer.timer!!.currentTimerValue/1000)!= (time/1000)) {
            timer.timer!!.currentTimerValue = time
            timer.timer!!.initialTimerValue = timer.timer!!.currentTimerValue
            timer.timer!!.initialTimerForProgressBar = timer.timer!!.currentTimerValue
        }
        context?.let {
            wrapper.saveTimersToSharedPreference(it, "SaveFile")
        }

    }
}